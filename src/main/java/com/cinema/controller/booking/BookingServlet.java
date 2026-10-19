package com.cinema.controller.booking;

import com.cinema.model.entity.Combo;
import com.cinema.model.entity.Seat;
import com.cinema.service.booking.BookingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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

        String pathInfo = request.getPathInfo();
        String action = request.getParameter("action");
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
            case "/combo":
                handleGetCombos(request, response);
                break;
            case "/checkout":
                handleGetCheckout(request, response);
                break;
            case "/vnpay-return":
                handleVNPayReturn(request, response);
                break;
            case "/success":
                handleGetSuccess(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/movies");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        String action = request.getParameter("action");
        if (action != null && !action.trim().isEmpty()) {
            pathInfo = action.startsWith("/") ? action : "/" + action;
        } else if (pathInfo == null) {
            pathInfo = "";
        }

        switch (pathInfo) {
            case "/hold":
            case "/combo":
                handlePostHold(request, response);
                break;
            case "/saveCombos":
                handlePostSaveCombos(request, response);
                break;
            case "/pay":
            case "/complete":
                handlePostPay(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    /**
     * GET /booking/seats?showtimeId=...
     * Lấy sơ đồ ghế và forward sang seat-map.jsp
     */
    private void handleGetSeats(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String showtimeId = request.getParameter("showtimeId");
        if (showtimeId == null || showtimeId.trim().isEmpty()) {
            showtimeId = request.getParameter("id"); // Fallback
        }

        if (showtimeId == null || showtimeId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/movies");
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
     * Nhận danh sách ghế và giữ chỗ trong 10 phút.
     * Thành công -> redirect sang /booking/combos?bookingId=...
     * Thất bại (trùng ghế) -> redirect về /booking/seats?showtimeId=...&error=occupied
     */
    private void handlePostHold(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String showtimeId = request.getParameter("showtimeId");
        if (showtimeId == null || showtimeId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        // Đọc danh sách ghế từ form (hỗ trợ cả mảng checkbox và chuỗi phân cách bởi dấu phẩy)
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
            String singleParam = request.getParameter("selectedSeats");
            if (singleParam != null && !singleParam.trim().isEmpty()) {
                for (String part : singleParam.split(",")) {
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

        // Lấy userID: mặc định là 2 (Lê Ngọc Hải) hoặc từ session
        int userID = 2;
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userObj = session.getAttribute("loginUser");
            if (userObj == null) {
                userObj = session.getAttribute("user");
            }
            if (userObj != null) {
                try {
                    java.lang.reflect.Method getIdMethod = userObj.getClass().getMethod("getUserID");
                    Object idVal = getIdMethod.invoke(userObj);
                    if (idVal instanceof Integer) {
                        userID = (Integer) idVal;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        // Thực hiện giữ ghế với transaction chống tranh chấp
        String bookingId = bookingService.holdSeats(userID, showtimeId, seatList);

        if (bookingId != null) {
            // Lưu bookingId vào session để hỗ trợ fallback
            HttpSession curSession = request.getSession(true);
            curSession.setAttribute("currentBookingId", bookingId);
            curSession.setAttribute("currentShowtimeId", showtimeId);

            // Thành công -> chuyển sang bước chọn bắp nước
            response.sendRedirect(request.getContextPath() + "/booking/combos?bookingId=" + bookingId);
        } else {
            // Thất bại do bị trùng ghế hoặc ghế vừa bị đặt
            response.sendRedirect(request.getContextPath() + "/booking/seats?showtimeId=" + showtimeId + "&error=occupied");
        }
    }

    /**
     * GET /booking/combos
     * Nạp combos và bookingId, forward sang combo-select.jsp
     */
    private void handleGetCombos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bookingId = request.getParameter("bookingId");
        if (bookingId == null || bookingId.trim().isEmpty()) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                bookingId = (String) session.getAttribute("currentBookingId");
            }
        }

        if (bookingId == null || bookingId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        List<Combo> comboList = bookingService.getAllCombos();
        request.setAttribute("combos", comboList);
        request.setAttribute("bookingId", bookingId.trim());

        request.getRequestDispatcher("/WEB-INF/views/booking/combo-select.jsp").forward(request, response);
    }

    /**
     * POST /booking/saveCombos
     * Đọc các param bắt đầu bằng combo_, gọi saveCombos, redirect sang /booking/checkout?bookingId=...
     */
    private void handlePostSaveCombos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bookingId = request.getParameter("bookingId");
        if (bookingId == null || bookingId.trim().isEmpty()) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                bookingId = (String) session.getAttribute("currentBookingId");
            }
        }

        if (bookingId == null || bookingId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        Map<String, Integer> comboQuantities = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if (paramName.startsWith("combo_")) {
                String comboID = paramName.substring("combo_".length());
                String qtyStr = request.getParameter(paramName);
                if (qtyStr != null && !qtyStr.trim().isEmpty()) {
                    try {
                        int qty = Integer.parseInt(qtyStr.trim());
                        if (qty > 0) {
                            comboQuantities.put(comboID, qty);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        bookingService.saveCombos(bookingId.trim(), comboQuantities);
        response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId.trim());
    }

    /**
     * GET /booking/checkout
     * Tính tổng tiền, nạp attribute và forward sang checkout.jsp
     */
    private void handleGetCheckout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bookingId = request.getParameter("bookingId");
        if (bookingId == null || bookingId.trim().isEmpty()) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                bookingId = (String) session.getAttribute("currentBookingId");
            }
        }

        if (bookingId == null || bookingId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        int pointsUsed = 0;
        String pointsStr = request.getParameter("pointsUsed");
        if (pointsStr != null && !pointsStr.trim().isEmpty()) {
            try {
                pointsUsed = Math.max(0, Integer.parseInt(pointsStr.trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        BigDecimal totalAmount = bookingService.calculateFinalAmount(bookingId.trim(), pointsUsed);
        request.setAttribute("bookingId", bookingId.trim());
        request.setAttribute("totalAmount", totalAmount);
        request.setAttribute("pointsUsed", pointsUsed);

        String error = request.getParameter("error");
        if (error != null) {
            request.setAttribute("error", error);
        }

        request.getRequestDispatcher("/WEB-INF/views/booking/checkout.jsp").forward(request, response);
    }

    /**
     * POST /booking/pay
     * Nhận paymentType, pointsUsed, gọi processPayment.
     * Thành công -> redirect sang /booking/success?bookingId=...
     * Thất bại -> redirect sang /booking/checkout?bookingId=...&error=failed
     */
    private void handlePostPay(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bookingId = request.getParameter("bookingId");
        if (bookingId == null || bookingId.trim().isEmpty()) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                bookingId = (String) session.getAttribute("currentBookingId");
            }
        }

        if (bookingId == null || bookingId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        String paymentType = request.getParameter("paymentType");
        if (paymentType == null || paymentType.trim().isEmpty()) {
            paymentType = request.getParameter("paymentMethod"); // Fallback
        }
        if (paymentType == null || paymentType.trim().isEmpty()) {
            paymentType = "CASH";
        }

        int pointsUsed = 0;
        String pointsStr = request.getParameter("pointsUsed");
        if (pointsStr != null && !pointsStr.trim().isEmpty()) {
            try {
                pointsUsed = Math.max(0, Integer.parseInt(pointsStr.trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        // Nếu phương thức thanh toán là VNPAY -> chuyển hướng sang cổng thanh toán VNPay Sandbox
        if ("VNPAY".equalsIgnoreCase(paymentType.trim())) {
            BigDecimal finalAmount = bookingService.calculateFinalAmount(bookingId.trim(), pointsUsed);
            long amountVnd = finalAmount.longValue();
            String vnpayUrl = bookingService.createVNPayUrl(request, bookingId.trim(), amountVnd);
            response.sendRedirect(vnpayUrl);
            return;
        }

        // Các phương thức thanh toán trực tiếp khác (CASH / MOMO)
        boolean success = bookingService.processPayment(bookingId.trim(), paymentType.trim(), pointsUsed);
        if (success) {
            response.sendRedirect(request.getContextPath() + "/booking/success?bookingId=" + bookingId.trim());
        } else {
            response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId.trim() + "&error=failed");
        }
    }

    /**
     * GET /booking/vnpay-return
     * Nhận phản hồi IPN / Callback từ cổng thanh toán VNPay
     */
    private void handleVNPayReturn(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String responseCode = request.getParameter("vnp_ResponseCode");
        String txnRef = request.getParameter("vnp_TxnRef");
        String transactionNo = request.getParameter("vnp_TransactionNo");
        String vnpAmount = request.getParameter("vnp_Amount");

        String bookingId = null;
        if (txnRef != null && !txnRef.trim().isEmpty()) {
            if (txnRef.contains("-")) {
                bookingId = txnRef.split("-")[0];
            } else {
                int lastUnderscore = txnRef.lastIndexOf('_');
                if (lastUnderscore > 2) {
                    bookingId = txnRef.substring(0, lastUnderscore);
                } else {
                    bookingId = txnRef;
                }
            }
        }
        if (bookingId == null || bookingId.trim().isEmpty() || "BK".equals(bookingId)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                bookingId = (String) session.getAttribute("currentBookingId");
            }
        }

        if (bookingId == null || bookingId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        // Kiểm tra mã phản hồi VNPay: "00" là thành công
        if ("00".equals(responseCode)) {
            BigDecimal amount = null;
            if (vnpAmount != null && !vnpAmount.trim().isEmpty()) {
                try {
                    // VNPay quy định số tiền gửi sang và trả về đều nhân 100
                    amount = new BigDecimal(vnpAmount.trim()).divide(new BigDecimal(100));
                } catch (Exception ignored) {
                }
            }
            if (amount == null) {
                amount = bookingService.calculateFinalAmount(bookingId, 0);
            }

            boolean success = bookingService.completePaymentVNPay(bookingId, transactionNo, amount);
            if (success) {
                response.sendRedirect(request.getContextPath() + "/booking/success?bookingId=" + bookingId);
            } else {
                response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId + "&error=failed");
            }
        } else {
            // Người dùng hủy thanh toán hoặc giao dịch thất bại
            response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId + "&error=vnpay_cancelled");
        }
    }

    /**
     * GET /booking/success
     * Nạp bookingId, forward sang booking-success.jsp
     */
    private void handleGetSuccess(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bookingId = request.getParameter("bookingId");
        if (bookingId == null || bookingId.trim().isEmpty()) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                bookingId = (String) session.getAttribute("currentBookingId");
            }
        }

        if (bookingId == null || bookingId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        request.setAttribute("bookingId", bookingId.trim());
        request.getRequestDispatcher("/WEB-INF/views/booking/booking-success.jsp").forward(request, response);
    }
}
