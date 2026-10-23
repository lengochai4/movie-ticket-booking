package com.cinema.controller.booking;

import com.cinema.entity.Combo;
import com.cinema.entity.Seat;
import com.cinema.order.LoginUserSupport;
import com.cinema.service.booking.BookingService;
import com.cinema.service.order.LoyaltyService;
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
    private LoyaltyService loyaltyService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.bookingService = new BookingService();
        this.loyaltyService = new LoyaltyService();
    }

    // =========================================================
    // GET REQUESTS: ĐIỀU HƯỚNG HIỂN THỊ CÁC BƯỚC ĐẶT VÉ
    // =========================================================
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

    // =========================================================
    // POST REQUESTS: TIẾP NHẬN DỮ LIỆU TỪ CÁC FORM
    // =========================================================
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

    // =========================================================
    // BƯỚC 1: XỬ LÝ CHỌN GHẾ VÀ GIỮ GHẾ
    // =========================================================

    /**
     * GET /booking/seats?showtimeId=...
     * Hiển thị sơ đồ ghế của suất chiếu lên giao diện seat-map.jsp
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
     * Nhận danh sách ghế đã chọn và giữ chỗ trong 10 phút.
     */
    private void handlePostHold(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Kiểm tra suất chiếu
        String showtimeId = request.getParameter("showtimeId");
        if (showtimeId == null || showtimeId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        // 2. Đọc danh sách ghế từ form
        List<String> seatList = new ArrayList<>();
        String[] seatValues = request.getParameterValues("selectedSeats");
        if (seatValues != null) {
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
            response.sendRedirect(request.getContextPath() + "/booking/seats?showtimeId=" + showtimeId + "&error=no_seats_selected");
            return;
        }

        // 3. Lấy thông tin người đặt vé từ session (nếu có)
        HttpSession session = request.getSession(false);
        Object userObj = session != null ? session.getAttribute("loginUser") : null;
        if (userObj == null && session != null) {
            userObj = session.getAttribute("user");
        }

        String userId = resolveUserId(request);
        String customerName = LoginUserSupport.getFullName(userObj);
        String email = LoginUserSupport.getEmail(userObj);
        String phone = LoginUserSupport.getPhone(userObj);

        // 4. Gọi Service thực hiện giữ ghế trong 10 phút
        String bookingId = bookingService.holdSeats(userId, showtimeId, seatList, customerName, email, phone);

        // 5. Chuyển hướng theo kết quả
        if (bookingId != null) {
            HttpSession curSession = request.getSession(true);
            curSession.setAttribute("currentBookingId", bookingId);
            curSession.setAttribute("currentShowtimeId", showtimeId);

            response.sendRedirect(request.getContextPath() + "/booking/combos?bookingId=" + bookingId);
        } else {
            response.sendRedirect(request.getContextPath() + "/booking/seats?showtimeId=" + showtimeId + "&error=occupied");
        }
    }

    // =========================================================
    // BƯỚC 2: XỬ LÝ CHỌN BẮP NƯỚC (COMBO)
    // =========================================================

    /**
     * GET /booking/combos?bookingId=...
     * Hiển thị danh sách bắp nước lên giao diện combo-selection.jsp
     */
    private void handleGetCombos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Chống bypass: Đảm bảo có đơn PENDING hợp lệ
        Map<String, Object> booking = checkPendingBooking(request, response);
        if (booking == null) return;

        String bookingId = (String) booking.get("bookingId");

        // 2. Lấy danh sách combo và tạm tính tiền
        List<Combo> comboList = bookingService.getAllCombos();
        BigDecimal rawTotal = bookingService.getRawTotal(bookingId);

        // 3. Nạp dữ liệu sang combo-selection.jsp
        request.setAttribute("combos", comboList);
        request.setAttribute("comboList", comboList);
        request.setAttribute("bookingId", bookingId);
        request.setAttribute("booking", booking);
        request.setAttribute("rawTotal", rawTotal);
        request.setAttribute("showtimeId", booking.get("showtimeId"));

        request.getRequestDispatcher("/WEB-INF/views/booking/combo-selection.jsp").forward(request, response);
    }

    /**
     * POST /booking/saveCombos
     * Lưu danh sách combo khách đã chọn vào đơn hàng
     */
    private void handlePostSaveCombos(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Chống bypass
        Map<String, Object> booking = checkPendingBooking(request, response);
        if (booking == null) return;

        String bookingId = (String) booking.get("bookingId");

        // 2. Đọc các param bắt đầu bằng combo_
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
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // 3. Cập nhật combo và chuyển sang bước Checkout
        bookingService.saveCombos(bookingId, comboQuantities);
        response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId);
    }

    // =========================================================
    // BƯỚC 3: XỬ LÝ THANH TOÁN (CHECKOUT & PAY)
    // =========================================================

    /**
     * GET /booking/checkout?bookingId=...
     * Hiển thị trang xác nhận đơn hàng, điểm tích lũy và cổng thanh toán
     */
    private void handleGetCheckout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Chống bypass
        Map<String, Object> bookingDetails = checkPendingBooking(request, response);
        if (bookingDetails == null) return;

        String bookingId = (String) bookingDetails.get("bookingId");
        String userId = resolveUserId(request);
        if ((userId == null || userId.isBlank()) && bookingDetails.get("userId") != null) {
            userId = String.valueOf(bookingDetails.get("userId"));
        }

        // 2. Lấy số điểm hiện có của khách
        int availablePoints = 0;
        if (userId != null && !userId.isBlank()) {
            try {
                availablePoints = loyaltyService.getPoints(userId.trim());
            } catch (Exception ignored) {}
        }

        // 3. Tính toán số tiền và số điểm tối đa được dùng qua BookingService
        BigDecimal rawTotal = bookingService.getRawTotal(bookingId);
        int maxUsablePoints = bookingService.calculateMaxUsablePoints(rawTotal, availablePoints);

        int pointsUsed = 0;
        String pointsStr = request.getParameter("pointsUsed");
        if (pointsStr != null && !pointsStr.trim().isEmpty()) {
            try {
                pointsUsed = Math.max(0, Integer.parseInt(pointsStr.trim()));
            } catch (NumberFormatException ignored) {}
        }
        if (pointsUsed > maxUsablePoints) {
            pointsUsed = maxUsablePoints;
        }

        BigDecimal totalAmount = bookingService.calculateFinalAmount(bookingId, pointsUsed);

        // 4. Đẩy toàn bộ dữ liệu ra checkout.jsp
        request.setAttribute("bookingId", bookingId);
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
     * Nhận thông tin liên hệ và hình thức thanh toán (VNPay hoặc 100% Điểm)
     */
    private void handlePostPay(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Chống bypass
        Map<String, Object> bookingDetails = checkPendingBooking(request, response);
        if (bookingDetails == null) return;

        String bookingId = (String) bookingDetails.get("bookingId");
        String contactName = request.getParameter("contactName");
        String contactPhone = request.getParameter("contactPhone");
        String contactEmail = request.getParameter("contactEmail");

        String userId = resolveUserId(request);
        if (userId == null || userId.isBlank()) {
            userId = (String) bookingDetails.get("userId");
        }

        // 2. Tính số điểm dùng hợp lệ
        int availablePoints = 0;
        if (userId != null && !userId.isBlank()) {
            try {
                availablePoints = loyaltyService.getPoints(userId.trim());
            } catch (Exception ignored) {}
        }

        BigDecimal rawTotal = bookingService.getRawTotal(bookingId);
        int maxUsablePoints = bookingService.calculateMaxUsablePoints(rawTotal, availablePoints);

        int pointsUsed = 0;
        String pointsStr = request.getParameter("pointsUsed");
        if (pointsStr != null && !pointsStr.trim().isEmpty()) {
            try {
                pointsUsed = Math.max(0, Integer.parseInt(pointsStr.trim()));
            } catch (NumberFormatException ignored) {}
        }
        if (pointsUsed > maxUsablePoints) {
            pointsUsed = maxUsablePoints;
        }

        BigDecimal finalAmount = bookingService.calculateFinalAmount(bookingId, pointsUsed);

        // 3. Cập nhật thông tin nhận vé và điểm dùng vào đơn PENDING
        bookingService.updatePendingBooking(bookingId, pointsUsed, finalAmount, contactName, contactEmail, contactPhone);

        // NHÁNH A: Dùng điểm thanh toán 100% đơn hàng (tiền phải trả = 0)
        if (finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            boolean success = bookingService.processPayment(bookingId, "POINTS", pointsUsed, userId, contactName, contactEmail, contactPhone);
            if (success) {
                syncUserSessionPoints(request, userId);
                response.sendRedirect(request.getContextPath() + "/booking/success?bookingId=" + bookingId);
            } else {
                response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId + "&error=failed");
            }
            return;
        }

        // NHÁNH B: Bẫy kiểm tra số tiền tối thiểu của cổng VNPay (tối thiểu 5.000 VNĐ)
        if (finalAmount.compareTo(BigDecimal.valueOf(5000)) < 0) {
            response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId + "&error=vnpay_min_amount");
            return;
        }

        // NHÁNH C: Tạo URL và chuyển hướng sang cổng thanh toán VNPAY Sandbox
        long amountVnd = finalAmount.longValue();
        String vnpayUrl = bookingService.createVNPayUrl(request, bookingId, amountVnd);
        response.sendRedirect(vnpayUrl);
    }

    // =========================================================
    // BƯỚC 4: XỬ LÝ KẾT QUẢ TRẢ VỀ TỪ VNPAY
    // =========================================================

    /**
     * GET /booking/vnpay-return
     * Nhận phản hồi IPN / Callback từ cổng thanh toán VNPay
     */
    private void handleVNPayReturn(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Đọc toàn bộ tham số do VNPay gửi về
        Map<String, String> fields = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if (entry.getValue() != null && entry.getValue().length > 0) {
                fields.put(entry.getKey(), entry.getValue()[0]);
            }
        }

        String vnp_SecureHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        String responseCode = request.getParameter("vnp_ResponseCode");
        String txnRef = request.getParameter("vnp_TxnRef");
        String transactionNo = request.getParameter("vnp_TransactionNo");
        String vnpAmount = request.getParameter("vnp_Amount");

        // Tách lấy bookingId từ txnRef (dạng BK_xxxxxxxx-xxxx)
        String bookingId = null;
        if (txnRef != null && !txnRef.trim().isEmpty()) {
            bookingId = txnRef.contains("-") ? txnRef.split("-")[0] : txnRef;
        }
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

        // 2. Xác thực chữ ký số VNPay (chống giả mạo URL)
        if (!bookingService.verifyVNPaySignature(fields, vnp_SecureHash)) {
            response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId + "&error=vnpay_checksum_failed");
            return;
        }

        // 3. Kiểm tra mã phản hồi: "00" là khách đã chuyển tiền thành công
        if (!"00".equals(responseCode)) {
            response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId + "&error=vnpay_cancelled");
            return;
        }

        // 4. Đối chiếu thông tin đơn hàng trong CSDL
        Map<String, Object> bookingDetails = bookingService.getBookingDetails(bookingId);
        if (bookingDetails == null) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        // Idempotency: Nếu đơn đã PAID trước đó thì sang trang success luôn
        String status = (String) bookingDetails.get("status");
        if ("PAID".equalsIgnoreCase(status)) {
            response.sendRedirect(request.getContextPath() + "/booking/success?bookingId=" + bookingId);
            return;
        }

        // 5. Đối chiếu số tiền thanh toán thực tế với DB (VNPay nhân 100 lần số tiền)
        BigDecimal amount = null;
        if (vnpAmount != null && !vnpAmount.trim().isEmpty()) {
            try {
                amount = new BigDecimal(vnpAmount.trim()).divide(new BigDecimal(100));
            } catch (Exception ignored) {}
        }
        double dbTotal = bookingDetails.get("totalPrice") != null ? (Double) bookingDetails.get("totalPrice") : 0.0;
        if (amount != null && Math.abs(dbTotal - amount.doubleValue()) > 1.0) {
            response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId + "&error=amount_mismatch");
            return;
        }

        // 6. Cập nhật trạng thái PAID trong DB và cộng điểm tích lũy
        String userId = resolveUserId(request);
        if (userId == null && bookingDetails.get("userId") != null) {
            userId = String.valueOf(bookingDetails.get("userId"));
        }

        boolean success = bookingService.completePaymentVNPay(bookingId, transactionNo, amount, userId, null, null, null);
        if (success) {
            syncUserSessionPoints(request, userId);
            response.sendRedirect(request.getContextPath() + "/booking/success?bookingId=" + bookingId);
        } else {
            response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId + "&error=failed");
        }
    }

    // =========================================================
    // BƯỚC 5: XUẤT VÉ ĐIỆN TỬ THÀNH CÔNG (SUCCESS)
    // =========================================================

    /**
     * GET /booking/success?bookingId=...
     * Hiển thị vé điện tử và mã QR check-in
     */
    private void handleGetSuccess(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Chống bypass: Chỉ đơn hàng đã PAID thực sự mới được mở trang này
        Map<String, Object> bookingDetails = checkPaidBooking(request, response);
        if (bookingDetails == null) return;

        String bookingId = (String) bookingDetails.get("bookingId");

        // 2. Nạp dữ liệu sang booking-success.jsp
        request.setAttribute("bookingId", bookingId);
        request.setAttribute("booking", bookingDetails);
        request.setAttribute("bookingDetails", bookingDetails);

        request.getRequestDispatcher("/WEB-INF/views/booking/booking-success.jsp").forward(request, response);
    }

    // =========================================================
    // CÁC HÀM HỖ TRỢ (HELPER & GUARD CHỐNG BYPASS)
    // =========================================================

    /**
     * Chốt bảo vệ (Guard): Kiểm tra xem người dùng có đơn hàng PENDING hợp lệ hay không.
     * Chống nhảy cóc qua URL vào các bước combos, checkout, pay.
     */
    private Map<String, Object> checkPendingBooking(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        String currentBookingId = (session != null) ? (String) session.getAttribute("currentBookingId") : null;
        String paramBookingId = request.getParameter("bookingId");

        String bookingId = (paramBookingId != null && !paramBookingId.trim().isEmpty())
                ? paramBookingId.trim()
                : currentBookingId;

        // Nếu không có mã booking -> đá về trang chủ phim
        if (bookingId == null || bookingId.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return null;
        }

        // Tìm đơn trong CSDL
        Map<String, Object> booking = bookingService.getBookingDetails(bookingId);
        if (booking == null || booking.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return null;
        }

        // Nếu đơn ĐÃ THANH TOÁN RỒI mà cố tình vào lại -> đá sang trang vé thành công
        String status = (String) booking.get("status");
        if (status == null) status = (String) booking.get("bookingStatus");
        if ("PAID".equalsIgnoreCase(status)) {
            response.sendRedirect(request.getContextPath() + "/booking/success?bookingId=" + bookingId);
            return null;
        }

        return booking;
    }

    /**
     * Chốt bảo vệ (Guard): Đảm bảo chỉ đơn hàng đã PAID thực sự mới được xem vé.
     * Khách cố tình gõ link /booking/success khơi khơi sẽ bị đá ngược lại.
     */
    private Map<String, Object> checkPaidBooking(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        String currentBookingId = (session != null) ? (String) session.getAttribute("currentBookingId") : null;
        String paramBookingId = request.getParameter("bookingId");

        String bookingId = (paramBookingId != null && !paramBookingId.trim().isEmpty())
                ? paramBookingId.trim()
                : currentBookingId;

        if (bookingId == null || bookingId.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return null;
        }

        Map<String, Object> booking = bookingService.getBookingDetails(bookingId);
        if (booking == null || booking.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return null;
        }

        // Chưa thanh toán (PENDING) mà đòi xem vé -> đá về checkout
        String status = (String) booking.get("status");
        if (status == null) status = (String) booking.get("bookingStatus");
        if (!"PAID".equalsIgnoreCase(status)) {
            response.sendRedirect(request.getContextPath() + "/booking/checkout?bookingId=" + bookingId);
            return null;
        }

        return booking;
    }

    /**
     * Lấy ID của người dùng đăng nhập trong session (hỗ trợ cả đối tượng User lẫn wrapper)
     */
    private String resolveUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object userObj = session.getAttribute("loginUser");
        if (userObj == null) userObj = session.getAttribute("user");
        if (userObj instanceof com.cinema.entity.User) {
            return ((com.cinema.entity.User) userObj).getUserID();
        }
        return LoginUserSupport.getUserId(userObj);
    }

    /**
     * Đồng bộ lại số điểm thưởng mới nhất vào session người dùng
     */
    private void syncUserSessionPoints(HttpServletRequest request, String userId) {
        if (userId == null || userId.isBlank()) return;
        try {
            int remainingPoints = loyaltyService.getPoints(userId.trim());
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object loginUser = session.getAttribute("loginUser");
                if (loginUser != null) {
                    LoginUserSupport.setLoyaltyPoints(loginUser, remainingPoints);
                }
            }
        } catch (Exception ignored) {}
    }
}
