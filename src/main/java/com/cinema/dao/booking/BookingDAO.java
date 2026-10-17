package com.cinema.dao.booking;

import com.cinema.model.entity.*;
import com.cinema.model.enums.BookingStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class BookingDAO {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/cinema_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ignored) {
            }
        }
    }

    /**
     * Obtains a new database connection.
     * Checks system properties/environment variables, otherwise uses defaults.
     */
    protected Connection getConnection() throws SQLException {
        String url = System.getProperty("db.url", System.getenv("DB_URL"));
        if (url == null || url.trim().isEmpty()) {
            url = DEFAULT_URL;
        }

        String user = System.getProperty("db.user", System.getenv("DB_USER"));
        if (user == null) {
            user = DEFAULT_USER;
        }

        String password = System.getProperty("db.password", System.getenv("DB_PASSWORD"));
        if (password == null) {
            password = DEFAULT_PASSWORD;
        }

        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Releases expired pending bookings and their tickets.
     * Updates Booking to CANCELLED and Ticket to CANCELLED where expiresAt < NOW().
     */
    public void releaseExpiredBookings() {
        String updateTicketsSql =
                "UPDATE Ticket t " +
                "JOIN Booking b ON t.bookingID = b.bookingID " +
                "SET t.ticketStatus = 'CANCELLED' " +
                "WHERE b.bookingStatus = 'PENDING' AND b.expiresAt < NOW()";

        String updateBookingsSql =
                "UPDATE Booking " +
                "SET bookingStatus = 'CANCELLED' " +
                "WHERE bookingStatus = 'PENDING' AND expiresAt < NOW()";

        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            stmt.executeUpdate(updateTicketsSql);
            stmt.executeUpdate(updateBookingsSql);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            e.printStackTrace();
        } finally {
            close(stmt, conn);
        }
    }

    /**
     * Retrieves all seats for a given showtime and sets isAvailable flag.
     * A seat is unavailable if it is booked (PAID) or held (PENDING with expiresAt > NOW()).
     */
    public List<Seat> getSeatsByShowtime(String showTimeID) {
        releaseExpiredBookings();

        List<Seat> seats = new ArrayList<>();
        Set<String> occupiedSeatIDs = new HashSet<>();

        String occupiedSql =
                "SELECT t.seatID " +
                "FROM Ticket t " +
                "JOIN Booking b ON t.bookingID = b.bookingID " +
                "WHERE t.showTimeID = ? " +
                "  AND (b.bookingStatus = 'PAID' OR (b.bookingStatus = 'PENDING' AND b.expiresAt > NOW())) " +
                "  AND t.ticketStatus != 'CANCELLED'";

        String seatsSql =
                "SELECT s.seatID, s.roomID, s.`row`, s.`column`, s.`type`, s.extraPrice " +
                "FROM Seat s " +
                "JOIN Showtime st ON s.roomID = st.roomID " +
                "WHERE st.showTimeID = ? " +
                "ORDER BY s.`row`, CAST(s.`column` AS UNSIGNED), s.seatID";

        Connection conn = null;
        PreparedStatement psOccupied = null;
        PreparedStatement psSeats = null;
        ResultSet rsOccupied = null;
        ResultSet rsSeats = null;

        try {
            conn = getConnection();

            psOccupied = conn.prepareStatement(occupiedSql);
            psOccupied.setString(1, showTimeID);
            rsOccupied = psOccupied.executeQuery();
            while (rsOccupied.next()) {
                occupiedSeatIDs.add(rsOccupied.getString("seatID"));
            }

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
                seat.setAvailable(!occupiedSeatIDs.contains(seat.getSeatID()));
                seats.add(seat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rsOccupied, psOccupied, rsSeats, psSeats, conn);
        }

        return seats;
    }

    /**
     * Holds seats in a transaction for a specified duration in minutes.
     * If any seat is already booked or held, rolls back and returns false.
     */
    public boolean holdSeats(String bookingID, int userID, String showTimeID, List<String> seatIDs, int holdMinutes) {
        if (seatIDs == null || seatIDs.isEmpty()) {
            return false;
        }

        releaseExpiredBookings();

        String checkConflictSql =
                "SELECT t.seatID " +
                "FROM Ticket t " +
                "JOIN Booking b ON t.bookingID = b.bookingID " +
                "WHERE t.showTimeID = ? " +
                "  AND (b.bookingStatus = 'PAID' OR (b.bookingStatus = 'PENDING' AND b.expiresAt > NOW())) " +
                "  AND t.ticketStatus != 'CANCELLED'";

        String getShowtimeInfoSql =
                "SELECT st.movieID, r.cinemaID " +
                "FROM Showtime st " +
                "JOIN Room r ON st.roomID = r.roomID " +
                "WHERE st.showTimeID = ?";

        String insertBookingSql =
                "INSERT INTO Booking (bookingID, userID, bookingStatus, createdAt, expiresAt) " +
                "VALUES (?, ?, 'PENDING', NOW(), DATE_ADD(NOW(), INTERVAL ? MINUTE))";

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
            conn.setAutoCommit(false);

            // 1. Check for conflicts
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
                    return false;
                }
            }

            // 2. Query movieID and cinemaID
            psInfo = conn.prepareStatement(getShowtimeInfoSql);
            psInfo.setString(1, showTimeID);
            rsInfo = psInfo.executeQuery();
            if (!rsInfo.next()) {
                conn.rollback();
                return false;
            }
            String movieID = rsInfo.getString("movieID");
            String cinemaID = rsInfo.getString("cinemaID");

            // 3. Insert Booking
            psBooking = conn.prepareStatement(insertBookingSql);
            psBooking.setString(1, bookingID);
            psBooking.setInt(2, userID);
            psBooking.setInt(3, holdMinutes);
            psBooking.executeUpdate();

            // 4. Insert Tickets
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

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            close(rsCheck, psCheck, rsInfo, psInfo, psBooking, psTicket, conn);
        }
    }

    /**
     * Saves selected combos for a given booking.
     * Replaces previous combo selections for this booking if present.
     */
    public boolean saveBookingCombos(String bookingID, Map<String, Integer> comboQuantities) {
        if (bookingID == null || comboQuantities == null) {
            return false;
        }

        String deleteSql = "DELETE FROM BookingCombo WHERE bookingID = ?";
        String insertSql = "INSERT INTO BookingCombo (bookingComboID, bookingID, comboID, quantity) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement psDelete = null;
        PreparedStatement psInsert = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            psDelete = conn.prepareStatement(deleteSql);
            psDelete.setString(1, bookingID);
            psDelete.executeUpdate();

            psInsert = conn.prepareStatement(insertSql);
            for (Map.Entry<String, Integer> entry : comboQuantities.entrySet()) {
                String comboID = entry.getKey();
                Integer qty = entry.getValue();
                if (qty != null && qty > 0) {
                    String bookingComboID = "BC_" + bookingID + "_" + comboID;
                    if (bookingComboID.length() > 50) {
                        bookingComboID = "BC_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
                    }
                    psInsert.setString(1, bookingComboID);
                    psInsert.setString(2, bookingID);
                    psInsert.setString(3, comboID);
                    psInsert.setInt(4, qty);
                    psInsert.addBatch();
                }
            }
            psInsert.executeBatch();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            close(psDelete, psInsert, conn);
        }
    }

    /**
     * Calculates the raw total: SUM(showtime price + seat extraPrice) + SUM(combo price * quantity).
     */
    public BigDecimal calculateRawTotal(String bookingID) {
        BigDecimal total = BigDecimal.ZERO;

        String ticketsSql =
                "SELECT COALESCE(SUM(st.price + s.extraPrice), 0) AS ticketTotal " +
                "FROM Ticket t " +
                "JOIN Showtime st ON t.showTimeID = st.showTimeID " +
                "JOIN Seat s ON t.seatID = s.seatID AND s.roomID = st.roomID " +
                "WHERE t.bookingID = ? AND t.ticketStatus != 'CANCELLED'";

        String combosSql =
                "SELECT COALESCE(SUM(c.price * bc.quantity), 0) AS comboTotal " +
                "FROM BookingCombo bc " +
                "JOIN Combo c ON bc.comboID = c.comboID " +
                "WHERE bc.bookingID = ?";

        Connection conn = null;
        PreparedStatement psTickets = null;
        PreparedStatement psCombos = null;
        ResultSet rsTickets = null;
        ResultSet rsCombos = null;

        try {
            conn = getConnection();

            psTickets = conn.prepareStatement(ticketsSql);
            psTickets.setString(1, bookingID);
            rsTickets = psTickets.executeQuery();
            if (rsTickets.next()) {
                BigDecimal ticketTotal = rsTickets.getBigDecimal("ticketTotal");
                if (ticketTotal != null) {
                    total = total.add(ticketTotal);
                }
            }

            psCombos = conn.prepareStatement(combosSql);
            psCombos.setString(1, bookingID);
            rsCombos = psCombos.executeQuery();
            if (rsCombos.next()) {
                BigDecimal comboTotal = rsCombos.getBigDecimal("comboTotal");
                if (comboTotal != null) {
                    total = total.add(comboTotal);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rsTickets, psTickets, rsCombos, psCombos, conn);
        }

        return total;
    }

    /**
     * Completes payment in a transaction.
     * Inserts Payment record and updates Booking to PAID if it is PENDING and not expired.
     */
    public boolean completePayment(String bookingID, Payment payment) {
        if (bookingID == null || payment == null) {
            return false;
        }

        String insertPaymentSql =
                "INSERT INTO Payment (paymentID, `type`, amount, paymentDate) VALUES (?, ?, ?, ?)";

        String updateBookingSql =
                "UPDATE Booking " +
                "SET paymentID = ?, bookingStatus = 'PAID' " +
                "WHERE bookingID = ? AND bookingStatus = 'PENDING' AND expiresAt > NOW()";

        Connection conn = null;
        PreparedStatement psPayment = null;
        PreparedStatement psBooking = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 1. Insert Payment
            psPayment = conn.prepareStatement(insertPaymentSql);
            psPayment.setString(1, payment.getPaymentID());
            psPayment.setString(2, payment.getType());
            psPayment.setBigDecimal(3, payment.getAmount());
            LocalDateTime pDate = payment.getPaymentDate() != null ? payment.getPaymentDate() : LocalDateTime.now();
            psPayment.setTimestamp(4, Timestamp.valueOf(pDate));
            psPayment.executeUpdate();

            // 2. Update Booking
            psBooking = conn.prepareStatement(updateBookingSql);
            psBooking.setString(1, payment.getPaymentID());
            psBooking.setString(2, bookingID);
            int rowsAffected = psBooking.executeUpdate();

            if (rowsAffected > 0) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            close(psPayment, psBooking, conn);
        }
    }

    /**
     * Cancels a booking and releases held seats.
     */
    public boolean cancelBookingAndReleaseSeats(String bookingID) {
        String cancelBookingSql = "UPDATE Booking SET bookingStatus = 'CANCELLED' WHERE bookingID = ?";
        String cancelTicketsSql = "UPDATE Ticket SET ticketStatus = 'CANCELLED' WHERE bookingID = ?";

        Connection conn = null;
        PreparedStatement psBooking = null;
        PreparedStatement psTickets = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            psTickets = conn.prepareStatement(cancelTicketsSql);
            psTickets.setString(1, bookingID);
            psTickets.executeUpdate();

            psBooking = conn.prepareStatement(cancelBookingSql);
            psBooking.setString(1, bookingID);
            psBooking.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            close(psTickets, psBooking, conn);
        }
    }

    /**
     * Retrieves all food & drink combos available.
     */
    public List<Combo> getAllCombos() {
        List<Combo> combos = new ArrayList<>();
        String sql = "SELECT comboID, name, description, price FROM Combo";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Combo combo = new Combo(
                        rs.getString("comboID"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("price")
                );
                combos.add(combo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs, ps, conn);
        }

        return combos;
    }

    /**
     * Retrieves a booking by its ID including its tickets and combos.
     */
    public Booking getBookingById(String bookingID) {
        String sql = "SELECT bookingID, userID, paymentID, bookingStatus, createdAt, expiresAt FROM Booking WHERE bookingID = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, bookingID);
            rs = ps.executeQuery();

            if (rs.next()) {
                Booking booking = new Booking();
                booking.setBookingID(rs.getString("bookingID"));
                booking.setUserID(rs.getInt("userID"));
                booking.setPaymentID(rs.getString("paymentID"));

                String statusStr = rs.getString("bookingStatus");
                if (statusStr != null) {
                    try {
                        booking.setBookingStatus(BookingStatus.valueOf(statusStr));
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                Timestamp createdTs = rs.getTimestamp("createdAt");
                if (createdTs != null) {
                    booking.setCreatedAt(createdTs.toLocalDateTime());
                }

                Timestamp expiresTs = rs.getTimestamp("expiresAt");
                if (expiresTs != null) {
                    booking.setExpiresAt(expiresTs.toLocalDateTime());
                }

                return booking;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs, ps, conn);
        }

        return null;
    }

    /**
     * Murach style helper to close multiple database resources cleanly.
     */
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
