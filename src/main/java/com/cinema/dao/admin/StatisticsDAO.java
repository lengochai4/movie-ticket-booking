package com.cinema.dao.admin;

import com.cinema.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Truy vấn thống kê CHỈ ĐỌC trên bảng bookings / tickets (do Hải, Như quản lý).
 * Dùng native query để không phụ thuộc entity Booking của module khác.
 * Chỉ tính các đơn đã thanh toán (status = PAID), mốc thời gian theo bookings.created_at.
 */
public class StatisticsDAO {

    // Số vé của 1 đơn: đếm vé chưa hủy trong bảng tickets, nếu chưa có thì đếm số ghế trong cột seats ("F5,F6")
    private static final String TICKETS_OF_BOOKING =
            "COALESCE(NULLIF((SELECT COUNT(*) FROM tickets t WHERE t.booking_id = b.booking_id AND t.status <> 'CANCELLED'), 0), "
            + "CARDINALITY(STRING_TO_ARRAY(NULLIF(b.seats, ''), ',')), 0)";

    // Tiền vé của 1 đơn: ticket_price, nếu trống thì lấy tổng tiền trừ tiền combo
    private static final String TICKET_REVENUE_OF_BOOKING =
            "COALESCE(b.ticket_price, COALESCE(b.total_price, 0) - COALESCE(b.combo_price, 0))";

