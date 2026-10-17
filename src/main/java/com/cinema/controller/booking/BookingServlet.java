package com.cinema.controller.booking;

import com.cinema.model.entity.Combo;
import com.cinema.model.entity.Seat;
import com.cinema.service.booking.BookingService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@WebServlet("/booking/*")
public class BookingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private BookingService bookingService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.bookingService = new BookingService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        String pathInfo = request.getPathInfo();
        if (action != null && !action.trim().isEmpty()) {
            pathInfo = action.startsWith("/") ? action : "/" + action;
        } else if (pathInfo == null || pathInfo.equals("/")) {
            pathInfo = "/seats";
        }

        switch (pathInfo) {
            case "/seats":
                handleGetSeats(request, response);
                break;
            case "/combos":
                handleGetCombos(request, response);
                break;
            case "/checkout":
                handleGetCheckout(request, response);
                break;
            case "/success":
                handleGetSuccess(request, response);
                break;
            case "/cancel":
                handleCancelBooking(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        String pathInfo = request.getPathInfo();
        if (action != null && !action.trim().isEmpty()) {
            pathInfo = action.startsWith("/") ? action : "/" + action;
        } else if (pathInfo == null) {
            pathInfo = "";
        }

        switch (pathInfo) {
            case "/hold":
                handlePostHold(request, response);
                break;
            case "/saveCombos":
                handlePostSaveCombos(request, response);
                break;
            case "/pay":
                handlePostPay(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    /**
     * GET /booking/seats?showtimeId=...
     * Loads seats for showtime and forwards to seat-map.jsp
     */
    private void handleGetSeats(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String showtimeId = request.getParameter("showtimeId");
        if (showtimeId == null || showtimeId.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing showtimeId");
            return;
        }

        List<Seat> seats = bookingService.getSeatMap(showtimeId);
        request.setAttribute("seats", seats);
        request.setAttribute("showtimeId", showtimeId);

        String error = request.getParameter("error");
        if (error != null) {
            request.setAttribute("error", error);
        }

        request.getRequestDispatcher("/WEB-INF/views/booking/seat-map.jsp").forward(request, response);
    }

    /**
     * POST /booking/hold
     * Holds seats for 10 minutes.
     * On success -> redirects to /booking/combos?bookingId=...
     * On failure -> redirects back to /booking/seats?showtimeId=...&error=occupied
     */
    private void handlePostHold(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String showtimeId = request.getParameter("showtimeId");
        if (showtimeId == null || showtimeId.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing showtimeId");
            return;
        }

        // Support both multiple checkbox values and comma-separated string
        List<String> seatList = new ArrayList<>();
        String[] seatValues = request.getParameterValues("selectedSeats");
        if (seatValues != null && seatValues.length > 0) {
            for (String val : seatValues) {
                if (val != null) {
                    for (String part : val.split(",")) {
                        String trimmed = part.trim();
                        if (!trimmed.isEmpty() && !seatList.contains(trimmed)) {
                            seatList.add(trimmed);
                        }
                    }
                }
            }
        }

        if (seatList.isEmpty()) {
            String singleSeatParam = request.getParameter("selectedSeats");
            if (singleSeatParam != null && !singleSeatParam.trim().isEmpty()) {
                for (String part : singleSeatParam.split(",")) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty() && !seatList.contains(trimmed)) {
                        seatList.add(trimmed);
                    }
                }
            }
        }

        if (seatList.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/booking/seats?showtimeId=" + showtimeId + "&error=no_seats_selected");
            return;
        }

        // Default userID = 1 or from session if logged in
        int userID = 1;
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object sessionUser = session.getAttribute("user");
            if (sessionUser != null) {
                try {
                    // Reflection or duck-typing in case User class is mapped in session
                    java.lang.reflect.Method getIdMethod = sessionUser.getClass().getMethod("getUserID");
                    Object idVal = getIdMethod.invoke(sessionUser);
                    if (idVal instanceof Integer) {
                        userID = (Integer) idVal;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        String bookingId = bookingService.holdSeats(userID, showtimeId, seatList);
        if (bookingId != null) {
            response.sendRedirect(request.getContextPath() + "/booking/combos?bookingId=" + bookingId);
        } else {
            response.sendRedirect(request.getContextPath() + "/booking/seats?showtimeId=" + showtimeId + "&error=occupied");
        }
    }

    /**
     * GET /booking/combos?bookingId=...
     * Loads combos and forwards to combo-select.jsp
     */
    private void handleGetCombos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bookingId = request.getParameter("bookingId");
        if (bookingId == null || bookingId.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing bookingId");
            return;
        }

        List<Combo> combos = bookingService.getAllCombos();
        request.setAttribute("combos", combos);
        request.setAttribute("bookingId", bookingId);

        request.getRequestDispatcher("/WEB-INF/views/booking/combo-select.jsp").forward(request, response);
    }

    /**
     * POST /booking/saveCombos
     * Saves combo quantities and redirects to checkout.
     */
    private void handlePostSaveCombos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bookingId = request.getParameter("bookingId");
        if (bookingId == null || bookingId.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing bookingId");
            return;
        }

        Map<String, Integer> comboQuantities = new HashMap<>();

        // Parse format 1: combo_<comboID> = quantity
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String param = paramNames.nextElement();
            if (param.startsWith("combo_")) {
                String comboID = param.substring(6);
                try {
                    int qty = Integer.parseInt(request.getParameter(param));
                    if (qty > 0) {
                        comboQuantities.put(comboID, qty);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // Parse format 2: array inputs comboId[] and quantity[]
        String[] comboIds = request.getParameterValues("comboId");
        String[] quantities = request.getParameterValues("quantity");
        if (comboIds != null && quantities != null && comboIds.length == quantities.length) {
            for (int i = 0; i < comboIds.length; i++) {
                try {
                    int qty = Integer.parseInt(quantities[i]);
                    if (qty > 0) {
                        comboQuantities.put(comboIds[i], qty);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        bookingService.addCombosToBooking(bookingId, comboQuantities);
        response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId);
    }

    /**
     * GET /booking/checkout?bookingId=...
     * Calculates totals and forwards to checkout.jsp
     */
    private void handleGetCheckout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bookingId = request.getParameter("bookingId");
        if (bookingId == null || bookingId.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing bookingId");
            return;
        }

        int pointsToUse = 0;
        String pointsParam = request.getParameter("points");
        if (pointsParam != null) {
            try {
                pointsToUse = Math.max(0, Integer.parseInt(pointsParam));
            } catch (NumberFormatException ignored) {
            }
        }

        BigDecimal rawTotal = bookingService.getRawTotal(bookingId);
        BigDecimal finalAmount = bookingService.calculateFinalAmount(bookingId, pointsToUse);

        request.setAttribute("bookingId", bookingId);
        request.setAttribute("rawTotal", rawTotal);
        request.setAttribute("finalAmount", finalAmount);
        request.setAttribute("pointsUsed", pointsToUse);

        String error = request.getParameter("error");
        if (error != null) {
            request.setAttribute("error", error);
        }

        request.getRequestDispatcher("/WEB-INF/views/booking/checkout.jsp").forward(request, response);
    }

    /**
     * POST /booking/pay
     * Receives paymentType, pointsUsed and completes payment.
     * Success -> redirects to /booking/success?bookingId=...
     * Failure -> redirects to /booking/checkout?bookingId=...&error=failed
     */
    private void handlePostPay(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bookingId = request.getParameter("bookingId");
        if (bookingId == null || bookingId.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing bookingId");
            return;
        }

        String paymentType = request.getParameter("paymentType");
        if (paymentType == null || paymentType.trim().isEmpty()) {
            paymentType = "VNPAY";
        }

        int pointsUsed = 0;
        String pointsParam = request.getParameter("pointsUsed");
        if (pointsParam != null) {
            try {
                pointsUsed = Math.max(0, Integer.parseInt(pointsParam));
            } catch (NumberFormatException ignored) {
            }
        }

        BigDecimal finalAmount = bookingService.calculateFinalAmount(bookingId, pointsUsed);
        boolean success = bookingService.processCheckout(bookingId, paymentType, finalAmount);

        if (success) {
            response.sendRedirect(request.getContextPath() + "/booking/success?bookingId=" + bookingId);
        } else {
            response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId + "&error=failed");
        }
    }

    /**
     * GET /booking/success?bookingId=...
     * Forwards to booking-success.jsp
     */
    private void handleGetSuccess(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bookingId = request.getParameter("bookingId");
        if (bookingId == null || bookingId.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing bookingId");
            return;
        }

        request.setAttribute("bookingId", bookingId);
        request.getRequestDispatcher("/WEB-INF/views/booking/booking-success.jsp").forward(request, response);
    }

    /**
     * GET /booking/cancel?bookingId=...
     * Cancels booking and releases seats.
     */
    private void handleCancelBooking(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bookingId = request.getParameter("bookingId");
        if (bookingId != null && !bookingId.trim().isEmpty()) {
            bookingService.cancelBooking(bookingId);
        }
        response.sendRedirect(request.getContextPath() + "/");
    }
}
