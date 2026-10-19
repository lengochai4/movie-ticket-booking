package com.cinema.service.booking;

import com.cinema.dao.booking.BookingDAO;
import com.cinema.model.entity.Combo;
import com.cinema.model.entity.Payment;
import com.cinema.model.entity.Seat;
import com.cinema.util.VNPayConfig;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

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
     * Lấy toàn bộ danh sách Combo bắp nước từ bảng "Combo".
     */
    public List<Combo> getAllCombos() {
        return bookingDAO.getAllCombos();
    }

    /**
     * Lưu danh sách combo bắp nước cho đơn đặt vé vào "BookingCombo".
     */
    public void saveCombos(String bookingID, Map<String, Integer> comboQuantities) {
        if (bookingID != null && !bookingID.trim().isEmpty() && comboQuantities != null) {
            bookingDAO.saveBookingCombos(bookingID.trim(), comboQuantities);
        }
    }

    /**
     * Tính tổng tiền thanh toán sau khi trừ điểm tích lũy:
     * Tổng thô (vé + combo) - (pointsToUse * 1.000). Nếu kết quả < 0 thì trả về 0.
     */
    public BigDecimal calculateFinalAmount(String bookingID, int pointsToUse) {
        if (bookingID == null || bookingID.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal rawTotal = bookingDAO.calculateRawTotal(bookingID.trim());
        int safePoints = Math.max(0, pointsToUse);
        BigDecimal discount = BigDecimal.valueOf(safePoints * 1000L);

        BigDecimal finalAmount = rawTotal.subtract(discount);
        return (finalAmount.compareTo(BigDecimal.ZERO) < 0) ? BigDecimal.ZERO : finalAmount;
    }

    /**
     * Xử lý giao dịch thanh toán trực tiếp (Tiền mặt / Mock):
     * - Sinh mã paymentID: PAY_ + UUID
     * - Tính số tiền thanh toán thực tế (sau giảm trừ điểm)
     * - Tạo thực thể Payment
     * - Gọi DAO completePayment để cập nhật Booking sang 'PAID' và chuyển vé sang 'AVAILABLE'
     */
    public boolean processPayment(String bookingID, String paymentType, int pointsToUse) {
        if (bookingID == null || bookingID.trim().isEmpty()) {
            return false;
        }

        String paymentID = "PAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        BigDecimal finalAmount = calculateFinalAmount(bookingID.trim(), pointsToUse);
        String resolvedType = (paymentType != null && !paymentType.trim().isEmpty())
                ? paymentType.trim().toUpperCase()
                : "CASH";

        Payment payment = new Payment(paymentID, resolvedType, finalAmount, LocalDateTime.now());
        return bookingDAO.completePayment(bookingID.trim(), payment);
    }

    /**
     * Tạo URL chuyển hướng sang cổng thanh toán VNPay Sandbox:
     * - Thuật toán chữ ký HMAC-SHA512
     * - Sắp xếp key theo alphabet và hash dữ liệu chuẩn VNPay 2.1.0
     */
    public String createVNPayUrl(HttpServletRequest req, String bookingId, long amountVnd) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderType = "other";
        String vnp_Locale = "vn";
        String vnp_CurrCode = "VND";
        String vnp_BankCode = "NCB";

        String vnp_TxnRef = bookingId + "-" + VNPayConfig.getRandomNumber(4);
        String vnp_OrderInfo = "Thanh toan ve xem phim: " + bookingId;
        String vnp_ReturnUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath() + "/booking/vnpay-return";
        String vnp_IpAddr = VNPayConfig.getIpAddress(req);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", VNPayConfig.VNP_TMN_CODE);
        vnp_Params.put("vnp_Amount", String.valueOf(amountVnd * 100)); // VNPay quy định nhân 100
        vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
        vnp_Params.put("vnp_BankCode", vnp_BankCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", vnp_Locale);
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        TimeZone tz = TimeZone.getTimeZone("GMT+7");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(tz);

        Calendar cld = Calendar.getInstance(tz);
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        try {
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[BookingService] Lỗi encode URL VNPay: " + e.getMessage());
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.VNP_HASH_SECRET, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return VNPayConfig.VNP_PAY_URL + "?" + queryUrl;
    }

    /**
     * Xác nhận thanh toán qua VNPay:
     * - Tạo đối tượng Payment (paymentID = "PAY_" + transactionNo, type = "VNPAY", amount, now)
     * - Gọi BookingDAO.completePayment(...) để đổi BookingStatus sang PAID và TicketStatus sang AVAILABLE
     */
    public boolean completePaymentVNPay(String bookingId, String transactionNo, BigDecimal amount) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            return false;
        }

        String paymentId = "PAY_" + ((transactionNo != null && !transactionNo.trim().isEmpty())
                ? transactionNo.trim()
                : UUID.randomUUID().toString().replace("-", "").substring(0, 10));

        Payment payment = new Payment(
                paymentId,
                "VNPAY",
                amount != null ? amount : BigDecimal.ZERO,
                LocalDateTime.now()
        );

        return bookingDAO.completePayment(bookingId.trim(), payment);
    }

    /**
     * Xác nhận thanh toán thành công cho đơn đặt vé (fallback).
     */
    public boolean confirmPayment(String bookingID, String paymentID) {
        if (bookingID == null || bookingID.trim().isEmpty()) {
            return false;
        }
        return bookingDAO.confirmPayment(bookingID.trim(), paymentID != null ? paymentID : "PAY_ONLINE");
    }
}
