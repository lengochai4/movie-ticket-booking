-- ====================================================================
-- TRUY VẤN KIỂM TRA TRẠNG THÁI GHẾ REAL-TIME (Cinema Booking System)
-- ====================================================================
-- KẾT NỐI CLOUD RENDER (DBeaver / pgAdmin / IntelliJ Database Tool):
-- Host:     dpg-dbapchnbtjjs7382rt0g-a.singapore-postgres.render.com
-- Port:     5432
-- Database: cinema_postgres_db
-- User:     cinema_postgres_db_user
-- Password: ipzu40YAWB3VmNjkPpGpMoBW4zREBtDo
-- SSL Mode: Require
-- ====================================================================

-- CÁCH 1: Xem tất cả các ghế ĐANG ĐƯỢC GIỮ CHỖ hoặc ĐÃ MUA gần đây nhất
SELECT 
    b.bookingID AS "Mã Đơn",
    CONCAT(s."row", s."column") AS "Tên Ghế",
    s.type AS "Loại Ghế",
    t.seatID AS "Mã Ghế (UUID)",
    t.showTimeID AS "Suất Chiếu",
    b.bookingStatus AS "Trạng Thái Đơn",
    CASE 
        WHEN b.bookingStatus = 'PAID' THEN '🟢 ĐÃ BÁN (PAID)'
        WHEN b.bookingStatus = 'PENDING' AND b.expiresAt > CURRENT_TIMESTAMP THEN '🟡 ĐANG GIỮ CHỖ (PENDING)'
        WHEN b.bookingStatus = 'PENDING' AND b.expiresAt <= CURRENT_TIMESTAMP THEN '🔴 HẾT HẠN GIỮ CHỖ (Chờ nhả)'
        WHEN b.bookingStatus = 'CANCELLED' THEN '⚪ ĐÃ HỦY (CANCELLED)'
        ELSE b.bookingStatus
    END AS "Tình Trạng Ghế",
    ROUND(CAST(EXTRACT(EPOCH FROM (b.expiresAt - CURRENT_TIMESTAMP)) / 60 AS NUMERIC), 1) AS "Phút Còn Lại",
    u.fullName AS "Người Đặt",
    b.createdAt AS "Thời Điểm Tạo"
FROM Ticket t
JOIN Booking b ON t.bookingID = b.bookingID
LEFT JOIN Seat s ON t.seatID = s.seatID
LEFT JOIN "User" u ON b.userID = u.userID
ORDER BY b.createdAt DESC;


-- CÁCH 2: Xem toàn bộ ghế của Suất chiếu (Xem ghế nào TRỐNG, ghế nào BỊ KHÓA)
-- Gợi ý: Thay mã suất chiếu bên dưới hoặc để mặc định kiểm tra suất chiếu mới nhất
SELECT 
    CONCAT(s."row", s."column") AS "Tên Ghế",
    s."row" AS "Hàng",
    s."column" AS "Cột",
    s.type AS "Loại Ghế",
    COALESCE(
        CASE 
            WHEN b.bookingStatus = 'PAID' THEN '🟢 ĐÃ BÁN (PAID)'
            WHEN b.bookingStatus = 'PENDING' AND b.expiresAt > CURRENT_TIMESTAMP 
                THEN '🟡 ĐANG GIỮ (' || ROUND(CAST(EXTRACT(EPOCH FROM (b.expiresAt - CURRENT_TIMESTAMP)) / 60 AS NUMERIC), 1) || ' phút)'
            ELSE NULL 
        END, 
        '⚪ CÒN TRỐNG'
    ) AS "Trạng Thái Ghế",
    b.bookingID AS "Mã Đơn Đặt",
    s.seatID AS "UUID Ghế"
FROM Seat s
JOIN Showtime st ON s.roomID = st.roomID
LEFT JOIN Ticket t ON s.seatID = t.seatID AND t.showTimeID = st.showTimeID
LEFT JOIN Booking b ON t.bookingID = b.bookingID 
    AND (b.bookingStatus = 'PAID' OR (b.bookingStatus = 'PENDING' AND b.expiresAt > CURRENT_TIMESTAMP))
WHERE st.showTimeID = (SELECT showTimeID FROM Ticket ORDER BY ticketID DESC LIMIT 1)
ORDER BY s."row", CAST(s."column" AS INTEGER);
