-- ====================================================================
-- SCRIPT XÓA TOÀN BỘ VÉ & ĐƠN ĐẶT (RESET TOÀN BỘ GHẾ VỀ TRỐNG 100%)
-- ====================================================================

-- 1. Xóa vé trong bảng Ticket (giải phóng tất cả các ghế)
DELETE FROM Ticket;

-- 2. Xóa chi tiết bắp nước kèm theo đơn
DELETE FROM BookingCombo;

-- 3. Xóa các đơn đặt vé Booking
DELETE FROM Booking;

-- 4. Xóa các bản ghi thanh toán thử nghiệm
DELETE FROM Payment;

-- 5. Kiểm tra lại kết quả (kỳ vọng: 0 rows)
SELECT COUNT(*) AS "Số Vé Còn Lại" FROM Ticket;
SELECT COUNT(*) AS "Số Đơn Đặt Còn Lại" FROM Booking;
