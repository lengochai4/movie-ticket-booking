package com.cinema.dao.admin;

import com.cinema.dto.CinemaRevenueDTO;
import com.cinema.dto.DashboardSummaryDTO;
import com.cinema.dto.MovieRevenueDTO;
import com.cinema.dto.RevenueRecordDTO;
import com.cinema.dto.SeatFillDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Truy vấn thống kê CHỈ ĐỌC trên bảng bookings / tickets (do Hải, Như quản lý),
 * JDBC: ConnectionPool + PreparedStatement.
 * Chỉ tính các đơn đã thanh toán (bookingstatus = PAID), mốc thời gian theo bookings.createdat.
 */
public class StatisticsDAO {

    // Số vé của 1 đơn: đếm vé chưa hủy trong bảng tickets, nếu chưa có thì đếm số ghế trong cột seats ("F5,F6")
    private static final String TICKETS_OF_BOOKING =
            "COALESCE(NULLIF((SELECT COUNT(*) FROM tickets t WHERE t.bookingid = b.bookingid AND t.ticketstatus <> 'CANCELLED'), 0), "
            + "CARDINALITY(STRING_TO_ARRAY(NULLIF(b.seats, ''), ',')), 0)";

    // Tiền vé của 1 đơn: ticketprice, nếu trống thì lấy tổng tiền trừ tiền combo
    private static final String TICKET_REVENUE_OF_BOOKING =
            "COALESCE(b.ticketprice, COALESCE(b.totalprice, 0) - COALESCE(b.comboprice, 0))";

    // Tổng hợp doanh thu trong khoảng [from, to)
    public DashboardSummaryDTO getSummary(String paidStatus, LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT COALESCE(SUM(b.totalprice), 0), COALESCE(SUM(" + TICKET_REVENUE_OF_BOOKING + "), 0), "
                + "COALESCE(SUM(b.comboprice), 0), COUNT(*), COALESCE(SUM(" + TICKETS_OF_BOOKING + "), 0) "
                + "FROM bookings b WHERE b.bookingstatus = ? AND b.createdat >= ? AND b.createdat < ?";
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(sql);
            bindPeriod(ps, paidStatus, from, to);
            rs = ps.executeQuery();
            DashboardSummaryDTO summary = new DashboardSummaryDTO();
            if (rs.next()) {
                summary.setTotalRevenue(rs.getDouble(1));
                summary.setTicketRevenue(rs.getDouble(2));
                summary.setComboRevenue(rs.getDouble(3));
                summary.setBookingCount(rs.getLong(4));
                summary.setTicketCount(rs.getLong(5));
            }
            return summary;
        } catch (SQLException e) {
            System.err.println("[StatisticsDAO] Lỗi getSummary: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            close(rs, ps, connection, pool);
        }
    }

    // Thời điểm đặt và tổng tiền của các đơn đã thanh toán, để Servlet chia theo giờ/ngày/tháng
    public List<RevenueRecordDTO> getPaidBookingRecords(String paidStatus, LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT b.createdat, COALESCE(b.totalprice, 0) FROM bookings b "
                + "WHERE b.bookingstatus = ? AND b.createdat >= ? AND b.createdat < ? ORDER BY b.createdat";
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(sql);
            bindPeriod(ps, paidStatus, from, to);
            rs = ps.executeQuery();
            List<RevenueRecordDTO> list = new ArrayList<>();
            while (rs.next()) {
                Timestamp createdAt = rs.getTimestamp(1);
                if (createdAt == null) continue;
                RevenueRecordDTO r = new RevenueRecordDTO();
                r.setCreatedAt(createdAt.toLocalDateTime());
                r.setTotalPrice(rs.getDouble(2));
                list.add(r);
            }
            return list;
        } catch (SQLException e) {
            System.err.println("[StatisticsDAO] Lỗi getPaidBookingRecords: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            close(rs, ps, connection, pool);
        }
    }