    /**
     * Tổng hợp trong khoảng [from, to).
     * @return {tổng doanh thu, doanh thu vé, doanh thu combo, số đơn, số vé}
     */
    public double[] getSummary(String paidStatus, LocalDateTime from, LocalDateTime to) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String sql = "SELECT COALESCE(SUM(b.total_price), 0), COALESCE(SUM(" + TICKET_REVENUE_OF_BOOKING + "), 0), "
                    + "COALESCE(SUM(b.combo_price), 0), COUNT(*), COALESCE(SUM(" + TICKETS_OF_BOOKING + "), 0) "
                    + "FROM bookings b WHERE b.status = ?1 AND b.created_at >= ?2 AND b.created_at < ?3";
            Object[] row = (Object[]) bind(em.createNativeQuery(sql), paidStatus, from, to).getSingleResult();
            return new double[] { num(row[0]), num(row[1]), num(row[2]), num(row[3]), num(row[4]) };
        } catch (Exception e) {
            System.err.println("[StatisticsDAO] Lỗi getSummary: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    // Danh sách (thời điểm đặt, tổng tiền) của các đơn đã thanh toán, để Servlet chia theo giờ/ngày/tháng
    public List<Object[]> getPaidBookingTimes(String paidStatus, LocalDateTime from, LocalDateTime to) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String sql = "SELECT b.created_at, COALESCE(b.total_price, 0) FROM bookings b "
                    + "WHERE b.status = ?1 AND b.created_at >= ?2 AND b.created_at < ?3 ORDER BY b.created_at";
            return castRows(bind(em.createNativeQuery(sql), paidStatus, from, to).getResultList());
        } catch (Exception e) {
            System.err.println("[StatisticsDAO] Lỗi getPaidBookingTimes: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    // Doanh thu theo cụm rạp: {tên rạp, doanh thu}
    public List<Object[]> getRevenueByCinema(String paidStatus, LocalDateTime from, LocalDateTime to) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String sql = "SELECT COALESCE(c.name, b.cinema_name, 'Khác'), COALESCE(SUM(b.total_price), 0) "
                    + "FROM bookings b "
                    + "LEFT JOIN showtimes s ON s.showtimeid = b.showtime_id "
                    + "LEFT JOIN rooms r ON r.roomid = s.room_id "
                    + "LEFT JOIN cinema c ON c.cinemaid = r.cinema_id "
                    + "WHERE b.status = ?1 AND b.created_at >= ?2 AND b.created_at < ?3 "
                    + "GROUP BY 1 ORDER BY 2 DESC";
            return castRows(bind(em.createNativeQuery(sql), paidStatus, from, to).getResultList());
        } catch (Exception e) {
            System.err.println("[StatisticsDAO] Lỗi getRevenueByCinema: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    // Top phim theo doanh thu: {movieID, title, genre, duration, posterURL, số suất có bán, số vé, doanh thu}
    public List<Object[]> getTopMovies(String paidStatus, LocalDateTime from, LocalDateTime to, int limit) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String sql = "SELECT m.movieid, m.title, m.genre, m.duration, m.posterurl, "
                    + "COUNT(DISTINCT b.showtime_id), COALESCE(SUM(" + TICKETS_OF_BOOKING + "), 0), COALESCE(SUM(b.total_price), 0) "
                    + "FROM bookings b "
                    + "JOIN showtimes s ON s.showtimeid = b.showtime_id "
                    + "JOIN movies m ON m.movieid = s.movie_id "
                    + "WHERE b.status = ?1 AND b.created_at >= ?2 AND b.created_at < ?3 "
                    + "GROUP BY m.movieid, m.title, m.genre, m.duration, m.posterurl "
                    + "ORDER BY 8 DESC";
            Query q = bind(em.createNativeQuery(sql), paidStatus, from, to);
            q.setMaxResults(limit);
            return castRows(q.getResultList());
        } catch (Exception e) {
            System.err.println("[StatisticsDAO] Lỗi getTopMovies: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    /**
     * Tỷ lệ lấp đầy của các suất chiếu bắt đầu trong [from, to).
     * @return {số vé đã bán, tổng số ghế}
     */
    public double[] getSeatFill(String paidStatus, LocalDateTime from, LocalDateTime to) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String soldSql = "SELECT COALESCE(SUM(" + TICKETS_OF_BOOKING + "), 0) FROM bookings b "
                    + "JOIN showtimes s ON s.showtimeid = b.showtime_id "
                    + "WHERE b.status = ?1 AND s.starttime >= ?2 AND s.starttime < ?3";
            String capacitySql = "SELECT COALESCE(SUM(r.capacity), 0) FROM showtimes s "
                    + "JOIN rooms r ON r.roomid = s.room_id "
                    + "WHERE s.starttime >= ?1 AND s.starttime < ?2";
            Object sold = bind(em.createNativeQuery(soldSql), paidStatus, from, to).getSingleResult();
            Object capacity = em.createNativeQuery(capacitySql)
                    .setParameter(1, from)
                    .setParameter(2, to)
                    .getSingleResult();
            return new double[] { num(sold), num(capacity) };
        } catch (Exception e) {
            System.err.println("[StatisticsDAO] Lỗi getSeatFill: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    // Số vé đã bán (đơn PAID) của từng suất chiếu bắt đầu trong [from, to): showtimeID -> số vé
    public Map<String, Long> getSoldTicketsByShowtime(String paidStatus, LocalDateTime from, LocalDateTime to) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String sql = "SELECT b.showtime_id, COALESCE(SUM(" + TICKETS_OF_BOOKING + "), 0) FROM bookings b "
                    + "JOIN showtimes s ON s.showtimeid = b.showtime_id "
                    + "WHERE b.status = ?1 AND s.starttime >= ?2 AND s.starttime < ?3 "
                    + "GROUP BY b.showtime_id";
            Map<String, Long> result = new HashMap<>();
            for (Object[] row : castRows(bind(em.createNativeQuery(sql), paidStatus, from, to).getResultList())) {
                result.put((String) row[0], (long) num(row[1]));
            }
            return result;
        } catch (Exception e) {
            System.err.println("[StatisticsDAO] Lỗi getSoldTicketsByShowtime: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    // Số đơn đặt vé chưa hủy (PENDING hoặc PAID) của 1 suất chiếu, dùng để chặn sửa/xóa suất đã bán vé
    public long countOpenBookings(String showtimeId, String cancelledStatus) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Object count = em.createNativeQuery(
                    "SELECT COUNT(*) FROM bookings b WHERE b.showtime_id = ?1 AND b.status <> ?2")
                    .setParameter(1, showtimeId)
                    .setParameter(2, cancelledStatus)
                    .getSingleResult();
            return (long) num(count);
        } catch (Exception e) {
            System.err.println("[StatisticsDAO] Lỗi countOpenBookings: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    private Query bind(Query q, String paidStatus, LocalDateTime from, LocalDateTime to) {
        return q.setParameter(1, paidStatus).setParameter(2, from).setParameter(3, to);
    }

    private List<Object[]> castRows(List<?> rows) {
        List<Object[]> result = new ArrayList<>();
        for (Object r : rows) {
            result.add((Object[]) r);
        }
        return result;
    }

    private double num(Object o) {
        return o instanceof Number ? ((Number) o).doubleValue() : 0;
    }
}
