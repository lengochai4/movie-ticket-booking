package com.cinema.service.order;

import com.cinema.dao.order.BookingDAO;
import com.cinema.dto.BookingStats;
import com.cinema.dto.OrderActionResult;
import com.cinema.dto.OrderView;
import com.cinema.dto.PlaceBookingRequest;
import com.cinema.entity.Booking;
import com.cinema.entity.Movie;
import com.cinema.entity.Room;
import com.cinema.entity.Showtime;
import com.cinema.entity.Ticket;
import com.cinema.enums.BookingStatus;
import com.cinema.order.OrderText;
import com.cinema.order.QrCodeUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter CREATED = DateTimeFormatter.ofPattern("HH:mm '·' dd/MM/yyyy");

    private final BookingDAO bookingDAO = new BookingDAO();

    public List<OrderView> findHistory(String userId) {
        List<Booking> bookings = bookingDAO.findByUser(userId);
        List<OrderView> views = new ArrayList<>();
        for (Booking booking : bookings) {
            views.add(toView(booking, true));
        }
        return views;
    }

    public OrderView findTicket(String key) {
        Booking booking = bookingDAO.findByCodeOrId(key);
        if (booking == null) {
            return null;
        }
        return toView(booking, false);
    }

    public List<OrderView> searchForAdmin(String keyword, String statusName) {
        BookingStatus status = parseStatus(statusName);
        List<Booking> bookings = bookingDAO.search(keyword, status);
        List<OrderView> views = new ArrayList<>();
        for (Booking booking : bookings) {
            views.add(toView(booking, false));
        }
        return views;
    }

    public BookingStats stats() {
        return bookingDAO.stats();
    }

    public int ensurePoints(String userId, Integer sessionPoints) {
        if (userId == null || userId.isBlank()) {
            return sessionPoints == null ? 0 : sessionPoints;
        }
        return bookingDAO.ensurePoints(userId, sessionPoints);
    }

    public OrderActionResult placeBooking(PlaceBookingRequest request) {
        OrderActionResult result = bookingDAO.place(request);
        if (result.isSuccess() && result.getOrderCode() != null) {
            TicketMail.sendTicket(findTicket(result.getOrderCode()));
        }
        return result;
    }

    public OrderActionResult cancelForCustomer(String userId, String orderCode) {
        if (orderCode == null || orderCode.isBlank()) {
            return OrderActionResult.fail("Thiếu mã vé cần hủy.");
        }
        OrderActionResult result = bookingDAO.cancelForCustomer(userId, orderCode.trim());
        notifyCancelled(result);
        return result;
    }

    public OrderActionResult cancelForAdmin(String orderCode, String reason) {
        if (orderCode == null || orderCode.isBlank()) {
            return OrderActionResult.fail("Thiếu mã vé cần hủy.");
        }
        OrderActionResult result = bookingDAO.cancelForAdmin(orderCode.trim(), reason);
        notifyCancelled(result);
        return result;
    }

    private void notifyCancelled(OrderActionResult result) {
        if (result != null && result.isSuccess() && result.getOrderCode() != null) {
            TicketMail.sendCancelled(findTicket(result.getOrderCode()));
        }
    }

    public OrderActionResult updateStatus(String orderCode, String statusName, String reason) {
        if (orderCode == null || orderCode.isBlank()) {
            return OrderActionResult.fail("Thiếu mã vé.");
        }
        BookingStatus status = parseStatus(statusName);
        if ("CONFIRMED".equalsIgnoreCase(statusName)) {
            status = BookingStatus.PAID;
        }
        if (status == null) {
            return OrderActionResult.fail("Trạng thái không hợp lệ.");
        }
        OrderActionResult result = bookingDAO.updateStatus(orderCode.trim(), status, reason);
        if (result != null && result.isSuccess() && result.getOrderCode() != null) {
            if (status == BookingStatus.PAID) {
                TicketMail.sendTicket(findTicket(result.getOrderCode()));
            }
            if (status == BookingStatus.CANCELLED) {
                TicketMail.sendCancelled(findTicket(result.getOrderCode()));
            }
        }
        return result;
    }

    public BookingStatus parseStatus(String statusName) {
        if (statusName == null || statusName.isBlank()) {
            return null;
        }
        if ("CONFIRMED".equalsIgnoreCase(statusName.trim())) {
            return BookingStatus.PAID;
        }
        try {
            return BookingStatus.valueOf(statusName.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private OrderView toView(Booking booking, boolean customerCancelRule) {
        OrderView view = new OrderView();
        view.setBookingID(booking.getBookingID());
        view.setUserId(booking.getUserId());
        view.setOrderCode(booking.getOrderCode());
        view.setStatus(booking.getStatus() == null ? "" : booking.getStatus().name());
        view.setCustomerName(booking.getCustomerName());
        view.setPhone(booking.getPhone());
        view.setEmail(booking.getEmail());
        view.setTotalPrice(booking.getTotalPrice() == null ? 0 : booking.getTotalPrice());
        view.setCombos(booking.getCombos());
        view.setPosterURL(booking.getPosterURL());
        view.setMovieTitle(booking.getMovieTitle());
        view.setMovieFormat(isBlank(booking.getMovieFormat()) ? "2D Phụ Đề" : booking.getMovieFormat());
        view.setAgeRating(isBlank(booking.getAgeRating()) ? "" : booking.getAgeRating());
        view.setCinemaName(booking.getCinemaName());
        view.setCinemaAddress(booking.getCinemaAddress());
        view.setRoomName(booking.getRoomName());
        view.setQrImageUrl(QrCodeUtil.buildQrImageUrl(booking.getOrderCode()));
        int earned = booking.getPointsEarned() == null ? 0 : booking.getPointsEarned();
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.isPointsReversed()) {
            earned = 0;
        }
        view.setPointsEarned(earned);

        if (booking.getCreatedAt() != null) {
            view.setCreatedDate(booking.getCreatedAt().format(CREATED));
        } else {
            view.setCreatedDate("");
        }

        LocalDateTime start = booking.getShowStart();
        Showtime showtime = booking.getShowtime();
        if (showtime != null) {
            if (showtime.getStartTime() != null) {
                start = showtime.getStartTime();
            }
            Movie movie = showtime.getMovie();
            if (movie != null) {
                if (!isBlank(movie.getTitle())) {
                    view.setMovieTitle(movie.getTitle());
                }
                if (!isBlank(movie.getPosterURL())) {
                    view.setPosterURL(movie.getPosterURL());
                }
                if (!isBlank(movie.getAgeRating())) {
                    view.setAgeRating(movie.getAgeRating());
                }
            }
            Room room = showtime.getRoom();
            if (room != null) {
                if (!isBlank(room.getName())) {
                    view.setRoomName(room.getName());
                }
                if (room.getCinema() != null) {
                    if (!isBlank(room.getCinema().getName())) {
                        view.setCinemaName(room.getCinema().getName());
                    }
                    if (!isBlank(room.getCinema().getAddress())) {
                        view.setCinemaAddress(room.getCinema().getAddress());
                    }
                }
            }
        }

        if (start != null) {
            view.setStartTime(start.format(TIME));
            view.setShowDate(start.format(DATE));
        } else {
            view.setStartTime("");
            view.setShowDate("");
        }
        view.setRoomNumber(OrderText.roomNumber(view.getRoomName()));

        List<String> pills = new ArrayList<>();
        if (booking.getTickets() != null) {
            for (Ticket ticket : booking.getTickets()) {
                if (ticket.getSeatLabel() != null && !ticket.getSeatLabel().isBlank()) {
                    pills.add(ticket.getSeatLabel());
                }
            }
        }
        if (pills.isEmpty() && booking.getSeats() != null && !booking.getSeats().isBlank()) {
            String[] parts = booking.getSeats().split(",");
            for (String part : parts) {
                if (part != null && !part.trim().isEmpty()) {
                    pills.add(part.trim());
                }
            }
        }
        view.setSeatPills(pills);
        view.setSeats(pills.isEmpty() ? OrderText.blankToEmpty(booking.getSeats()) : String.join(", ", pills));

        boolean openStatus = booking.getStatus() == BookingStatus.PAID || booking.getStatus() == BookingStatus.PENDING;
        boolean beforeShow = start == null || start.isAfter(LocalDateTime.now());
        view.setCancellable(customerCancelRule && openStatus && beforeShow);
        if (!customerCancelRule) {
            view.setCancellable(booking.getStatus() != BookingStatus.CANCELLED);
        }
        return view;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