    // Doanh thu theo cụm rạp
    public List<CinemaRevenueDTO> getRevenueByCinema(String paidStatus, LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT COALESCE(c.name, b.cinemaname, 'Khác'), COALESCE(SUM(b.totalprice), 0) "
                + "FROM bookings b "
                + "LEFT JOIN showtimes s ON s.showtimeid = b.showtimeid "
                + "LEFT JOIN rooms r ON r.roomid = s.room_id "
                + "LEFT JOIN cinema c ON c.cinemaid = r.cinema_id "
                + "WHERE b.bookingstatus = ? AND b.createdat >= ? AND b.createdat < ? "
                + "GROUP BY 1 ORDER BY 2 DESC";
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(sql);
            bindPeriod(ps, paidStatus, from, to);
            rs = ps.executeQuery();
            List<CinemaRevenueDTO> list = new ArrayList<>();
            while (rs.next()) {
                CinemaRevenueDTO c = new CinemaRevenueDTO();
                c.setCinemaName(rs.getString(1));
                c.setRevenue(rs.getDouble(2));
                list.add(c);
            }
            return list;
        } catch (SQLException e) {
            System.err.println("[StatisticsDAO] Lỗi getRevenueByCinema: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            close(rs, ps, connection, pool);
        }
    }

    // Top phim theo doanh thu (phần trăm đóng góp do Servlet tính)
    public List<MovieRevenueDTO> getTopMovies(String paidStatus, LocalDateTime from, LocalDateTime to, int limit) {
        String sql = "SELECT m.movieid, m.title, m.genre, m.duration, m.posterurl, "
                + "COUNT(DISTINCT b.showtimeid), COALESCE(SUM(" + TICKETS_OF_BOOKING + "), 0), COALESCE(SUM(b.totalprice), 0) "
                + "FROM bookings b "
                + "JOIN showtimes s ON s.showtimeid = b.showtimeid "
                + "JOIN movies m ON m.movieid = s.movie_id "
                + "WHERE b.bookingstatus = ? AND b.createdat >= ? AND b.createdat < ? "
                + "GROUP BY m.movieid, m.title, m.genre, m.duration, m.posterurl "
                + "ORDER BY 8 DESC LIMIT ?";
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(sql);
            bindPeriod(ps, paidStatus, from, to);
            ps.setInt(4, limit);
            rs = ps.executeQuery();
            List<MovieRevenueDTO> list = new ArrayList<>();
            while (rs.next()) {
                MovieRevenueDTO m = new MovieRevenueDTO();
                m.setMovieID(rs.getString(1));
                m.setTitle(rs.getString(2));
                m.setGenre(rs.getString(3) != null ? rs.getString(3) : "Điện ảnh");
                m.setDuration(rs.getInt(4));
                m.setPosterUrl(rs.getString(5));
                m.setShowtimesCount(rs.getLong(6));
                m.setTicketsSold(rs.getLong(7));
                m.setRevenue(Math.round(rs.getDouble(8)));
                list.add(m);
            }
            return list;
        } catch (SQLException e) {
            System.err.println("[StatisticsDAO] Lỗi getTopMovies: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            close(rs, ps, connection, pool);
        }
    }

