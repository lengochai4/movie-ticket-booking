package com.cinema.service.booking;

import com.cinema.dao.booking.BookingDAO;
import com.cinema.model.entity.Booking;
import com.cinema.model.entity.Combo;
import com.cinema.model.entity.Payment;
import com.cinema.model.entity.Seat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
     * Gets the seat map for a given showtime, flagging available and occupied seats.
     */
    public List<Seat> getSeatMap(String showTimeID) {
        return bookingDAO.getSeatsByShowtime(showTimeID);
    }

    /**
     * Generates a booking ID (BK_...), holds seats for 10 minutes via DAO.
     * Returns bookingID if successful, or null if any seat is already taken.
     */
    public String holdSeats(int userID, String showTimeID, List<String> seatIDs) {
        if (seatIDs == null || seatIDs.isEmpty()) {
            return null;
        }

        String bookingID = "BK_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        if (bookingID.length() > 50) {
            bookingID = bookingID.substring(0, 50);
        }

        int holdMinutes = 10;
        boolean success = bookingDAO.holdSeats(bookingID, userID, showTimeID, seatIDs, holdMinutes);
        if (success) {
            return bookingID;
        }
        return null;
    }

    /**
     * Associates food/drink combos with the booking.
     */
    public boolean addCombosToBooking(String bookingID, Map<String, Integer> comboQuantities) {
        return bookingDAO.saveBookingCombos(bookingID, comboQuantities);
    }

    /**
     * Calculates the raw total (tickets + combos).
     */
    public BigDecimal getRawTotal(String bookingID) {
        return bookingDAO.calculateRawTotal(bookingID);
    }

    /**
     * Calculates the final amount after deducting loyalty points (1 point = 1,000 VND).
     */
    public BigDecimal calculateFinalAmount(String bookingID, int pointsToUse) {
        BigDecimal rawTotal = bookingDAO.calculateRawTotal(bookingID);
        if (rawTotal == null) {
            rawTotal = BigDecimal.ZERO;
        }

        if (pointsToUse <= 0) {
            return rawTotal;
        }

        BigDecimal discount = BigDecimal.valueOf((long) pointsToUse * 1000L);
        BigDecimal finalAmount = rawTotal.subtract(discount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }
        return finalAmount;
    }

    /**
     * Processes payment checkout for the booking.
     */
    public boolean processCheckout(String bookingID, String paymentType, BigDecimal amount) {
        if (bookingID == null || paymentType == null || amount == null) {
            return false;
        }

        String paymentID = "PAY_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        if (paymentID.length() > 50) {
            paymentID = paymentID.substring(0, 50);
        }

        Payment payment = new Payment(paymentID, paymentType, amount, LocalDateTime.now());
        return bookingDAO.completePayment(bookingID, payment);
    }

    /**
     * Cancels booking and releases held seats.
     */
    public boolean cancelBooking(String bookingID) {
        return bookingDAO.cancelBookingAndReleaseSeats(bookingID);
    }

    /**
     * Retrieves all available combos.
     */
    public List<Combo> getAllCombos() {
        return bookingDAO.getAllCombos();
    }

    /**
     * Retrieves booking details by ID.
     */
    public Booking getBooking(String bookingID) {
        return bookingDAO.getBookingById(bookingID);
    }
}
