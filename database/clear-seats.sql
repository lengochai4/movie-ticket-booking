-- ====================================================================
-- SCRIPT XÓA TOÀN BỘ VÉ & ĐƠN ĐẶT (RESET TOÀN BỘ GHẾ VỀ TRỐNG 100%)
-- Áp dụng cho cơ sở dữ liệu chung PostgreSQL (Oregon Cloud Render)
-- ====================================================================

-- 1. Xóa vé trong bảng tickets (giải phóng tất cả các ghế)
DELETE FROM tickets;

-- 2. Xóa các đơn đặt vé trong bảng bookings
DELETE FROM bookings;

-- 3. Kiểm tra lại kết quả (kỳ vọng: 0 rows)
SELECT COUNT(*) AS "Số Vé Còn Lại" FROM tickets;
SELECT COUNT(*) AS "Số Đơn Đặt Còn Lại" FROM bookings;
