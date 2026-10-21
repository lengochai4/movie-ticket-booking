package com.cinema.service.booking;

import com.cinema.dao.booking.BookingDAO;
import com.cinema.entity.Combo;
import com.cinema.entity.Payment;
import com.cinema.entity.Seat;
import com.cinema.service.order.LoyaltyService;
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
    private final LoyaltyService loyaltyService;

    public BookingService() {
        this.bookingDAO = new BookingDAO();
        this.loyaltyService = new LoyaltyService();
    }

    public BookingService(BookingDAO bookingDAO) {
        this.bookingDAO = bookingDAO;
        this.loyaltyService = new LoyaltyService();
    }

    public BookingService(BookingDAO bookingDAO, LoyaltyService loyaltyService) {
        this.bookingDAO = bookingDAO;
        this.loyaltyService = loyaltyService != null ? loyaltyService : new LoyaltyService();
    }

    /**
     * Lấy danh sách toàn bộ ghế của suất chiếu kèm trạng thái khả dụng.
     */
    public List<Seat> getSeatMap(String showTimeID) {
        if (showTimeID == null || showTimeID.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return bookingDAO.getSeatsByShowtime(showTimeID.trim());
    }

    /**
     * Giữ ghế với mã booking duy nhất trong 10 phút kèm thông tin người đặt.
     * Trả về bookingID nếu thành công, null nếu thất bại hoặc trùng ghế.
     */
    public String holdSeats(String userID, String showTimeID, List<String> seatIDs,
                            String customerName, String email, String phone) {
        if (showTimeID == null || showTimeID.trim().isEmpty() || seatIDs == null || seatIDs.isEmpty()) {
            return null;
        }

        // Sinh mã đơn đặt vé duy nhất: BK_ + 8 ký tự viết hoa
        String bookingID = "BK_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        int holdMinutes = 10;

        boolean success = bookingDAO.holdSeats(bookingID, userID, showTimeID.trim(), seatIDs, holdMinutes,
                customerName, email, phone);
        return success ? bookingID : null;
    }

    public String holdSeats(String userID, String showTimeID, List<String> seatIDs) {
        return holdSeats(userID, showTimeID, seatIDs, null, null, null);
    }

    public String holdSeats(int userID, String showTimeID, List<String> seatIDs) {
        return holdSeats(String.valueOf(userID), showTimeID, seatIDs, null, null, null);
    }

    /**
     * Lấy toàn bộ danh sách Combo bắp nước đang mở bán.
     */
    public List<Combo> getAllCombos() {
        return bookingDAO.getAllCombos();
    }

    /**
     * Lưu danh sách combo bắp nước cho đơn đặt vé vào "bookings".
     */
    public void saveCombos(String bookingID, Map<String, Integer> comboQuantities) {
        if (bookingID != null && !bookingID.trim().isEmpty() && comboQuantities != null) {
            bookingDAO.saveBookingCombos(bookingID.trim(), comboQuantities);
        }
    }

    /**
     * Cập nhật thông tin giảm trừ điểm và tổng tiền cuối cùng cho đơn hàng PENDING.
     */
    public boolean updatePendingBooking(String bookingID, int pointsUsed, BigDecimal finalAmount,
                                        String customerName, String email, String phone) {
        if (bookingID == null || bookingID.trim().isEmpty()) {
            return false;
        }
        return bookingDAO.updatePendingBooking(bookingID.trim(), pointsUsed, finalAmount, customerName, email, phone);
    }

    /**
     * Lấy tổng tiền tạm tính gốc (vé + bắp nước) từ đơn hàng.
     */
    public BigDecimal getRawTotal(String bookingID) {
        if (bookingID == null || bookingID.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return bookingDAO.calculateRawTotal(bookingID.trim());
    }

    /**
     * Lấy chi tiết thông tin đơn hàng để kiểm tra tính hợp lệ và hiển thị.
     */
    public Map<String, Object> getBookingDetails(String bookingID) {
        if (bookingID == null || bookingID.trim().isEmpty()) {
            return null;
        }
        return bookingDAO.getBookingDetails(bookingID.trim());
    }

    /**
     * Tính tổng tiền thanh toán sau khi trừ điểm tích lũy:
     * Tổng thô (vé + combo) - (pointsToUse * 10.000đ). Nếu kết quả < 0 thì trả về 0.
     */
    public BigDecimal calculateFinalAmount(String bookingID, int pointsToUse) {
        if (bookingID == null || bookingID.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal rawTotal = bookingDAO.calculateRawTotal(bookingID.trim());
        int safePoints = Math.max(0, pointsToUse);

        // Điểm số tối đa áp dụng: điểm * 10.000 <= 90% * tổng tiền (loại bỏ bẫy tiền thừa < 5.000đ)
        int maxPointsAllowed = (int) Math.floor((rawTotal.doubleValue() * 0.9) / LoyaltyService.POINT_VALUE_VND);
        while (maxPointsAllowed > 0 && (rawTotal.doubleValue() - (maxPointsAllowed * LoyaltyService.POINT_VALUE_VND)) < 5000.0) {
            maxPointsAllowed--;
        }
        if (safePoints > maxPointsAllowed) {
            safePoints = maxPointsAllowed;
        }

        BigDecimal discount = BigDecimal.valueOf(safePoints * (long) LoyaltyService.POINT_VALUE_VND);
        BigDecimal finalAmount = rawTotal.subtract(discount);
        return (finalAmount.compareTo(BigDecimal.ZERO) < 0) ? BigDecimal.ZERO : finalAmount;
    }

    /**
     * Xử lý giao dịch thanh toán trực tiếp (Tiền mặt / Mock):
     * - Sinh mã paymentID: PAY_ + UUID
     * - Tính số tiền thanh toán thực tế (sau giảm trừ điểm)
     * - Tạo thực thể Payment
     * - Trừ điểm tích lũy (nếu dùng) và tích điểm mới
     * - Cập nhật Booking sang 'PAID' và chuyển vé sang 'AVAILABLE'
     */
    public boolean processPayment(String bookingID, String paymentType, int pointsToUse,
                                  String userId, String customerName, String email, String phone) {
        if (bookingID == null || bookingID.trim().isEmpty()) {
            return false;
        }

        Map<String, Object> bookingDetails = bookingDAO.getBookingDetails(bookingID.trim());
        String resolvedUserId = userId;
        if ((resolvedUserId == null || resolvedUserId.isBlank()) && bookingDetails != null && bookingDetails.get("userId") != null) {
            resolvedUserId = String.valueOf(bookingDetails.get("userId"));
        }

        BigDecimal rawTotal = bookingDAO.calculateRawTotal(bookingID.trim());
        int maxPointsAllowed = (int) Math.floor((rawTotal.doubleValue() * 0.9) / LoyaltyService.POINT_VALUE_VND);
        while (maxPointsAllowed > 0 && (rawTotal.doubleValue() - (maxPointsAllowed * LoyaltyService.POINT_VALUE_VND)) < 5000.0) {
            maxPointsAllowed--;
        }
        int effectivePoints = Math.max(0, pointsToUse);
        if (effectivePoints > maxPointsAllowed) {
            effectivePoints = maxPointsAllowed;
        }

        String paymentID = "PAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        BigDecimal finalAmount = calculateFinalAmount(bookingID.trim(), effectivePoints);
        String resolvedType = (paymentType != null && !paymentType.trim().isEmpty())
                ? paymentType.trim().toUpperCase()
                : "VNPAY";

        Payment payment = new Payment(paymentID, resolvedType, finalAmount, LocalDateTime.now());

        // Tính điểm thưởng được cộng (1 điểm mỗi 10.000 VNĐ chi tiêu)
        int earnedPoints = (int) Math.floor(finalAmount.doubleValue() / 10000.0);

        boolean success = bookingDAO.completePayment(bookingID.trim(), payment, effectivePoints, earnedPoints,
                finalAmount, customerName, email, phone);

        if (success && resolvedUserId != null && !resolvedUserId.isBlank()) {
            // Trừ điểm đã sử dụng trong loyalty_accounts
            if (effectivePoints > 0) {
                try {
                    loyaltyService.usePoints(resolvedUserId.trim(), effectivePoints);
                } catch (Exception e) {
                    System.err.println("[BookingService] Lỗi trừ điểm loyalty: " + e.getMessage());
                }
            }
            // Cộng điểm thưởng mới
            if (earnedPoints > 0) {
                try {
                    loyaltyService.addPoints(resolvedUserId.trim(), earnedPoints);
                } catch (Exception e) {
                    System.err.println("[BookingService] Lỗi cộng điểm loyalty: " + e.getMessage());
                }
            }
        }

        return success;
    }

    public boolean processPayment(String bookingID, String paymentType, int pointsToUse) {
        return processPayment(bookingID, paymentType, pointsToUse, null, null, null, null);
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

        cld.add(Calendar.MINUTE, 10);
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
     * - Trừ điểm và tích lũy điểm thưởng thành viên
     */
    public boolean completePaymentVNPay(String bookingId, String transactionNo, BigDecimal amount,
                                        String userId, String customerName, String email, String phone) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            return false;
        }

        Map<String, Object> bookingDetails = bookingDAO.getBookingDetails(bookingId.trim());
        int pointsUsed = 0;
        String resolvedUserId = userId;
        if (bookingDetails != null) {
            if (bookingDetails.get("pointsUsed") instanceof Number) {
                pointsUsed = ((Number) bookingDetails.get("pointsUsed")).intValue();
            }
            if ((resolvedUserId == null || resolvedUserId.isBlank()) && bookingDetails.get("userId") != null) {
                resolvedUserId = String.valueOf(bookingDetails.get("userId"));
            }
        }

        String paymentId = "PAY_" + ((transactionNo != null && !transactionNo.trim().isEmpty())
                ? transactionNo.trim()
                : UUID.randomUUID().toString().replace("-", "").substring(0, 10));

        BigDecimal resolvedAmount = amount != null ? amount : BigDecimal.ZERO;
        Payment payment = new Payment(
                paymentId,
                "VNPAY",
                resolvedAmount,
                LocalDateTime.now()
        );

        int earnedPoints = (int) Math.floor(resolvedAmount.doubleValue() / 10000.0);

        boolean success = bookingDAO.completePayment(bookingId.trim(), payment, pointsUsed, earnedPoints,
                resolvedAmount, customerName, email, phone);

        if (success && resolvedUserId != null && !resolvedUserId.isBlank()) {
            if (pointsUsed > 0) {
                try {
                    loyaltyService.usePoints(resolvedUserId.trim(), pointsUsed);
                } catch (Exception e) {
                    System.err.println("[BookingService] Lỗi trừ điểm VNPay: " + e.getMessage());
                }
            }
            if (earnedPoints > 0) {
                try {
                    loyaltyService.addPoints(resolvedUserId.trim(), earnedPoints);
                } catch (Exception e) {
                    System.err.println("[BookingService] Lỗi cộng điểm VNPay: " + e.getMessage());
                }
            }
        }

        return success;
    }

    public boolean completePaymentVNPay(String bookingId, String transactionNo, BigDecimal amount) {
        return completePaymentVNPay(bookingId, transactionNo, amount, null, null, null, null);
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
