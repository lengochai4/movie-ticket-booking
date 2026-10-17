USE cinema_db;

-- 1. User: Tai khoan demo (password da bam bang BCrypt la: 123456)
INSERT INTO User (userID, fullName, passwordHash, email, phone, role, loyaltyPoints) VALUES
(1, 'Admin Cinema', '$2a$10$abcdefghijklmnopqrstuvwxyzA1B2C3D4E5F6G7H8I9J0K', 'admin@cinema.com', '0901234567', 1, 100),
(2, 'Le Ngoc Hai', '$2a$10$abcdefghijklmnopqrstuvwxyzA1B2C3D4E5F6G7H8I9J0K', 'hai@gmail.com', '0987654321', 0, 50),
(3, 'Nguyen Van A', '$2a$10$abcdefghijklmnopqrstuvwxyzA1B2C3D4E5F6G7H8I9J0K', 'usera@gmail.com', '0912345678', 0, 0);

-- 2. Cinema: 2 Rap mau
INSERT INTO Cinema (cinemaID, name, location) VALUES
('CINEMA_01', 'N3 Cinema Đồng Nai Center', '123 Phạm Văn Thuận, Biên Hòa, Đồng Nai'),
('CINEMA_02', 'N3 Cinema Landmark TP.HCM', '208 Nguyễn Hữu Cảnh, Quận Bình Thạnh, TP.HCM');

-- 3. Room: 2 Phong chieu
INSERT INTO Room (roomID, roomName, capacity, roomType, cinemaID) VALUES
('ROOM_01', 'Phòng Chiếu 1', 20, '2D Digital', 'CINEMA_01'),
('ROOM_02', 'Phòng Chiếu IMAX', 30, 'IMAX 3D', 'CINEMA_01');

-- 4. Seat: 20 ghe cho ROOM_01 (4 hang A, B, C, D moi hang 5 ghe)
INSERT INTO Seat (seatID, roomID, `row`, `column`, `type`, extraPrice) VALUES
('A1', 'ROOM_01', 'A', '1', 'STANDARD', 0.00),
('A2', 'ROOM_01', 'A', '2', 'STANDARD', 0.00),
('A3', 'ROOM_01', 'A', '3', 'STANDARD', 0.00),
('A4', 'ROOM_01', 'A', '4', 'STANDARD', 0.00),
('A5', 'ROOM_01', 'A', '5', 'STANDARD', 0.00),
('B1', 'ROOM_01', 'B', '1', 'STANDARD', 0.00),
('B2', 'ROOM_01', 'B', '2', 'STANDARD', 0.00),
('B3', 'ROOM_01', 'B', '3', 'STANDARD', 0.00),
('B4', 'ROOM_01', 'B', '4', 'STANDARD', 0.00),
('B5', 'ROOM_01', 'B', '5', 'STANDARD', 0.00),
('C1', 'ROOM_01', 'C', '1', 'VIP', 20000.00),
('C2', 'ROOM_01', 'C', '2', 'VIP', 20000.00),
('C3', 'ROOM_01', 'C', '3', 'VIP', 20000.00),
('C4', 'ROOM_01', 'C', '4', 'VIP', 20000.00),
('C5', 'ROOM_01', 'C', '5', 'VIP', 20000.00),
('D1', 'ROOM_01', 'D', '1', 'COUPLE', 50000.00),
('D2', 'ROOM_01', 'D', '2', 'COUPLE', 50000.00),
('D3', 'ROOM_01', 'D', '3', 'COUPLE', 50000.00),
('D4', 'ROOM_01', 'D', '4', 'COUPLE', 50000.00),
('D5', 'ROOM_01', 'D', '5', 'COUPLE', 50000.00);

-- 5. Movie: 2 Phim dang chieu
INSERT INTO Movie (movieID, title, description, posterURL, trailerURL, director, cast, genre, duration, releaseDate, language, ageRating, status) VALUES
('MOV_01', 'Avengers: Secret Wars', 'Trận chiến đa vũ trụ đỉnh cao của biệt đội siêu anh hùng Marvel.', 'assets/img/avengers.jpg', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 'Russo Brothers', 'Robert Downey Jr, Chris Evans', 'Hành động, Viễn tưởng', '150 phút', '2026-05-01', 'Tiếng Anh - Phụ đề Tiếng Việt', 'C16', 'NOW_SHOWING'),
('MOV_02', 'Lật Mặt 8', 'Bộ phim điện ảnh hành động kịch tính và cảm động về gia đình.', 'assets/img/latmat8.jpg', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 'Lý Hải', 'Dàn diễn viên tài năng Việt Nam', 'Hành động, Hài', '120 phút', '2026-04-30', 'Tiếng Việt', 'C13', 'NOW_SHOWING');

-- 6. Showtime: 2 Suat chieu mau
INSERT INTO Showtime (showTimeID, movieID, roomID, startTime, endTime, price) VALUES
('ST_01', 'MOV_01', 'ROOM_01', '2026-10-18 19:00:00', '2026-10-18 21:30:00', 80000.00),
('ST_02', 'MOV_02', 'ROOM_01', '2026-10-18 15:00:00', '2026-10-18 17:00:00', 75000.00);

-- 7. Combo: 3 Goi bap nuoc
INSERT INTO Combo (comboID, name, description, price) VALUES
('CB_01', 'Combo Solo', '1 Bắp ngọt lớn + 1 Nước ngọt lớn', 65000.00),
('CB_02', 'Combo Couple', '1 Bắp phô mai lớn + 2 Nước ngọt lớn', 99000.00),
('CB_03', 'Combo Gia Đình', '2 Bắp lớn + 3 Nước ngọt + 1 Snack khoai tây', 149000.00);

-- 8. Payment & Booking mau (da PAID de test thong ke va hien thi ve)
INSERT INTO Payment (paymentID, type, amount, paymentDate) VALUES
('PAY_DEMO_01', 'VNPAY', 160000.00, NOW());

INSERT INTO Booking (bookingID, userID, paymentID, bookingStatus, createdAt, expiresAt) VALUES
('BK_DEMO_01', 2, 'PAY_DEMO_01', 'PAID', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY));

INSERT INTO Ticket (ticketID, bookingID, movieID, cinemaID, showTimeID, seatID, ticketStatus) VALUES
('TK_01', 'BK_DEMO_01', 'MOV_01', 'CINEMA_01', 'ST_01', 'A1', 'AVAILABLE'),
('TK_02', 'BK_DEMO_01', 'MOV_01', 'CINEMA_01', 'ST_01', 'A2', 'AVAILABLE');

INSERT INTO BookingCombo (bookingComboID, bookingID, comboID, quantity) VALUES
('BC_01', 'BK_DEMO_01', 'CB_01', 1);

-- 9. Review mau
INSERT INTO Review (reviewID, userID, movieID, rating, comment, createdAt) VALUES
('REV_01', 2, 'MOV_01', 5.0, 'Phim quá đỉnh, âm thanh kỹ xảo rạp chiếu cực hay!', NOW());
