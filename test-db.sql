SELECT
    t.seatID AS "Mã Ghế",
    t.showTimeID AS "Suất Chiếu",
    b.bookingStatus AS "Trạng Thái Đơn",
    CASE
        WHEN b.bookingStatus = 'PAID' THEN '🟢 ĐÃ BÁN (PAID)'
        WHEN b.bookingStatus = 'PENDING' AND b.expiresAt > CURRENT_TIMESTAMP THEN '🟡 ĐANG GIỮ CHỖ (PENDING)'
        WHEN b.bookingStatus = 'PENDING' AND b.expiresAt <= CURRENT_TIMESTAMP THEN '🔴 HẾT HẠN GIỮ CHỖ'
        WHEN b.bookingStatus = 'CANCELLED' THEN '⚪ ĐÃ HỦY (CANCELLED)'
        ELSE b.bookingStatus
        END AS "Tình Trạng Ghế",
    ROUND(CAST(EXTRACT(EPOCH FROM (b.expiresAt - CURRENT_TIMESTAMP)) / 60 AS NUMERIC), 1) AS "Phút Còn Lại",
    b.bookingID AS "Mã Đơn",
    u.fullName AS "Người Đặt",
    b.createdAt AS "Thời Điểm Tạo"
FROM Ticket t
         JOIN Booking b ON t.bookingID = b.bookingID
         LEFT JOIN "User" u ON b.userID = u.userID
ORDER BY b.createdAt DESC;
