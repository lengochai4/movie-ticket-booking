-- ====================================================================
-- DỮ LIỆU MẪU: Cinema Booking System (PostgreSQL)
-- Phụ trách: Hải (Backend Developer)
-- ====================================================================

-- 1. Chèn dữ liệu User (1 Admin, 1 User thường với userID=2)
-- Mật khẩu hash demo bằng BCrypt tương ứng với: 123456
INSERT INTO "User" (userID, fullName, passwordHash, email, phone, role, loyaltyPoints) VALUES
(1, 'Admin Cinema', '$2a$10$abcdefghijklmnopqrstuvwxyzA1B2C3D4E5F6G7H8I9J0K', 'admin@cinema.com', '0901234567', 1, 100),
(2, 'Le Ngoc Hai', '$2a$10$abcdefghijklmnopqrstuvwxyzA1B2C3D4E5F6G7H8I9J0K', 'hai@gmail.com', '0987654321', 0, 50);

-- Đồng bộ sequence cho userID sau khi chèn id thủ công
SELECT setval(pg_get_serial_sequence('"User"', 'userid'), COALESCE((SELECT MAX(userID) FROM "User"), 1));

-- 2. Chèn 1 Cụm rạp (CINEMA_01)
INSERT INTO Cinema (cinemaID, name, location) VALUES
('CINEMA_01', 'N3 Cinema Đồng Nai Center', '123 Phạm Văn Thuận, Biên Hòa, Đồng Nai');

-- 3. Chèn 1 Phòng chiếu (ROOM_01, sức chứa 20 ghế)
INSERT INTO Room (roomID, roomName, capacity, roomType, cinemaID) VALUES
('ROOM_01', 'Phòng Chiếu 1', 20, '2D Digital', 'CINEMA_01');

-- 4. Chèn 20 Ghế cho ROOM_01 (4 hàng A, B, C, D mỗi hàng 5 ghế)
INSERT INTO Seat (seatID, roomID, "row", "column", type, extraPrice) VALUES
-- Hàng A: STANDARD (extraPrice = 0.00)
('A1', 'ROOM_01', 'A', '1', 'STANDARD', 0.00),
('A2', 'ROOM_01', 'A', '2', 'STANDARD', 0.00),
('A3', 'ROOM_01', 'A', '3', 'STANDARD', 0.00),
('A4', 'ROOM_01', 'A', '4', 'STANDARD', 0.00),
('A5', 'ROOM_01', 'A', '5', 'STANDARD', 0.00),

-- Hàng B: STANDARD (extraPrice = 0.00)
('B1', 'ROOM_01', 'B', '1', 'STANDARD', 0.00),
('B2', 'ROOM_01', 'B', '2', 'STANDARD', 0.00),
('B3', 'ROOM_01', 'B', '3', 'STANDARD', 0.00),
('B4', 'ROOM_01', 'B', '4', 'STANDARD', 0.00),
('B5', 'ROOM_01', 'B', '5', 'STANDARD', 0.00),

-- Hàng C: VIP (extraPrice = 20000.00)
('C1', 'ROOM_01', 'C', '1', 'VIP', 20000.00),
('C2', 'ROOM_01', 'C', '2', 'VIP', 20000.00),
('C3', 'ROOM_01', 'C', '3', 'VIP', 20000.00),
('C4', 'ROOM_01', 'C', '4', 'VIP', 20000.00),
('C5', 'ROOM_01', 'C', '5', 'VIP', 20000.00),

-- Hàng D: COUPLE (extraPrice = 50000.00)
('D1', 'ROOM_01', 'D', '1', 'COUPLE', 50000.00),
('D2', 'ROOM_01', 'D', '2', 'COUPLE', 50000.00),
('D3', 'ROOM_01', 'D', '3', 'COUPLE', 50000.00),
('D4', 'ROOM_01', 'D', '4', 'COUPLE', 50000.00),
('D5', 'ROOM_01', 'D', '5', 'COUPLE', 50000.00);

-- 5. Chèn 2 Phim (MOV_01, MOV_02)
INSERT INTO Movie (movieID, title, description, posterURL, trailerURL, director, castText, genre, duration, releaseDate, language, ageRating, status) VALUES
('MOV_01', 'Avengers: Doomsday', 'Biệt đội siêu anh hùng tái hợp trong cuộc chiến đa vũ trụ chống lại Doctor Doom.', 'https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 'Russo Brothers', 'Robert Downey Jr., Pedro Pascal, Vanessa Kirby', 'Hành động, Viễn tưởng', '150 phút', '2026-05-01', 'Tiếng Anh - Phụ đề Tiếng Việt', 'C16', 'NOW_SHOWING'),
('MOV_02', 'Lật Mặt 8: Vòng Tay Người Mẹ', 'Câu chuyện gia đình đầy cảm xúc xen lẫn những pha hành động nghẹt thở và hài hước duyên dáng.', 'https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 'Lý Hải', 'Dàn diễn viên điện ảnh Việt Nam', 'Hành động, Hài, Gia đình', '125 phút', '2026-04-30', 'Tiếng Việt', 'C13', 'NOW_SHOWING');

-- 6. Chèn 2 Suất chiếu (ST_01, ST_02) trong tương lai gần
INSERT INTO Showtime (showTimeID, movieID, roomID, startTime, endTime, price) VALUES
('ST_01', 'MOV_01', 'ROOM_01', CURRENT_TIMESTAMP + INTERVAL '1 day 2 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 4 hours 30 minutes', 80000.00),
('ST_02', 'MOV_02', 'ROOM_01', CURRENT_TIMESTAMP + INTERVAL '1 day 5 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 7 hours 5 minutes', 75000.00);

-- 7. Chèn 3 Combo bắp nước
INSERT INTO Combo (comboID, name, description, price) VALUES
('CB_01', 'Combo Solo', '1 Bắp ngọt lớn + 1 Nước ngọt lớn (Lựa chọn Coca / Sprite / Fanta)', 65000.00),
('CB_02', 'Combo Couple', '1 Bắp phô mai lớn + 2 Nước ngọt lớn', 99000.00),
('CB_03', 'Combo Gia Đình', '2 Bắp rang bơ lớn + 3 Nước ngọt + 1 Snack khoai tây chiên', 149000.00);
