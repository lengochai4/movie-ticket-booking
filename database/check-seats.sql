-- ====================================================================
-- TRUY VẤN KIỂM TRA TRẠNG THÁI GHẾ REAL-TIME (Cinema Booking System)
-- ====================================================================
-- KẾT NỐI CLOUD RENDER (DBeaver / pgAdmin / IntelliJ Database Tool):
-- Host:     dpg-datt14ou01pc73aecb50-a.oregon-postgres.render.com
-- Port:     5432
-- Database: email_list_db_jut9
-- User:     email_list_db_jut9_user
-- Password: 2hI1wdwOOKNvIH77Bilyuv4COJ7mkXDB
-- SSL Mode: Require
-- ====================================================================

-- CÁCH 1: Xem tất cả các ghế ĐANG ĐƯỢC GIỮ CHỖ hoặc ĐÃ MUA gần đây nhất
SELECT 
    b.bookingid AS "Mã Đơn",
    b.ordercode AS "Mã Tra Cứu (OrderCode)",
    COALESCE(t.seatlabel, s.seatcode, CONCAT(s.seatrow, s.seatcol)) AS "Tên Ghế",
    s.seattype AS "Loại Ghế",
    t.seatid AS "Mã Ghế (UUID)",
    b.showtimeid AS "Suất Chiếu",
    b.movietitle AS "Phim",
    b.bookingstatus AS "Trạng Thái Đơn",
    CASE 
        WHEN b.bookingstatus = 'PAID' THEN '🟢 ĐÃ BÁN (PAID)'
        WHEN b.bookingstatus = 'PENDING' 
             AND (b.createdat + INTERVAL '10 minute') > CURRENT_TIMESTAMP 
             THEN '🟡 ĐANG GIỮ CHỖ (PENDING)'
        WHEN b.bookingstatus = 'PENDING' 
             AND (b.createdat + INTERVAL '10 minute') <= CURRENT_TIMESTAMP 
             THEN '🔴 HẾT HẠN GIỮ CHỖ (Chờ giải phóng)'
        WHEN b.bookingstatus = 'CANCELLED' THEN '⚪ ĐÃ HỦY (CANCELLED)'
        ELSE b.bookingstatus
    END AS "Tình Trạng Ghế",
    ROUND(CAST(EXTRACT(EPOCH FROM ((b.createdat + INTERVAL '10 minute') - CURRENT_TIMESTAMP)) / 60 AS NUMERIC), 1) AS "Phút Còn Lại",
    COALESCE(b.customername, u.full_name, 'Khách Vãng Lai') AS "Người Đặt",
    b.createdat AS "Thời Điểm Tạo"
FROM tickets t
JOIN bookings b ON t.bookingid = b.bookingid
LEFT JOIN seats s ON t.seatid = s.seatid
LEFT JOIN users u ON b.userid = u.user_id
ORDER BY b.createdat DESC;


-- CÁCH 2: Xem toàn bộ ghế của Suất chiếu (Xem ghế nào TRỐNG, ghế nào BỊ KHÓA)
-- Gợi ý: Thay mã suất chiếu bên dưới hoặc để mặc định kiểm tra suất chiếu mới nhất
SELECT 
    COALESCE(s.seatcode, CONCAT(s.seatrow, s.seatcol)) AS "Tên Ghế",
    s.seatrow AS "Hàng",
    s.seatcol AS "Cột",
    s.seattype AS "Loại Ghế",
    COALESCE(
        CASE 
            WHEN b.bookingstatus = 'PAID' THEN '🟢 ĐÃ BÁN (PAID)'
            WHEN b.bookingstatus = 'PENDING' 
                 AND (b.createdat + INTERVAL '10 minute') > CURRENT_TIMESTAMP 
                THEN '🟡 ĐANG GIỮ (' || ROUND(CAST(EXTRACT(EPOCH FROM ((b.createdat + INTERVAL '10 minute') - CURRENT_TIMESTAMP)) / 60 AS NUMERIC), 1) || ' phút)'
            ELSE NULL 
        END, 
        '⚪ CÒN TRỐNG'
    ) AS "Trạng Thái Ghế",
    b.bookingid AS "Mã Đơn Đặt",
    s.seatid AS "UUID Ghế"
FROM seats s
JOIN showtimes st ON s.room_id = st.room_id
LEFT JOIN tickets t ON s.seatid = t.seatid
LEFT JOIN bookings b ON t.bookingid = b.bookingid 
    AND b.showtimeid = st.showtimeid
    AND t.ticketstatus != 'CANCELLED'
    AND (b.bookingstatus = 'PAID' 
         OR (b.bookingstatus = 'PENDING' AND (b.createdat + INTERVAL '10 minute') > CURRENT_TIMESTAMP))
WHERE st.showtimeid = (
    SELECT showtimeid FROM showtimes ORDER BY starttime DESC LIMIT 1
)
ORDER BY s.seatrow, s.seatcol;
