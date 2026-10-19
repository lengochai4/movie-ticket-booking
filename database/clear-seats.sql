-- ====================================================================
-- SCRIPT DỌN DẸP GHẾ & ĐƠN ĐẶT TEST (AN TOÀN CHO CƠ SỞ DỮ LIỆU CHUNG)
-- Áp dụng cho cơ sở dữ liệu chung PostgreSQL (Oregon Cloud Render)
-- CẢNH BÁO: CHỈ XÓA ĐƠN TEST HOẶC ĐƠN PENDING ĐÃ QUÁ HẠN
-- KHÔNG XÓA ĐƠN ĐÃ THANH TOÁN (PAID) ĐỂ BẢO VỆ DOANH THU CẢ NHÓM
-- ====================================================================

-- 1. Giải phóng vé của các đơn test hoặc đơn PENDING đã hết hạn (> 10 phút)
DELETE FROM tickets 
WHERE bookingid IN (
    SELECT bookingid FROM bookings 
    WHERE bookingid LIKE 'BK_TEST_%' 
       OR customername LIKE '%Test%'
       OR (bookingstatus = 'PENDING' AND (createdat + INTERVAL '10 minute') < CURRENT_TIMESTAMP)
);

-- 2. Xóa các đơn test hoặc đơn PENDING đã hết hạn (> 10 phút)
DELETE FROM bookings 
WHERE bookingid LIKE 'BK_TEST_%' 
   OR customername LIKE '%Test%'
   OR (bookingstatus = 'PENDING' AND (createdat + INTERVAL '10 minute') < CURRENT_TIMESTAMP);

-- 3. Kiểm tra lại kết quả
SELECT COUNT(*) AS "Tổng Số Đơn PAID Còn Lại (An Toàn)" FROM bookings WHERE bookingstatus = 'PAID';
SELECT COUNT(*) AS "Tổng Số Đơn PENDING Còn Lại" FROM bookings WHERE bookingstatus = 'PENDING';
