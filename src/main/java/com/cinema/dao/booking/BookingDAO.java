package com.cinema.dao.booking;

import com.cinema.model.entity.Seat;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class BookingDAO {

    private static final String RENDER_URL = "jdbc:postgresql://dpg-dbapchnbtjjs7382rt0g-a.singapore-postgres.render.com:5432/cinema_postgres_db?sslmode=require";
    private static final String RENDER_USER = "cinema_postgres_db_user";
    private static final String RENDER_PASSWORD = "ipzu40YAWB3VmNjkPpGpMoBW4zREBtDo";

    private static final String LOCAL_URL = "jdbc:postgresql://localhost:5432/cinema_db";
    private static final String LOCAL_USER = "postgres";
    private static final String LOCAL_PASSWORD = "1234";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[BookingDAO] Không tìm thấy PostgreSQL Driver: " + e.getMessage());
        }
    }

    /**
     * Mở kết nối Database: Sử dụng PostgreSQL Cloud Render trực tiếp.
     */
    protected Connection getConnection() throws SQLException {
        String envUrl = System.getProperty("db.url", System.getenv("DB_URL"));
        String envUser = System.getProperty("db.user", System.getenv("DB_USER"));
        String envPassword = System.getProperty("db.password", System.getenv("DB_PASSWORD"));

        String url = (envUrl != null && !envUrl.trim().isEmpty()) ? envUrl : RENDER_URL;
        String user = (envUser != null && !envUser.trim().isEmpty()) ? envUser : RENDER_USER;
        String password = (envPassword != null) ? envPassword : RENDER_PASSWORD;

        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Tự động giải phóng các lượt giữ ghế (PENDING) đã quá thời hạn (expiresAt < CURRENT_TIMESTAMP).
     */
    public void releaseExpiredBookings() {
        String updateBookingsSql =
                "UPDATE Booking SET bookingStatus = 'CANCELLED' " +
                "WHERE bookingStatus = 'PENDING' AND expiresAt < CURRENT_TIMESTAMP";

        String updateTicketsSql =
                "UPDATE Ticket t SET ticketStatus = 'CANCELLED' " +
                "FROM Booking b " +
                "WHERE t.bookingID = b.bookingID " +
                "  AND b.bookingStatus = 'CANCELLED' AND t.ticketStatus = 'AVAILABLE'";

        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            stmt.executeUpdate(updateBookingsSql);
            stmt.executeUpdate(updateTicketsSql);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            System.err.println("[BookingDAO] Lỗi releaseExpiredBookings: " + e.getMessage());
        } finally {
            close(stmt, conn);
        }
    }

    /**
     * Lấy toàn bộ sơ đồ ghế theo Suất chiếu và đánh dấu trạng thái khả dụng isAvailable.
     * Ghế không khả dụng nếu đã có vé thuộc đơn PAID hoặc đơn PENDING còn hạn giữ chỗ.
     */
    public List<Seat> getSeatsByShowtime(String showTimeID) {
        // Luôn giải phóng các đơn giữ ghế quá hạn trước khi lấy sơ đồ
        releaseExpiredBookings();

        List<Seat> seats = new ArrayList<>();
        Set<String> occupiedSeatIDs = new HashSet<>();

        String occupiedSql =
                "SELECT t.seatID FROM Ticket t " +
                "JOIN Booking b ON t.bookingID = b.bookingID " +
                "WHERE t.showTimeID = ? AND t.ticketStatus != 'CANCELLED' " +
                "  AND (b.bookingStatus = 'PAID' OR (b.bookingStatus = 'PENDING' AND b.expiresAt > CURRENT_TIMESTAMP))";

        String seatsSql =
                "SELECT s.seatID, s.roomID, s.\"row\", s.\"column\", s.type, s.extraPrice " +
                "FROM Seat s " +
                "JOIN Showtime st ON s.roomID = st.roomID " +
                "WHERE st.showTimeID = ? " +
                "ORDER BY s.\"row\", CAST(s.\"column\" AS INTEGER), s.seatID";

        Connection conn = null;
        PreparedStatement psOccupied = null;
        PreparedStatement psSeats = null;
        ResultSet rsOccupied = null;
        ResultSet rsSeats = null;

        try {
            conn = getConnection();

            // 1. Lấy danh sách ID ghế đã bị chiếm chỗ
            psOccupied = conn.prepareStatement(occupiedSql);
            psOccupied.setString(1, showTimeID);
            rsOccupied = psOccupied.executeQuery();
            while (rsOccupied.next()) {
                occupiedSeatIDs.add(rsOccupied.getString("seatID"));
            }

            // 2. Lấy toàn bộ danh sách ghế trong phòng chiếu
            psSeats = conn.prepareStatement(seatsSql);
            psSeats.setString(1, showTimeID);
            rsSeats = psSeats.executeQuery();
            while (rsSeats.next()) {
                Seat seat = new Seat();
                seat.setSeatID(rsSeats.getString("seatID"));
                seat.setRoomID(rsSeats.getString("roomID"));
                seat.setRow(rsSeats.getString("row"));
                seat.setColumn(rsSeats.getString("column"));
                seat.setType(rsSeats.getString("type"));
                seat.setExtraPrice(rsSeats.getBigDecimal("extraPrice"));
                // Ghế khả dụng khi không nằm trong danh sách occupied
                seat.setAvailable(!occupiedSeatIDs.contains(seat.getSeatID()));
                seats.add(seat);
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] Lỗi getSeatsByShowtime: " + e.getMessage());
        } finally {
            close(rsOccupied, psOccupied, rsSeats, psSeats, conn);
        }

        return seats;
    }

    /**
     * Giữ ghế trong Transaction: kiểm tra trùng lặp, tạo đơn Booking PENDING và vé Ticket AVAILABLE.
     * Nếu có bất kỳ ghế nào đã bị chọn -> Rollback và trả về false.
     */
    public boolean holdSeats(String bookingID, int userID, String showTimeID, List<String> seatIDs, int holdMinutes) {
        if (seatIDs == null || seatIDs.isEmpty()) {
            return false;
        }

        releaseExpiredBookings();

        String checkConflictSql =
                "SELECT t.seatID FROM Ticket t " +
                "JOIN Booking b ON t.bookingID = b.bookingID " +
                "WHERE t.showTimeID = ? AND t.ticketStatus != 'CANCELLED' " +
                "  AND (b.bookingStatus = 'PAID' OR (b.bookingStatus = 'PENDING' AND b.expiresAt > CURRENT_TIMESTAMP))";

        String getShowtimeInfoSql =
                "SELECT st.movieID, r.cinemaID " +
                "FROM Showtime st " +
                "JOIN Room r ON st.roomID = r.roomID " +
                "WHERE st.showTimeID = ?";

        String insertBookingSql =
                "INSERT INTO Booking (bookingID, userID, bookingStatus, createdAt, expiresAt) " +
                "VALUES (?, ?, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + (CAST(? AS INTEGER) * INTERVAL '1 minute'))";

        String insertTicketSql =
                "INSERT INTO Ticket (ticketID, bookingID, movieID, cinemaID, showTimeID, seatID, ticketStatus) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'AVAILABLE')";

        Connection conn = null;
        PreparedStatement psCheck = null;
        PreparedStatement psInfo = null;
        PreparedStatement psBooking = null;
        PreparedStatement psTicket = null;
        ResultSet rsCheck = null;
        ResultSet rsInfo = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // 1. Kiểm tra ghế đã bị giữ/đặt chưa (Chống xung đột hớt tay trên)
            psCheck = conn.prepareStatement(checkConflictSql);
            psCheck.setString(1, showTimeID);
            rsCheck = psCheck.executeQuery();
            Set<String> takenSeats = new HashSet<>();
            while (rsCheck.next()) {
                takenSeats.add(rsCheck.getString("seatID"));
            }

            for (String seatId : seatIDs) {
                if (takenSeats.contains(seatId)) {
                    conn.rollback();
                    return false; // Ghế đã bị giữ, hủy transaction
                }
            }

            // 2. Lấy movieID và cinemaID từ Showtime
            psInfo = conn.prepareStatement(getShowtimeInfoSql);
            psInfo.setString(1, showTimeID);
            rsInfo = psInfo.executeQuery();
            if (!rsInfo.next()) {
                conn.rollback();
                return false;
            }
            String movieID = rsInfo.getString("movieID");
            String cinemaID = rsInfo.getString("cinemaID");

            // 3. Tạo đơn Booking mới với thời hạn giữ chỗ
            psBooking = conn.prepareStatement(insertBookingSql);
            psBooking.setString(1, bookingID);
            psBooking.setInt(2, userID);
            psBooking.setInt(3, holdMinutes);
            psBooking.executeUpdate();

            // 4. Batch insert danh sách vé Ticket
            psTicket = conn.prepareStatement(insertTicketSql);
            for (String seatId : seatIDs) {
                String ticketID = "TK_" + bookingID + "_" + seatId;
                if (ticketID.length() > 50) {
                    ticketID = "TK_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
                }
                psTicket.setString(1, ticketID);
                psTicket.setString(2, bookingID);
                psTicket.setString(3, movieID);
                psTicket.setString(4, cinemaID);
                psTicket.setString(5, showTimeID);
                psTicket.setString(6, seatId);
                psTicket.addBatch();
            }
            psTicket.executeBatch();

            conn.commit(); // Commit toàn bộ thay đổi
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            System.err.println("[BookingDAO] Lỗi Transaction holdSeats: " + e.getMessage());
            return false;
        } finally {
            close(rsCheck, psCheck, rsInfo, psInfo, psBooking, psTicket, conn);
        }
    }

    /**
     * Xác nhận thanh toán thành công: chuyển trạng thái booking sang PAID.
     */
    public boolean confirmPayment(String bookingID, String paymentID) {
        String sql = "UPDATE Booking SET bookingStatus = 'PAID', paymentID = ? WHERE bookingID = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, paymentID);
            ps.setString(2, bookingID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BookingDAO] Lỗi confirmPayment: " + e.getMessage());
            return false;
        } finally {
            close(ps, conn);
        }
    }

    private static void close(AutoCloseable... closeables) {
        if (closeables == null) return;
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
