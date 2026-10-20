package com.cinema.controller.booking;

import com.cinema.entity.Combo;
import com.cinema.entity.Seat;
import com.cinema.order.LoginUserSupport;
import com.cinema.service.booking.BookingService;
import com.cinema.service.order.LoyaltyService;
import com.cinema.util.VNPayConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@WebServlet("/booking/*")
public class BookingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private BookingService bookingService;
    private LoyaltyService loyaltyService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.bookingService = new BookingService();
        this.loyaltyService = new LoyaltyService();
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
     * Lấy User ID của người dùng đang đăng nhập trong Session (dạng UUID String).
     */
    private String resolveUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object userObj = session.getAttribute("loginUser");
        if (userObj == null) {
            userObj = session.getAttribute("user");
        }
        return LoginUserSupport.getUserId(userObj);
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

        // Đọc danh sách ghế từ form
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

        // Lấy thông tin người đặt vé từ session (nếu có)
        HttpSession session = request.getSession(false);
        Object userObj = session != null ? session.getAttribute("loginUser") : null;
        if (userObj == null && session != null) {
            userObj = session.getAttribute("user");
        }

        String userId = LoginUserSupport.getUserId(userObj);
        String customerName = LoginUserSupport.getFullName(userObj);
        String email = LoginUserSupport.getEmail(userObj);
        String phone = LoginUserSupport.getPhone(userObj);

        // Thực hiện giữ ghế với transaction và row locking chống tranh chấp
        String bookingId = bookingService.holdSeats(userId, showtimeId, seatList, customerName, email, phone);

        if (bookingId != null) {
            HttpSession curSession = request.getSession(true);
            curSession.setAttribute("currentBookingId", bookingId);
            curSession.setAttribute("currentShowtimeId", showtimeId);
            @SuppressWarnings("unchecked")
            Set<String> sessionBookings = (Set<String>) curSession.getAttribute("sessionBookings");
            if (sessionBookings == null) {
                sessionBookings = new HashSet<>();
                curSession.setAttribute("sessionBookings", sessionBookings);
            }
            sessionBookings.add(bookingId);

            response.sendRedirect(request.getContextPath() + "/booking/combos?bookingId=" + bookingId);
        } else {
            response.sendRedirect(request.getContextPath() + "/booking/seats?showtimeId=" + showtimeId + "&error=occupied");
        }
    }

    /**
     * GET /booking/combos
     * Nạp combos, bookingId, rawTotal và forward sang combo-selection.jsp
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

        Map<String, Object> booking = bookingService.getBookingDetails(bookingId.trim());
        if (booking == null || booking.isEmpty() || !isBookingOwnedByCurrentSession(request, bookingId.trim(), booking)) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        String status = (String) booking.get("bookingStatus");
        if ("PAID".equalsIgnoreCase(status)) {
            response.sendRedirect(request.getContextPath() + "/booking/success?bookingId=" + bookingId.trim());
            return;
        }

        List<Combo> comboList = bookingService.getAllCombos();
        BigDecimal rawTotal = bookingService.getRawTotal(bookingId.trim());

        request.setAttribute("combos", comboList);
        request.setAttribute("comboList", comboList);
        request.setAttribute("bookingId", bookingId.trim());
        request.setAttribute("booking", booking);
        request.setAttribute("rawTotal", rawTotal);
        request.setAttribute("showtimeId", booking.get("showtimeId"));

        request.getRequestDispatcher("/WEB-INF/views/booking/combo-selection.jsp").forward(request, response);
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

        Map<String, Object> booking = bookingService.getBookingDetails(bookingId.trim());
        if (booking == null || booking.isEmpty() || !isBookingOwnedByCurrentSession(request, bookingId.trim(), booking)) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        String status = (String) booking.get("bookingStatus");
        if ("PAID".equalsIgnoreCase(status)) {
            response.sendRedirect(request.getContextPath() + "/booking/success?bookingId=" + bookingId.trim());
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
     * Tính tổng tiền, nạp số điểm khả dụng và forward sang checkout.jsp
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

        Map<String, Object> bookingDetails = bookingService.getBookingDetails(bookingId.trim());
        if (bookingDetails == null || bookingDetails.isEmpty() || !isBookingOwnedByCurrentSession(request, bookingId.trim(), bookingDetails)) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        String status = (String) bookingDetails.get("bookingStatus");
        if ("PAID".equalsIgnoreCase(status)) {
            response.sendRedirect(request.getContextPath() + "/booking/success?bookingId=" + bookingId.trim());
            return;
        }

        String userId = resolveUserId(request);
        if ((userId == null || userId.isBlank()) && bookingDetails.get("userId") != null) {
            userId = String.valueOf(bookingDetails.get("userId"));
        }

        int availablePoints = 0;
        if (userId != null && !userId.isBlank()) {
            try {
                availablePoints = loyaltyService.getPoints(userId.trim());
            } catch (Exception ignored) {
            }
        }

        BigDecimal rawTotal = bookingService.getRawTotal(bookingId.trim());
        // Điểm số tối đa áp dụng: điểm * 10.000 <= 90% * tổng tiền (loại bỏ bẫy tiền thừa < 5.000đ)
        int maxPointsForBill = (int) Math.floor((rawTotal.doubleValue() * 0.9) / LoyaltyService.POINT_VALUE_VND);
        while (maxPointsForBill > 0 && (rawTotal.doubleValue() - (maxPointsForBill * LoyaltyService.POINT_VALUE_VND)) < 5000.0) {
            maxPointsForBill--;
        }
        int maxUsablePoints = Math.min(availablePoints, maxPointsForBill);

        int pointsUsed = 0;
        String pointsStr = request.getParameter("pointsUsed");
        if (pointsStr != null && !pointsStr.trim().isEmpty()) {
            try {
                pointsUsed = Math.max(0, Integer.parseInt(pointsStr.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        // Giới hạn không quá số điểm khả dụng và không quá 90% giá trị đơn hàng
        if (pointsUsed > maxUsablePoints) {
            pointsUsed = maxUsablePoints;
        }

        BigDecimal totalAmount = bookingService.calculateFinalAmount(bookingId.trim(), pointsUsed);

        request.setAttribute("bookingId", bookingId.trim());
        request.setAttribute("booking", bookingDetails);
        request.setAttribute("bookingDetails", bookingDetails);
        request.setAttribute("rawTotal", rawTotal);
        request.setAttribute("totalAmount", totalAmount);
        request.setAttribute("pointsUsed", pointsUsed);
        request.setAttribute("availablePoints", availablePoints);
        request.setAttribute("maxUsablePoints", maxUsablePoints);

        String error = request.getParameter("error");
        if (error != null) {
            request.setAttribute("error", error);
        }

        request.getRequestDispatcher("/WEB-INF/views/booking/checkout.jsp").forward(request, response);
    }

    /**
     * POST /booking/pay
     * Nhận paymentType, pointsUsed, contact info.
     * Hỗ trợ thanh toán VNPay hoặc Tiền mặt/Ví điện tử.
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

        Map<String, Object> bookingDetails = bookingService.getBookingDetails(bookingId.trim());
        if (bookingDetails == null || bookingDetails.isEmpty() || !isBookingOwnedByCurrentSession(request, bookingId.trim(), bookingDetails)) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        String status = (String) bookingDetails.get("bookingStatus");
        if ("PAID".equalsIgnoreCase(status)) {
            response.sendRedirect(request.getContextPath() + "/booking/success?bookingId=" + bookingId.trim());
            return;
        }

        String contactName = request.getParameter("contactName");
        String contactPhone = request.getParameter("contactPhone");
        String contactEmail = request.getParameter("contactEmail");

        String userId = resolveUserId(request);
        if (userId == null || userId.isBlank()) {
            userId = (String) bookingDetails.get("userId");
        }

        int availablePoints = 0;
        if (userId != null && !userId.isBlank()) {
            try {
                availablePoints = loyaltyService.getPoints(userId.trim());
            } catch (Exception ignored) {
            }
        }

        int pointsUsed = 0;
        String pointsStr = request.getParameter("pointsUsed");
        if (pointsStr != null && !pointsStr.trim().isEmpty()) {
            try {
                pointsUsed = Math.max(0, Integer.parseInt(pointsStr.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        // Giới hạn điểm hợp lệ
        if (pointsUsed > availablePoints) {
            pointsUsed = availablePoints;
        }

        BigDecimal rawTotal = bookingService.getRawTotal(bookingId.trim());
        // Điểm số tối đa áp dụng: điểm * 10.000 <= 90% * tổng tiền (loại bỏ bẫy tiền thừa < 5.000đ)
        int maxPointsForOrder = (int) Math.floor((rawTotal.doubleValue() * 0.9) / LoyaltyService.POINT_VALUE_VND);
        while (maxPointsForOrder > 0 && (rawTotal.doubleValue() - (maxPointsForOrder * LoyaltyService.POINT_VALUE_VND)) < 5000.0) {
            maxPointsForOrder--;
        }
        int maxAllowedPoints = Math.min(availablePoints, maxPointsForOrder);
        if (pointsUsed > maxAllowedPoints) {
            pointsUsed = maxAllowedPoints;
        }

        BigDecimal finalAmount = bookingService.calculateFinalAmount(bookingId.trim(), pointsUsed);

        // Lưu trước điểm dùng và thông tin khách hàng vào đơn PENDING
        bookingService.updatePendingBooking(bookingId.trim(), pointsUsed, finalAmount, contactName, contactEmail, contactPhone);

        // 1. Nếu số tiền sau khi dùng điểm = 0 (khách dùng điểm thanh toán 100% đơn hàng)
        if (finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            boolean success = bookingService.processPayment(bookingId.trim(), "POINTS", pointsUsed,
                    userId, contactName, contactEmail, contactPhone);
            if (success) {
                // Cập nhật lại số điểm mới trong session loginUser
                if (userId != null && !userId.isBlank()) {
                    try {
                        int remainingPoints = loyaltyService.getPoints(userId.trim());
                        HttpSession session = request.getSession(false);
                        if (session != null) {
                            Object loginUser = session.getAttribute("loginUser");
                            if (loginUser != null) {
                                LoginUserSupport.setLoyaltyPoints(loginUser, remainingPoints);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
                response.sendRedirect(request.getContextPath() + "/booking/success?bookingId=" + bookingId.trim());
            } else {
                response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId.trim() + "&error=failed");
            }
            return;
        }

        // 2. Kiểm tra nếu số tiền > 0 nhưng < 5000 (VNPay yêu cầu tối thiểu 5.000 VNĐ)
        if (finalAmount.compareTo(BigDecimal.valueOf(5000)) < 0) {
            response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId.trim() + "&error=vnpay_min_amount");
            return;
        }

        // 3. Chuyển hướng sang cổng thanh toán VNPAY Sandbox
        long amountVnd = finalAmount.longValue();
        String vnpayUrl = bookingService.createVNPayUrl(request, bookingId.trim(), amountVnd);
        response.sendRedirect(vnpayUrl);
    }

    /**
     * GET /booking/vnpay-return
     * Nhận phản hồi IPN / Callback từ cổng thanh toán VNPay:
     * 1. Xác thực chữ ký HMAC-SHA512 qua hashData
     * 2. Kiểm tra mã phản hồi vnp_ResponseCode = 00
     * 3. Kiểm tra số tiền thanh toán khớp với giá trị đơn hàng trong DB
     * 4. Đổi trạng thái sang PAID, trừ điểm và cộng điểm tích lũy
     */
    private void handleVNPayReturn(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, String> fields = new HashMap<>();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                fields.put(name, values[0]);
            }
        }

        String vnp_SecureHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        // 1. Kiểm tra chữ ký bảo mật HMAC-SHA512
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        try {
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = fields.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[BookingServlet] Lỗi build hash data: " + e.getMessage());
        }

        String signValue = VNPayConfig.hmacSHA512(VNPayConfig.VNP_HASH_SECRET, hashData.toString());

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

        // Xác thực chữ ký số VNPay
        if (vnp_SecureHash == null || !signValue.equalsIgnoreCase(vnp_SecureHash)) {
            System.err.println("[BookingServlet] VNPay checksum không hợp lệ! Computed: " + signValue + ", Received: " + vnp_SecureHash);
            response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId + "&error=vnpay_checksum_failed");
            return;
        }

        // Kiểm tra mã phản hồi VNPay: "00" là thành công
        if (!"00".equals(responseCode)) {
            response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId + "&error=vnpay_cancelled");
            return;
        }

        // Kiểm tra thông tin đơn hàng trong database
        Map<String, Object> bookingDetails = bookingService.getBookingDetails(bookingId);
        if (bookingDetails == null) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        String status = (String) bookingDetails.get("status");
        if ("PAID".equalsIgnoreCase(status)) {
            // Đơn đã thanh toán thành công trước đó (Idempotency)
            response.sendRedirect(request.getContextPath() + "/booking/success?bookingId=" + bookingId);
            return;
        }

        // 3. Đối chiếu số tiền thanh toán thực tế với DB
        BigDecimal amount = null;
        if (vnpAmount != null && !vnpAmount.trim().isEmpty()) {
            try {
                amount = new BigDecimal(vnpAmount.trim()).divide(new BigDecimal(100));
            } catch (Exception ignored) {
            }
        }
        if (amount == null) {
            amount = BigDecimal.valueOf(bookingDetails.get("totalPrice") != null ? (Double) bookingDetails.get("totalPrice") : 0.0);
        }

        double dbTotal = bookingDetails.get("totalPrice") != null ? (Double) bookingDetails.get("totalPrice") : 0.0;
        if (Math.abs(dbTotal - amount.doubleValue()) > 1.0) {
            System.err.println("[BookingServlet] Số tiền VNPay (" + amount + ") không khớp với đơn hàng (" + dbTotal + ")");
            response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId + "&error=amount_mismatch");
            return;
        }

        String userId = resolveUserId(request);
        if (userId == null && bookingDetails.get("userId") != null) {
            userId = String.valueOf(bookingDetails.get("userId"));
        }

        boolean success = bookingService.completePaymentVNPay(bookingId, transactionNo, amount, userId, null, null, null);
        if (success) {
            // Đồng bộ điểm thưởng mới vào session loginUser
            if (userId != null && !userId.isBlank()) {
                try {
                    int remainingPoints = loyaltyService.getPoints(userId.trim());
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        Object loginUser = session.getAttribute("loginUser");
                        if (loginUser != null) {
                            LoginUserSupport.setLoyaltyPoints(loginUser, remainingPoints);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            response.sendRedirect(request.getContextPath() + "/booking/success?bookingId=" + bookingId);
        } else {
            response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId + "&error=failed");
        }
    }

    /**
     * GET /booking/success
     * Nạp bookingId và thông tin đơn vé, forward sang booking-success.jsp
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

        Map<String, Object> bookingDetails = bookingService.getBookingDetails(bookingId.trim());
        if (bookingDetails == null || bookingDetails.isEmpty() || !isBookingOwnedByCurrentSession(request, bookingId.trim(), bookingDetails)) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        request.setAttribute("bookingId", bookingId.trim());
        request.setAttribute("booking", bookingDetails);
        request.setAttribute("bookingDetails", bookingDetails);

        request.getRequestDispatcher("/WEB-INF/views/booking/booking-success.jsp").forward(request, response);
    }

    /**
     * Kiểm tra xem đơn hàng có thuộc session hiện tại hoặc user đang đăng nhập hay không (chống IDOR).
     */
    private boolean isBookingOwnedByCurrentSession(HttpServletRequest request, String bookingId, Map<String, Object> bookingDetails) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            return false;
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            String currentBookingId = (String) session.getAttribute("currentBookingId");
            if (bookingId.trim().equalsIgnoreCase(currentBookingId)) {
                return true;
            }
            @SuppressWarnings("unchecked")
            Set<String> sessionBookings = (Set<String>) session.getAttribute("sessionBookings");
            if (sessionBookings != null && sessionBookings.contains(bookingId.trim())) {
                return true;
            }
        }
        String currentUserId = resolveUserId(request);
        if (currentUserId != null && !currentUserId.isBlank() && bookingDetails != null) {
            String bookingUserId = (String) bookingDetails.get("userId");
            if (currentUserId.equals(bookingUserId)) {
                return true;
            }
        }
        return false;
    }
}
