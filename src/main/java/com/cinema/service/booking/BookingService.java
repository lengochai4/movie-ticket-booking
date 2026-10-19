package com.cinema.service.booking;

import com.cinema.dao.booking.BookingDAO;
import com.cinema.model.entity.Seat;

import java.util.List;
import java.util.UUID;

public class BookingService {

    private final BookingDAO bookingDAO;

    public BookingService() {
        this.bookingDAO = new BookingDAO();
    }

    public BookingService(BookingDAO bookingDAO) {
        this.bookingDAO = bookingDAO;
    }

    /**
     * Lấy danh sách toàn bộ ghế của suất chiếu kèm trạng thái khả dụng.
     */
    public List<Seat> getSeatMap(String showTimeID) {
        if (showTimeID == null || showTimeID.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return bookingDAO.getSeatsByShowtime(showTimeID.trim());
    }

    /**
     * Giữ ghế với mã booking duy nhất trong 10 phút.
     * Trả về bookingID nếu thành công, null nếu thất bại hoặc trùng ghế.
     */
    public String holdSeats(int userID, String showTimeID, List<String> seatIDs) {
        if (showTimeID == null || showTimeID.trim().isEmpty() || seatIDs == null || seatIDs.isEmpty()) {
            return null;
        }

        // Sinh mã đơn đặt vé duy nhất: BK_ + 8 ký tự viết hoa
        String bookingID = "BK_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        int holdMinutes = 10;

        boolean success = bookingDAO.holdSeats(bookingID, userID, showTimeID.trim(), seatIDs, holdMinutes);
        return success ? bookingID : null;
    }

    /**
     * Xác nhận thanh toán thành công cho đơn đặt vé.
     */
    public boolean confirmPayment(String bookingID, String paymentID) {
        if (bookingID == null || bookingID.trim().isEmpty()) {
            return false;
        }
        return bookingDAO.confirmPayment(bookingID.trim(), paymentID != null ? paymentID : "PAY_ONLINE");
    }
}