    // Số vé đã bán / tổng số ghế của các suất chiếu bắt đầu trong [from, to)
    public SeatFillDTO getSeatFill(String paidStatus, LocalDateTime from, LocalDateTime to) {
        String soldSql = "SELECT COALESCE(SUM(" + TICKETS_OF_BOOKING + "), 0) FROM bookings b "
                + "JOIN showtimes s ON s.showtimeid = b.showtimeid "
                + "WHERE b.bookingstatus = ? AND s.starttime >= ? AND s.starttime < ?";
        // Tổng ghế = số ghế thật của phòng chiếu mỗi suất (không dùng cột rooms.capacity)
        String capacitySql = "SELECT COUNT(se.seatid) FROM showtimes s "
                + "JOIN seats se ON se.room_id = s.room_id "
                + "WHERE s.starttime >= ? AND s.starttime < ?";
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement psSold = null;
        PreparedStatement psCapacity = null;
        ResultSet rsSold = null;
        ResultSet rsCapacity = null;
        try {
            connection = pool.getConnection();
            SeatFillDTO fill = new SeatFillDTO();

            psSold = connection.prepareStatement(soldSql);
            bindPeriod(psSold, paidStatus, from, to);
            rsSold = psSold.executeQuery();
            if (rsSold.next()) fill.setSoldTickets(rsSold.getLong(1));

            psCapacity = connection.prepareStatement(capacitySql);
            psCapacity.setTimestamp(1, Timestamp.valueOf(from));
            psCapacity.setTimestamp(2, Timestamp.valueOf(to));
            rsCapacity = psCapacity.executeQuery();
            if (rsCapacity.next()) fill.setTotalSeats(rsCapacity.getLong(1));

            return fill;
        } catch (SQLException e) {
            System.err.println("[StatisticsDAO] Lỗi getSeatFill: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeResultSet(rsCapacity);
            DBUtil.closePreparedStatement(psCapacity);
            close(rsSold, psSold, connection, pool);
        }
    }

    // Số vé đã bán (đơn PAID) của từng suất chiếu bắt đầu trong [from, to): showtimeID -> số vé
    public Map<String, Long> getSoldTicketsByShowtime(String paidStatus, LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT b.showtimeid, COALESCE(SUM(" + TICKETS_OF_BOOKING + "), 0) FROM bookings b "
                + "JOIN showtimes s ON s.showtimeid = b.showtimeid "
                + "WHERE b.bookingstatus = ? AND s.starttime >= ? AND s.starttime < ? "
                + "GROUP BY b.showtimeid";
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(sql);
            bindPeriod(ps, paidStatus, from, to);
            rs = ps.executeQuery();
            Map<String, Long> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getString(1), rs.getLong(2));
            }
            return result;
        } catch (SQLException e) {
            System.err.println("[StatisticsDAO] Lỗi getSoldTicketsByShowtime: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            close(rs, ps, connection, pool);
        }
    }

    // Số đơn đặt vé chưa hủy (PENDING hoặc PAID) của 1 suất chiếu, dùng để chặn sửa/xóa suất đã bán vé
    public long countOpenBookings(String showtimeId, String cancelledStatus) {
        String sql = "SELECT COUNT(*) FROM bookings b WHERE b.showtimeid = ? AND b.bookingstatus <> ?";
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setString(1, showtimeId);
            ps.setString(2, cancelledStatus);
            rs = ps.executeQuery();
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            System.err.println("[StatisticsDAO] Lỗi countOpenBookings: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            close(rs, ps, connection, pool);
        }
    }

    // Số đơn đặt vé chưa hủy trên tất cả suất chiếu của 1 phim, dùng để chặn xóa phim đã bán vé
    public long countOpenBookingsByMovie(String movieId, String cancelledStatus) {
        String sql = "SELECT COUNT(*) FROM bookings b JOIN showtimes s ON s.showtimeid = b.showtimeid "
                + "WHERE s.movie_id = ? AND b.bookingstatus <> ?";
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setString(1, movieId);
            ps.setString(2, cancelledStatus);
            rs = ps.executeQuery();
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            System.err.println("[StatisticsDAO] Lỗi countOpenBookingsByMovie: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            close(rs, ps, connection, pool);
        }
    }

    private void bindPeriod(PreparedStatement ps, String paidStatus, LocalDateTime from, LocalDateTime to)
            throws SQLException {
        ps.setString(1, paidStatus);
        ps.setTimestamp(2, Timestamp.valueOf(from));
        ps.setTimestamp(3, Timestamp.valueOf(to));
    }

    private void close(ResultSet rs, PreparedStatement ps, Connection connection, ConnectionPool pool) {
        DBUtil.closeResultSet(rs);
        DBUtil.closePreparedStatement(ps);
        pool.freeConnection(connection);
    }
}
