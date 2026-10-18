package com.cinema.dao.order;

import com.cinema.dto.BookingStats;
import com.cinema.dto.OrderActionResult;
import com.cinema.dto.PlaceBookingRequest;
import com.cinema.entity.Booking;
import com.cinema.entity.Movie;
import com.cinema.entity.Room;
import com.cinema.entity.Seat;
import com.cinema.entity.Showtime;
import com.cinema.entity.Ticket;
import com.cinema.enums.BookingStatus;
import com.cinema.enums.TicketStatus;
import com.cinema.order.OrderText;
import com.cinema.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class BookingDAO {

    private static final String FETCH = "SELECT DISTINCT b FROM Booking b "
            + "LEFT JOIN FETCH b.tickets t "
            + "LEFT JOIN FETCH t.seat "
            + "LEFT JOIN FETCH b.showtime s "
            + "LEFT JOIN FETCH s.movie "
            + "LEFT JOIN FETCH s.room r "
            + "LEFT JOIN FETCH r.cinema ";

    private final LoyaltyAccountDAO loyaltyAccountDAO = new LoyaltyAccountDAO();

    public List<Booking> findByUser(String userId) {
        EntityManager em = open();
        try {
            TypedQuery<Booking> query = em.createQuery(
                    FETCH + "WHERE b.userId = :userId ORDER BY b.createdAt DESC", Booking.class);
            query.setParameter("userId", userId);
            List<Booking> list = distinct(query.getResultList());
            touchAll(list);
            return list;
        } catch (Exception e) {
            System.err.println("[BookingDAO] Lỗi findByUser: " + e.getMessage());
            throw new IllegalStateException("Không tải được lịch sử đặt vé.", e);
        } finally {
            em.close();
        }
    }

    public Booking findByCodeOrId(String key) {
        EntityManager em = open();
        try {
            return loadByCodeOrId(em, key);
        } catch (Exception e) {
            System.err.println("[BookingDAO] Lỗi findByCodeOrId: " + e.getMessage());
            throw new IllegalStateException("Không tải được vé điện tử.", e);
        } finally {
            em.close();
        }
    }

    public List<Booking> search(String keyword, BookingStatus status) {
        EntityManager em = open();
        try {
            StringBuilder jpql = new StringBuilder(FETCH);
            jpql.append("WHERE 1 = 1 ");
            if (status != null) {
                jpql.append("AND b.status = :status ");
            }
            if (keyword != null && !keyword.isBlank()) {
                jpql.append("AND (LOWER(b.orderCode) LIKE :kw ESCAPE '\\' "
                        + "OR LOWER(b.customerName) LIKE :kw ESCAPE '\\' "
                        + "OR LOWER(b.phone) LIKE :kw ESCAPE '\\' "
                        + "OR LOWER(b.email) LIKE :kw ESCAPE '\\' "
                        + "OR LOWER(b.movieTitle) LIKE :kw ESCAPE '\\') ");
            }
            jpql.append("ORDER BY b.createdAt DESC");
            TypedQuery<Booking> query = em.createQuery(jpql.toString(), Booking.class);
            if (status != null) {
                query.setParameter("status", status);
            }
            if (keyword != null && !keyword.isBlank()) {
                query.setParameter("kw", "%" + escapeLike(keyword.trim().toLowerCase(Locale.ROOT)) + "%");
            }
            List<Booking> list = distinct(query.getResultList());
            touchAll(list);
            return list;
        } catch (Exception e) {
            System.err.println("[BookingDAO] Lỗi search: " + e.getMessage());
            throw new IllegalStateException("Không tải được danh sách đơn vé.", e);
        } finally {
            em.close();
        }
    }

    public BookingStats stats() {
        EntityManager em = open();
        BookingStats stats = new BookingStats();
        try {
            List<Object[]> rows = em.createQuery(
                    "SELECT b.status, COUNT(b) FROM Booking b GROUP BY b.status", Object[].class)
                    .getResultList();
            int total = 0;
            if (rows != null) {
                for (Object[] row : rows) {
                    BookingStatus status = (BookingStatus) row[0];
                    int count = row[1] == null ? 0 : ((Number) row[1]).intValue();
                    total += count;
                    if (status == BookingStatus.PAID) {
                        stats.setPaidOrders(count);
                    } else if (status == BookingStatus.PENDING) {
                        stats.setPendingOrders(count);
                    } else if (status == BookingStatus.CANCELLED) {
                        stats.setCancelledOrders(count);
                    }
                }
            }
            stats.setTotalOrders(total);
            Object rawRevenue = em.createQuery(
                    "SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.status = :paid")
                    .setParameter("paid", BookingStatus.PAID)
                    .getSingleResult();
            double revenue = rawRevenue instanceof Number ? ((Number) rawRevenue).doubleValue() : 0;
            stats.setTotalRevenue(OrderText.vnd(revenue));
            return stats;
        } catch (Exception e) {
            System.err.println("[BookingDAO] Lỗi stats: " + e.getMessage());
            throw new IllegalStateException("Không tải được thống kê đơn vé.", e);
        } finally {
            em.close();
        }
    }

    public int ensurePoints(String userId, Integer sessionPoints) {
        EntityManager em = open();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            int points = loyaltyAccountDAO.ensure(em, userId, sessionPoints);
            trans.commit();
            return points;
        } catch (Exception e) {
            rollback(trans);
            System.err.println("[BookingDAO] Lỗi ensurePoints: " + e.getMessage());
            return sessionPoints == null ? 0 : sessionPoints;
        } finally {
            em.close();
        }
    }

    public OrderActionResult place(PlaceBookingRequest request) {
        if (request == null) {
            return OrderActionResult.fail("Thiếu thông tin đơn vé.");
        }
        if (isBlank(request.getUserId())) {
            return OrderActionResult.fail("Bạn cần đăng nhập trước khi đặt vé.");
        }
        if (isBlank(request.getShowtimeId())) {
            return OrderActionResult.fail("Không tìm thấy suất chiếu.");
        }
        List<String> seatTokens = splitSeats(request.getSelectedSeats());
        if (seatTokens.isEmpty()) {
            return OrderActionResult.fail("Vui lòng chọn ít nhất một ghế.");
        }

        EntityManager em = open();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            Showtime showtime = em.find(Showtime.class, request.getShowtimeId().trim());
            if (showtime == null) {
                rollback(trans);
                return OrderActionResult.fail("Không tìm thấy suất chiếu.");
            }
            Movie movie = showtime.getMovie();
            Room room = showtime.getRoom();

            Booking booking = new Booking();
            booking.setBookingID(UUID.randomUUID().toString());
            booking.setOrderCode(nextOrderCode(em));
            booking.setUserId(request.getUserId().trim());
            booking.setCustomerName(trimToNull(request.getCustomerName()));
            booking.setPhone(trimToNull(request.getPhone()));
            booking.setEmail(trimToNull(request.getEmail()));
            booking.setShowtime(showtime);
            booking.setStatus(request.isPaid() ? BookingStatus.PAID : BookingStatus.PENDING);
            booking.setCreatedAt(LocalDateTime.now());
            booking.setMovieFormat(isBlank(request.getMovieFormat()) ? "2D Phụ Đề" : request.getMovieFormat().trim());
            snapshotShow(booking, showtime, movie, room);

            double ticketTotal = positive(request.getTicketTotal());
            double comboTotal = positive(request.getComboTotal());
            double finalTotal = request.getFinalTotal() == null ? ticketTotal + comboTotal : positive(request.getFinalTotal());
            booking.setTicketPrice(ticketTotal);
            booking.setComboPrice(comboTotal);
            booking.setTotalPrice(finalTotal);
            booking.setCombos(OrderText.formatCombos(request.getCombosRaw()));
            booking.setPointsUsed(0);

            double eachPrice = seatTokens.isEmpty() ? 0 : ticketTotal / seatTokens.size();
            List<String> labels = new ArrayList<>();
            for (String token : seatTokens) {
                Seat seat = findSeat(em, room, token);
                String label = labelOf(seat, token);
                if (seatTaken(em, showtime.getShowtimeID(), seat, label)) {
                    rollback(trans);
                    return OrderActionResult.fail("Ghế " + label + " đã có người đặt. Vui lòng chọn ghế khác.");
                }
                Ticket ticket = new Ticket();
                ticket.setTicketID(UUID.randomUUID().toString());
                ticket.setSeat(seat);
                ticket.setSeatLabel(label);
                ticket.setStatus(TicketStatus.AVAILABLE);
                ticket.setPrice(eachPrice);
                booking.addTicket(ticket);
                labels.add(label);
            }
            booking.setSeats(String.join(", ", labels));

            Integer balance = null;
            if (booking.getStatus() == BookingStatus.PAID) {
                balance = awardPoints(em, booking);
            }
            em.persist(booking);
            trans.commit();

            OrderActionResult result = OrderActionResult.ok("Đã ghi nhận đơn vé " + booking.getOrderCode() + ".");
            result.setUserId(booking.getUserId());
            result.setOrderCode(booking.getOrderCode());
            result.setLoyaltyPoints(balance);
            return result;
        } catch (Exception e) {
            rollback(trans);
            System.err.println("[BookingDAO] Lỗi place: " + e.getMessage());
            return OrderActionResult.fail("Không lưu được đơn vé. Vui lòng thử lại.");
        } finally {
            em.close();
        }
    }

    public OrderActionResult cancelForCustomer(String userId, String orderCode) {
        EntityManager em = open();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            Booking booking = loadByCodeOrId(em, orderCode);
            if (booking == null) {
                rollback(trans);
                return OrderActionResult.fail("Không tìm thấy đơn vé.");
            }
            if (userId == null || !userId.equals(booking.getUserId())) {
                rollback(trans);
                return OrderActionResult.fail("Bạn không có quyền hủy đơn vé này.");
            }
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                rollback(trans);
                return OrderActionResult.fail("Đơn vé này đã được hủy trước đó.");
            }
            if (booking.getShowStart() != null && !booking.getShowStart().isAfter(LocalDateTime.now())) {
                rollback(trans);
                return OrderActionResult.fail("Không thể hủy vé sau giờ chiếu.");
            }
            Integer balance = applyCancel(em, booking, "Khách hàng tự hủy");
            trans.commit();
            return successCancel(booking, balance);
        } catch (Exception e) {
            rollback(trans);
            System.err.println("[BookingDAO] Lỗi cancelForCustomer: " + e.getMessage());
            return OrderActionResult.fail("Không hủy được vé. Vui lòng thử lại.");
        } finally {
            em.close();
        }
    }

    public OrderActionResult cancelForAdmin(String orderCode, String reason) {
        EntityManager em = open();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            Booking booking = loadByCodeOrId(em, orderCode);
            if (booking == null) {
                rollback(trans);
                return OrderActionResult.fail("Không tìm thấy đơn vé.");
            }
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                rollback(trans);
                return OrderActionResult.fail("Đơn vé này đã được hủy trước đó.");
            }
            String note = isBlank(reason) ? "Quản trị viên hủy vé" : reason.trim();
            Integer balance = applyCancel(em, booking, note);
            trans.commit();
            return successCancel(booking, balance);
        } catch (Exception e) {
            rollback(trans);
            System.err.println("[BookingDAO] Lỗi cancelForAdmin: " + e.getMessage());
            return OrderActionResult.fail("Không hủy được vé. Vui lòng thử lại.");
        } finally {
            em.close();
        }
    }

    public OrderActionResult updateStatus(String orderCode, BookingStatus newStatus, String reason) {
        if (newStatus == null) {
            return OrderActionResult.fail("Trạng thái không hợp lệ.");
        }
        if (newStatus == BookingStatus.CANCELLED) {
            return cancelForAdmin(orderCode, reason);
        }
        if (newStatus != BookingStatus.PAID) {
            return OrderActionResult.fail("Chỉ có thể xác nhận đã thanh toán hoặc hủy vé.");
        }

        EntityManager em = open();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            Booking booking = loadByCodeOrId(em, orderCode);
            if (booking == null) {
                rollback(trans);
                return OrderActionResult.fail("Không tìm thấy đơn vé.");
            }
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                rollback(trans);
                return OrderActionResult.fail("Đơn đã hủy, không thể chuyển sang đã thanh toán.");
            }
            if (booking.getStatus() == BookingStatus.PAID) {
                rollback(trans);
                return OrderActionResult.fail("Đơn vé đã ở trạng thái đã thanh toán.");
            }
            booking.setStatus(BookingStatus.PAID);
            Integer balance = awardPoints(em, booking);
            trans.commit();
            OrderActionResult result = OrderActionResult.ok("Đã xác nhận thanh toán đơn " + booking.getOrderCode() + ".");
            result.setUserId(booking.getUserId());
            result.setOrderCode(booking.getOrderCode());
            result.setLoyaltyPoints(balance);
            return result;
        } catch (Exception e) {
            rollback(trans);
            System.err.println("[BookingDAO] Lỗi updateStatus: " + e.getMessage());
            return OrderActionResult.fail("Không cập nhật được trạng thái đơn vé.");
        } finally {
            em.close();
        }
    }

    private Integer applyCancel(EntityManager em, Booking booking, String reason) {
        boolean wasPaid = booking.getStatus() == BookingStatus.PAID;
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelReason(reason);
        booking.setCancelledAt(LocalDateTime.now());
        for (Ticket ticket : booking.getTickets()) {
            ticket.setStatus(TicketStatus.CANCELLED);
        }
        if (!wasPaid || booking.isPointsReversed()) {
            return null;
        }
        int delta = 0;
        if (booking.getPointsEarned() != null) {
            delta -= booking.getPointsEarned();
        }
        if (booking.getPointsUsed() != null) {
            delta += booking.getPointsUsed();
        }
        booking.setPointsReversed(true);
        if (delta == 0) {
            return loyaltyAccountDAO.ensure(em, booking.getUserId(), 0);
        }
        return loyaltyAccountDAO.adjust(em, booking.getUserId(), delta);
    }

    private Integer awardPoints(EntityManager em, Booking booking) {
        if (booking.getPointsEarned() != null) {
            return null;
        }
        double total = booking.getTotalPrice() == null ? 0 : booking.getTotalPrice();
        int earned = (int) Math.floor(total / 10000.0);
        if (earned < 0) {
            earned = 0;
        }
        booking.setPointsEarned(earned);
        if (earned == 0) {
            return loyaltyAccountDAO.ensure(em, booking.getUserId(), 0);
        }
        return loyaltyAccountDAO.adjust(em, booking.getUserId(), earned);
    }

    private OrderActionResult successCancel(Booking booking, Integer balance) {
        String message = "Đã hủy vé " + booking.getOrderCode() + ".";
        if (balance != null) {
            message = message + " Điểm thưởng hiện có: " + balance + ".";
        }
        OrderActionResult result = OrderActionResult.ok(message);
        result.setUserId(booking.getUserId());
        result.setOrderCode(booking.getOrderCode());
        result.setLoyaltyPoints(balance);
        return result;
    }

    private Booking loadByCodeOrId(EntityManager em, String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String value = key.trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        }
        TypedQuery<Booking> query = em.createQuery(
                FETCH + "WHERE b.orderCode = :key OR b.bookingID = :key", Booking.class);
        query.setParameter("key", value);
        List<Booking> list = query.getResultList();
        if (list == null || list.isEmpty()) {
            return null;
        }
        Booking booking = list.get(0);
        touch(booking);
        return booking;
    }

    private boolean seatTaken(EntityManager em, String showtimeId, Seat seat, String label) {
        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(t) FROM Ticket t JOIN t.booking b "
                        + "WHERE b.showtime.showtimeID = :sid "
                        + "AND b.status <> :cancelled "
                        + "AND t.status <> :ticketCancelled "
                        + "AND (t.seatLabel = :label");
        if (seat != null && seat.getSeatID() != null) {
            jpql.append(" OR t.seat.seatID = :seatId");
        }
        jpql.append(")");
        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        query.setParameter("sid", showtimeId);
        query.setParameter("cancelled", BookingStatus.CANCELLED);
        query.setParameter("ticketCancelled", TicketStatus.CANCELLED);
        query.setParameter("label", label);
        if (seat != null && seat.getSeatID() != null) {
            query.setParameter("seatId", seat.getSeatID());
        }
        Long count = query.getSingleResult();
        return count != null && count > 0;
    }

    private Seat findSeat(EntityManager em, Room room, String token) {
        Seat byId = em.find(Seat.class, token);
        if (byId != null) {
            return byId;
        }
        if (room == null || room.getRoomID() == null) {
            return null;
        }
        List<Seat> byCode = em.createQuery(
                "SELECT s FROM Seat s WHERE s.room.roomID = :roomId AND (s.seatCode = :code OR s.seatCode = :padded)",
                Seat.class)
                .setParameter("roomId", room.getRoomID())
                .setParameter("code", token)
                .setParameter("padded", padSeatCode(token))
                .setMaxResults(1)
                .getResultList();
        if (byCode != null && !byCode.isEmpty()) {
            return byCode.get(0);
        }
        String row = token.replaceAll("[0-9].*", "");
        String digits = token.replaceAll("\\D", "");
        if (row.isEmpty() || digits.isEmpty()) {
            return null;
        }
        int col;
        try {
            col = Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return null;
        }
        List<Seat> byPos = em.createQuery(
                "SELECT s FROM Seat s WHERE s.room.roomID = :roomId AND s.seatRow = :row AND s.seatCol = :col",
                Seat.class)
                .setParameter("roomId", room.getRoomID())
                .setParameter("row", row)
                .setParameter("col", col)
                .setMaxResults(1)
                .getResultList();
        if (byPos == null || byPos.isEmpty()) {
            return null;
        }
        return byPos.get(0);
    }

    private String labelOf(Seat seat, String token) {
        if (seat == null) {
            return token;
        }
        String type = seat.getSeatType() == null ? "" : seat.getSeatType().trim();
        String row = seat.getSeatRow() == null ? "" : seat.getSeatRow().trim();
        String code = row + seat.getSeatCol();
        if (code.isEmpty()) {
            code = seat.getSeatCode() == null ? token : seat.getSeatCode();
        }
        if (type.isEmpty()) {
            return code;
        }
        return type + " " + code;
    }

    private String padSeatCode(String token) {
        String row = token.replaceAll("[0-9].*", "");
        String digits = token.replaceAll("\\D", "");
        if (row.isEmpty() || digits.isEmpty()) {
            return token;
        }
        if (digits.length() == 1) {
            return row + "0" + digits;
        }
        return row + digits;
    }

    private void snapshotShow(Booking booking, Showtime showtime, Movie movie, Room room) {
        booking.setShowStart(showtime.getStartTime());
        if (movie != null) {
            booking.setMovieTitle(movie.getTitle());
            booking.setPosterURL(movie.getPosterURL());
            booking.setAgeRating(movie.getAgeRating());
        }
        if (room != null) {
            booking.setRoomName(room.getName());
            if (room.getCinema() != null) {
                booking.setCinemaName(room.getCinema().getName());
                booking.setCinemaAddress(room.getCinema().getAddress());
            }
        }
    }

    private String nextOrderCode(EntityManager em) {
        int year = LocalDate.now().getYear();
        String prefix = "CNK-" + year + "-";
        List<String> codes = em.createQuery(
                "SELECT b.orderCode FROM Booking b WHERE b.orderCode LIKE :prefix ORDER BY b.orderCode DESC",
                String.class)
                .setParameter("prefix", prefix + "%")
                .setMaxResults(1)
                .getResultList();
        int next = 1;
        if (codes != null && !codes.isEmpty() && codes.get(0) != null) {
            String last = codes.get(0);
            int dash = last.lastIndexOf('-');
            if (dash >= 0 && dash < last.length() - 1) {
                try {
                    next = Integer.parseInt(last.substring(dash + 1)) + 1;
                } catch (NumberFormatException ignored) {
                    next = 1;
                }
            }
        }
        return prefix + String.format("%05d", next);
    }

    private List<Booking> distinct(List<Booking> list) {
        Map<String, Booking> unique = new LinkedHashMap<>();
        if (list != null) {
            for (Booking booking : list) {
                if (booking != null && booking.getBookingID() != null) {
                    unique.put(booking.getBookingID(), booking);
                }
            }
        }
        return new ArrayList<>(unique.values());
    }

    private void touchAll(List<Booking> list) {
        if (list == null) {
            return;
        }
        for (Booking booking : list) {
            touch(booking);
        }
    }

    private void touch(Booking booking) {
        if (booking == null) {
            return;
        }
        for (Ticket ticket : booking.getTickets()) {
            if (ticket.getSeat() != null) {
                ticket.getSeat().getSeatCode();
            }
        }
        Showtime showtime = booking.getShowtime();
        if (showtime != null) {
            if (showtime.getMovie() != null) {
                showtime.getMovie().getTitle();
            }
            if (showtime.getRoom() != null) {
                showtime.getRoom().getName();
                if (showtime.getRoom().getCinema() != null) {
                    showtime.getRoom().getCinema().getName();
                }
            }
        }
    }

    private List<String> splitSeats(String raw) {
        List<String> tokens = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return tokens;
        }
        String[] parts = raw.split(",");
        for (String part : parts) {
            if (part != null && !part.trim().isEmpty()) {
                tokens.add(part.trim());
            }
        }
        return tokens;
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private double positive(Double value) {
        if (value == null || value < 0) {
            return 0;
        }
        return value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private EntityManager open() {
        if (JPAUtil.getEntityManagerFactory() == null) {
            throw new IllegalStateException("Không kết nối được cơ sở dữ liệu.");
        }
        return JPAUtil.getEntityManagerFactory().createEntityManager();
    }

    private void rollback(EntityTransaction trans) {
        if (trans != null && trans.isActive()) {
            trans.rollback();
        }
    }
}
