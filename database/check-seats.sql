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
    t.seatID AS "Mã Ghế",
    t.showTimeID AS "Suất Chiếu",
    b.bookingStatus AS "Trạng Thái Đơn",
    CASE 
        WHEN b.bookingStatus = 'PAID' THEN 'ĐÃ BÁN (PAID)'
        WHEN b.bookingStatus = 'PENDING' AND b.expiresAt > CURRENT_TIMESTAMP THEN 'ĐANG GIỮ CHỖ (PENDING)'
        WHEN b.bookingStatus = 'PENDING' AND b.expiresAt <= CURRENT_TIMESTAMP THEN 'HẾT HẠN GIỮ CHỖ (Chờ nhả)'
        WHEN b.bookingStatus = 'CANCELLED' THEN 'ĐÃ HỦY (CANCELLED)'
        ELSE b.bookingStatus
    END AS "Tình Trạng Ghế",
    ROUND(CAST(EXTRACT(EPOCH FROM (b.expiresAt - CURRENT_TIMESTAMP)) / 60 AS NUMERIC), 1) AS "Phút Còn Lại",
    b.bookingID AS "Mã Đơn",
    u.fullName AS "Người Đặt",
    b.createdAt AS "Thời Điểm Tạo",
    b.expiresAt AS "Hạn Giữ Chỗ"
FROM Ticket t
JOIN Booking b ON t.bookingID = b.bookingID
LEFT JOIN "User" u ON b.userID = u.userID
ORDER BY b.createdAt DESC;


-- CÁCH 2: Xem toàn bộ 20 ghế của Suất chiếu 'ST_01' (Xem ghế nào TRỐNG, ghế nào BỊ KHÓA)
SELECT 
    s.seatID AS "Ghế",
    s."row" AS "Hàng",
    s."column" AS "Cột",
    s.type AS "Loại Ghế",
    COALESCE(
        CASE 
            WHEN b.bookingStatus = 'PAID' THEN 'ĐÃ BÁN'
            WHEN b.bookingStatus = 'PENDING' AND b.expiresAt > CURRENT_TIMESTAMP 
                THEN 'ĐANG GIỮ (' || ROUND(CAST(EXTRACT(EPOCH FROM (b.expiresAt - CURRENT_TIMESTAMP)) / 60 AS NUMERIC), 1) || ' phút)'
            ELSE NULL 
        END, 
        'CÒN TRỐNG'
    ) AS "Trạng Thái Ghế",
    b.bookingID AS "Mã Đơn Đặt"
FROM Seat s
LEFT JOIN Ticket t ON s.seatID = t.seatID AND t.showTimeID = 'ST_01'
LEFT JOIN Booking b ON t.bookingID = b.bookingID 
    AND (b.bookingStatus = 'PAID' OR (b.bookingStatus = 'PENDING' AND b.expiresAt > CURRENT_TIMESTAMP))
WHERE s.roomID = 'ROOM_01'
ORDER BY s."row", CAST(s."column" AS INTEGER);
