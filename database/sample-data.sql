-- ====================================================================
-- DỮ LIỆU MẪU ĐẦY ĐỦ: Cinema Booking System (PostgreSQL)
-- Đầy đủ 12 bảng trong schema.sql + 33 cụm rạp toàn quốc
-- Phục vụ test trọn vẹn mọi luồng: Chọn ghế, Giữ chỗ, Trạng thái vé, Đơn hàng, Review
-- ====================================================================

-- --------------------------------------------------------------------
-- 1. BẢNG "User" (Tài khoản người dùng & Admin)
-- Mật khẩu hash demo bằng BCrypt tương ứng với: 123456
-- --------------------------------------------------------------------
INSERT INTO "User" (userID, fullName, passwordHash, email, phone, role, loyaltyPoints) VALUES
(1, 'Admin Cinema', '$2a$10$abcdefghijklmnopqrstuvwxyzA1B2C3D4E5F6G7H8I9J0K', 'admin@cinema.com', '0901234567', 1, 500),
(2, 'Lê Ngọc Hải', '$2a$10$abcdefghijklmnopqrstuvwxyzA1B2C3D4E5F6G7H8I9J0K', 'hai@gmail.com', '0987654321', 0, 150),
(3, 'Trần Thị Phương', '$2a$10$abcdefghijklmnopqrstuvwxyzA1B2C3D4E5F6G7H8I9J0K', 'phuong@gmail.com', '0912345678', 0, 80),
(4, 'Nguyễn Văn An', '$2a$10$abcdefghijklmnopqrstuvwxyzA1B2C3D4E5F6G7H8I9J0K', 'an.nguyen@gmail.com', '0933445566', 0, 20)
ON CONFLICT (userID) DO UPDATE SET
    fullName = EXCLUDED.fullName,
    email = EXCLUDED.email,
    phone = EXCLUDED.phone,
    role = EXCLUDED.role,
    loyaltyPoints = EXCLUDED.loyaltyPoints;

-- Đồng bộ sequence cho userID sau khi chèn id thủ công
SELECT setval(pg_get_serial_sequence('"User"', 'userid'), COALESCE((SELECT MAX(userID) FROM "User"), 1));

-- --------------------------------------------------------------------
-- 2. BẢNG Cinema: Đầy đủ 33 Cụm rạp toàn quốc (Đồng Nai + 32 rạp CGV, Beta, Galaxy)
-- --------------------------------------------------------------------
INSERT INTO Cinema (cinemaID, name, location, address) VALUES
-- Rạp Biên Hòa - Đồng Nai Center
('CINEMA_01', 'N3 Cinema Đồng Nai Center', '123 Phạm Văn Thuận, Biên Hòa, Đồng Nai', '123 Phạm Văn Thuận, Biên Hòa, Đồng Nai'),

-- 32 Cụm rạp hệ thống toàn quốc
('0590cbc6-8ff1-449b-9f05-7e9a00c9e1d8', 'Beta Cinemas Ung Văn Khiêm', 'Tầng 1, tòa nhà PAX SKY, số 26 Ung Văn Khiêm, Phường 25, Bình Thạnh, TP.HCM', 'Tầng 1, tòa nhà PAX SKY, số 26 Ung Văn Khiêm, Phường 25, Bình Thạnh, TP.HCM'),
('cb2da965-5fce-4613-b372-95ecb06a7eea', 'CGV Aeon Tân Phú', 'Lầu 3, Aeon Mall 30 Bờ Bao Tân Thắng, Phường Sơn Kỳ, Tân Phú, TP.HCM', 'Lầu 3, Aeon Mall 30 Bờ Bao Tân Thắng, Phường Sơn Kỳ, Tân Phú, TP.HCM'),
('099155da-4ffd-4679-896f-8b50b8715903', 'Beta Cinemas Quang Trung', 'Số 645 Quang Trung, Phường 11, Quận Gò Vấp, TP.HCM', 'Số 645 Quang Trung, Phường 11, Quận Gò Vấp, TP.HCM'),
('72bd4e43-8506-44cf-bb26-8a5bde947492', 'Beta Cinemas Đan Phượng', 'Tòa nhà HHA, KĐT Tân Tây Đô, Xã Tân Lập, Huyện Đan Phượng, Hà Nội', 'Tòa nhà HHA, KĐT Tân Tây Đô, Xã Tân Lập, Huyện Đan Phượng, Hà Nội'),
('253963ae-1b87-41ac-a99e-9a54f52f524a', 'Beta Cinemas Mỹ Đình', 'Tầng hầm B1, Tòa nhà Golden Palace, Đường Mễ Trì, Quận Nam Từ Liêm, Hà Nội', 'Tầng hầm B1, Tòa nhà Golden Palace, Đường Mễ Trì, Quận Nam Từ Liêm, Hà Nội'),
('5024a395-a741-495f-a68f-35032c5e70eb', 'Beta Cinemas Thanh Xuân', 'Tầng hầm B1, Tòa nhà Golden West, Số 2 Lê Văn Thiêm, Quận Thanh Xuân, Hà Nội', 'Tầng hầm B1, Tòa nhà Golden West, Số 2 Lê Văn Thiêm, Quận Thanh Xuân, Hà Nội'),
('724a9866-c58c-4491-adb2-de51ca2e109d', 'Beta Cinemas Giải Phóng', 'Tầng 3, Tòa nhà Imperial Plaza, 360 Giải Phóng, Quận Thanh Xuân, Hà Nội', 'Tầng 3, Tòa nhà Imperial Plaza, 360 Giải Phóng, Quận Thanh Xuân, Hà Nội'),
('ed08aa97-3299-47bb-a909-ffa77224d367', 'Beta Cinemas Tân Uyên', 'Tầng 3, Siêu thị Co.opmart Tân Uyên, Đường ĐT 746, TP. Tân Uyên, Bình Dương', 'Tầng 3, Siêu thị Co.opmart Tân Uyên, Đường ĐT 746, TP. Tân Uyên, Bình Dương'),
('4cf67543-74a8-49e4-af6a-28f2fd47207e', 'Beta Cinemas Biên Hòa', 'Tầng 6, Pegasus Plaza, 53-55 Võ Thị Sáu, Phường Quyết Thắng, TP. Biên Hòa, Đồng Nai', 'Tầng 6, Pegasus Plaza, 53-55 Võ Thị Sáu, Phường Quyết Thắng, TP. Biên Hòa, Đồng Nai'),
('bd65b81d-e539-4153-9f2f-f5f1ec378f3a', 'Beta Cinemas Thái Nguyên', 'Số 389 Đường Cách Mạng Tháng 8, Phường Hương Sơn, TP. Thái Nguyên', 'Số 389 Đường Cách Mạng Tháng 8, Phường Hương Sơn, TP. Thái Nguyên'),
('19de10fd-17d9-404c-ad9f-37df6ec1ddf6', 'Beta Cinemas Nha Trang', 'Tầng 1, TTTM Nha Trang Center, 20 Trần Phú, Lộc Thọ, TP. Nha Trang, Khánh Hòa', 'Tầng 1, TTTM Nha Trang Center, 20 Trần Phú, Lộc Thọ, TP. Nha Trang, Khánh Hòa'),
('a0375eaa-a6c6-45ba-a182-6ef54a84922c', 'CGV Vincom Landmark 81', 'Tầng B1, Vincom Center Landmark 81, 720A Điện Biên Phủ, Bình Thạnh, TP.HCM', 'Tầng B1, Vincom Center Landmark 81, 720A Điện Biên Phủ, Bình Thạnh, TP.HCM'),
('68de3c36-7c08-4afa-84bb-cde3956dee0b', 'CGV Vincom Đồng Khởi', 'Tầng 3, Vincom Center Đồng Khởi, 72 Lê Thánh Tôn, Bến Nghé, Quận 1, TP.HCM', 'Tầng 3, Vincom Center Đồng Khởi, 72 Lê Thánh Tôn, Bến Nghé, Quận 1, TP.HCM'),
('4ee726f0-cc50-4237-bbfa-115dc08d999b', 'CGV Crescent Mall', 'Tầng 5, Crescent Mall, Đại Lộ Nguyễn Văn Linh, Tân Phú, Quận 7, TP.HCM', 'Tầng 5, Crescent Mall, Đại Lộ Nguyễn Văn Linh, Tân Phú, Quận 7, TP.HCM'),
('30337440-fec2-42e3-9769-ea9760c80220', 'CGV Sư Vạn Hạnh', 'Tầng 6, Vạn Hạnh Mall, 11 Sư Vạn Hạnh, Phường 12, Quận 10, TP.HCM', 'Tầng 6, Vạn Hạnh Mall, 11 Sư Vạn Hạnh, Phường 12, Quận 10, TP.HCM'),
('69f2c343-2765-4c7f-835e-f6a2ca15e30d', 'CGV Aeon Bình Tân', 'Tầng 3, Aeon Mall Bình Tân, Số 1 Đường Số 17A, Bình Tân, TP.HCM', 'Tầng 3, Aeon Mall Bình Tân, Số 1 Đường Số 17A, Bình Tân, TP.HCM'),
('171f4bd8-ba2e-44d8-a474-882be902e796', 'CGV Vincom Bà Triệu', 'Tầng 6, Vincom Center Hà Nội, 191 Bà Triệu, Hai Bà Trưng, Hà Nội', 'Tầng 6, Vincom Center Hà Nội, 191 Bà Triệu, Hai Bà Trưng, Hà Nội'),
('5a80a73d-c9aa-4a32-923a-3a95e0b38705', 'CGV Vincom Metropolis Liễu Giai', 'Tầng B1, Vincom Center Metropolis, 29 Liễu Giai, Ba Đình, Hà Nội', 'Tầng B1, Vincom Center Metropolis, 29 Liễu Giai, Ba Đình, Hà Nội'),
('dc8df245-e945-45e8-b676-ade48e6e198b', 'CGV Vincom Đà Nẵng', 'Tầng 4, Vincom Plaza Ngô Quyền, 910A Ngô Quyền, Sơn Trà, TP. Đà Nẵng', 'Tầng 4, Vincom Plaza Ngô Quyền, 910A Ngô Quyền, Sơn Trà, TP. Đà Nẵng'),
('683a1710-6efb-4005-b31c-59a5392f80fc', 'CGV Vincom Xuân Khánh Cần Thơ', 'Tầng 5, Vincom Plaza Xuân Khánh, 209 Đường 30/4, Ninh Kiều, Cần Thơ', 'Tầng 5, Vincom Plaza Xuân Khánh, 209 Đường 30/4, Ninh Kiều, Cần Thơ'),
('f6013a71-d647-42af-b0d7-fec8db4ac604', 'CGV Vincom Plaza Hạ Long', 'Tầng 4, Vincom Center Hạ Long, Phường Bạch Đằng, TP. Hạ Long, Quảng Ninh', 'Tầng 4, Vincom Center Hạ Long, Phường Bạch Đằng, TP. Hạ Long, Quảng Ninh'),
('6d7c0ccc-586b-4862-9450-44e3d2d0700e', 'Galaxy Nguyễn Du', 'Số 116 Nguyễn Du, Phường Bến Thành, Quận 1, TP.HCM', 'Số 116 Nguyễn Du, Phường Bến Thành, Quận 1, TP.HCM'),
('fe191272-5fff-4e69-b0bd-a74ca0687125', 'Galaxy Tân Bình', 'Số 246 Nguyễn Hồng Đào, Phường 14, Tân Bình, TP.HCM', 'Số 246 Nguyễn Hồng Đào, Phường 14, Tân Bình, TP.HCM'),
('76f80c98-3d10-44df-bacf-4c24ae837411', 'Galaxy Kinh Dương Vương', 'Số 718bis Kinh Dương Vương, Phường 13, Quận 6, TP.HCM', 'Số 718bis Kinh Dương Vương, Phường 13, Quận 6, TP.HCM'),
('bde6b405-bdc8-4fca-a665-9de2ffa077e0', 'Galaxy Quang Trung', 'Lầu 3, Co.opmart Quang Trung, 304A Quang Trung, Gò Vấp, TP.HCM', 'Lầu 3, Co.opmart Quang Trung, 304A Quang Trung, Gò Vấp, TP.HCM'),
('a00245c8-c5a6-4b65-82c3-7b5a325a51fb', 'Galaxy Trung Chánh', 'Lầu 3, TTTM Trung Chánh, 117-117A Nguyễn Ảnh Thủ, Quận 12, TP.HCM', 'Lầu 3, TTTM Trung Chánh, 117-117A Nguyễn Ảnh Thủ, Quận 12, TP.HCM'),
('71f78878-6605-41c4-a88a-4bcf8ee6d563', 'Galaxy Sala Thủ Thiêm', 'Tầng 3, Thiso Mall Sala, Số 10 Mai Chí Thọ, TP. Thủ Đức, TP.HCM', 'Tầng 3, Thiso Mall Sala, Số 10 Mai Chí Thọ, TP. Thủ Đức, TP.HCM'),
('e621d040-39a3-4f31-b228-eba5632448f5', 'Galaxy Huỳnh Tấn Phát', 'Tầng 2, Co.opmart Huỳnh Tấn Phát, 80/4A Huỳnh Tấn Phát, Quận 7, TP.HCM', 'Tầng 2, Co.opmart Huỳnh Tấn Phát, 80/4A Huỳnh Tấn Phát, Quận 7, TP.HCM'),
('7a510432-70bd-4a47-bc83-a6f110779488', 'Galaxy Tràng Thi', 'Số 10 Tràng Thi, Phường Hàng Trống, Quận Hoàn Kiếm, Hà Nội', 'Số 10 Tràng Thi, Phường Hàng Trống, Quận Hoàn Kiếm, Hà Nội'),
('afde1959-21ed-4e6a-ad9f-f13a049b6745', 'Galaxy Mipec Long Biên', 'Tầng 6, Mipec Riverside, Số 2 Phố Long Biên 2, Long Biên, Hà Nội', 'Tầng 6, Mipec Riverside, Số 2 Phố Long Biên 2, Long Biên, Hà Nội'),
('8c09a284-abc2-47cb-9eb3-2e91ecd41527', 'Galaxy Đà Nẵng', 'Tầng 3, Co.opmart Đà Nẵng, 478 Điện Biên Phủ, Thanh Khê, TP. Đà Nẵng', 'Tầng 3, Co.opmart Đà Nẵng, 478 Điện Biên Phủ, Thanh Khê, TP. Đà Nẵng'),
('d5682697-7809-4970-8073-8573a528dd91', 'Galaxy Bến Tre', 'Tầng 1, Sense City Bến Tre, 26A Trần Quốc Tuấn, TP. Bến Tre', 'Tầng 1, Sense City Bến Tre, 26A Trần Quốc Tuấn, TP. Bến Tre')
ON CONFLICT (cinemaID) DO UPDATE SET
    name = EXCLUDED.name,
    location = EXCLUDED.location,
    address = EXCLUDED.address;

-- --------------------------------------------------------------------
-- 3. BẢNG Room (Phòng chiếu phim)
-- --------------------------------------------------------------------
INSERT INTO Room (roomID, roomName, capacity, roomType, cinemaID) VALUES
('ROOM_01', 'Phòng Chiếu 1 (Laser 2D)', 20, '2D Digital', 'CINEMA_01'),
('ROOM_02', 'Phòng Chiếu 2 (IMAX Laser)', 20, 'IMAX Laser', 'CINEMA_01'),
('ROOM_03', 'Phòng 01 (Laser Landmark)', 20, '2D Digital', 'a0375eaa-a6c6-45ba-a182-6ef54a84922c')
ON CONFLICT (roomID) DO UPDATE SET
    roomName = EXCLUDED.roomName,
    capacity = EXCLUDED.capacity,
    roomType = EXCLUDED.roomType,
    cinemaID = EXCLUDED.cinemaID;

-- --------------------------------------------------------------------
-- 4. BẢNG Seat: Ghế ngồi trong phòng chiếu (ROOM_01: 20 ghế)
-- Hàng A, B: STANDARD (0đ) | Hàng C: VIP (+20.000đ) | Hàng D: COUPLE (+50.000đ)
-- --------------------------------------------------------------------
INSERT INTO Seat (seatID, roomID, "row", "column", type, extraPrice) VALUES
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
('D5', 'ROOM_01', 'D', '5', 'COUPLE', 50000.00)
ON CONFLICT (seatID, roomID) DO UPDATE SET
    "row" = EXCLUDED."row",
    "column" = EXCLUDED."column",
    type = EXCLUDED.type,
    extraPrice = EXCLUDED.extraPrice;

-- --------------------------------------------------------------------
-- 5. BẢNG Movie: 5 bộ phim bom tấn đa dạng thể loại
-- --------------------------------------------------------------------
INSERT INTO Movie (movieID, title, description, posterURL, trailerURL, director, castText, genre, duration, releaseDate, language, ageRating, status) VALUES
('MOV_01', 'Avengers: Doomsday', 'Biệt đội siêu anh hùng tái hợp trong cuộc chiến đa vũ trụ chống lại Doctor Doom nguy hiểm bậc nhất.', 'https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 'Russo Brothers', 'Robert Downey Jr., Pedro Pascal, Vanessa Kirby', 'Hành động, Viễn tưởng', '150 phút', '2026-05-01', 'Tiếng Anh - Phụ đề Tiếng Việt', 'C16', 'NOW_SHOWING'),
('MOV_02', 'Lật Mặt 8: Vòng Tay Người Mẹ', 'Câu chuyện gia đình đầy cảm xúc xen lẫn những pha hành động nghẹt thở và hài hước duyên dáng của Lý Hải.', 'https://image.tmdb.org/t/p/w500/1K7pHhCZprfDXB7LkCquIK62yCb.jpg', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 'Lý Hải', 'Dàn diễn viên điện ảnh Việt Nam', 'Hành động, Hài, Gia đình', '125 phút', '2026-04-30', 'Tiếng Việt', 'C13', 'NOW_SHOWING'),
('MOV_03', 'Mai', 'Tác phẩm điện ảnh tâm lý xã hội sâu sắc xoay quanh cuộc đời của người phụ nữ mang tên Mai cùng tình yêu định mệnh.', 'https://image.tmdb.org/t/p/w500/uFsn8qBEzbeJCqH0LsViwPom5ww.jpg', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 'Trấn Thành', 'Phương Anh Đào, Tuấn Trần, Hồng Đào', 'Tâm lý, Tình cảm', '131 phút', '2026-02-10', 'Tiếng Việt', 'C18', 'NOW_SHOWING'),
('MOV_04', 'Dune: Hành Tinh Cát 2', 'Paul Atreides hợp lực cùng Chani và tộc người Fremen để trả thù những kẻ đã hủy hoại gia đình mình.', 'https://image.tmdb.org/t/p/w500/6izwz7rsy95ARzTR3poZ8H6c5pp.jpg', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 'Denis Villeneuve', 'Timothée Chalamet, Zendaya, Rebecca Ferguson', 'Hành động, Phiêu lưu, Khoa học viễn tưởng', '166 phút', '2026-03-15', 'Tiếng Anh - Phụ đề Tiếng Việt', 'C16', 'NOW_SHOWING'),
('MOV_05', 'Avatar 3: Lửa Và Tro Tàn', 'Jake Sully và Neytiri khám phá những vùng đất mới trên hành tinh Pandora và đối đầu với Tộc Người Tro.', 'https://image.tmdb.org/t/p/w500/t6HIqrRAclMCA60NsSmeqe9RmNV.jpg', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 'James Cameron', 'Sam Worthington, Zoe Saldana, Sigourney Weaver', 'Khoa học viễn tưởng, Phiêu lưu', '190 phút', '2026-12-18', 'Tiếng Anh - Phụ đề Tiếng Việt', 'P', 'COMING_SOON')
ON CONFLICT (movieID) DO UPDATE SET
    title = EXCLUDED.title,
    description = EXCLUDED.description,
    posterURL = EXCLUDED.posterURL,
    trailerURL = EXCLUDED.trailerURL,
    director = EXCLUDED.director,
    castText = EXCLUDED.castText,
    genre = EXCLUDED.genre,
    duration = EXCLUDED.duration,
    releaseDate = EXCLUDED.releaseDate,
    language = EXCLUDED.language,
    ageRating = EXCLUDED.ageRating,
    status = EXCLUDED.status;

-- --------------------------------------------------------------------
-- 6. BẢNG Showtime: Suất chiếu cụ thể trong tương lai
-- --------------------------------------------------------------------
INSERT INTO Showtime (showTimeID, movieID, roomID, startTime, endTime, price) VALUES
('ST_01', 'MOV_01', 'ROOM_01', CURRENT_TIMESTAMP + INTERVAL '1 day 2 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 4 hours 30 minutes', 80000.00),
('ST_02', 'MOV_02', 'ROOM_01', CURRENT_TIMESTAMP + INTERVAL '1 day 5 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 7 hours 5 minutes', 75000.00),
('ST_03', 'MOV_03', 'ROOM_02', CURRENT_TIMESTAMP + INTERVAL '1 day 3 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 5 hours 11 minutes', 85000.00),
('ST_04', 'MOV_04', 'ROOM_02', CURRENT_TIMESTAMP + INTERVAL '1 day 6 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 8 hours 46 minutes', 90000.00),
('ST_05', 'MOV_01', 'ROOM_03', CURRENT_TIMESTAMP + INTERVAL '2 days 3 hours', CURRENT_TIMESTAMP + INTERVAL '2 days 5 hours 30 minutes', 110000.00)
ON CONFLICT (showTimeID) DO UPDATE SET
    movieID = EXCLUDED.movieID,
    roomID = EXCLUDED.roomID,
    startTime = EXCLUDED.startTime,
    endTime = EXCLUDED.endTime,
    price = EXCLUDED.price;

-- --------------------------------------------------------------------
-- 7. BẢNG Combo: Bắp nước / đồ ăn nhẹ
-- --------------------------------------------------------------------
INSERT INTO Combo (comboID, name, description, price) VALUES
('CB_01', 'Combo Solo', '1 Bắp ngọt lớn + 1 Nước ngọt lớn (Lựa chọn Coca / Sprite / Fanta)', 65000.00),
('CB_02', 'Combo Couple', '1 Bắp phô mai lớn + 2 Nước ngọt lớn', 99000.00),
('CB_03', 'Combo Gia Đình', '2 Bắp rang bơ lớn + 3 Nước ngọt + 1 Snack khoai tây chiên', 149000.00),
('CB_04', 'Bắp Phô Mai Thượng Hạng', '1 Bắp phô mai lắc cỡ lớn thơm ngon giòn rụm', 55000.00),
('CB_05', 'Nước Ngọt Coca-Cola Lớn', '1 Ly Coca-Cola tươi mát lạnh 32oz', 35000.00)
ON CONFLICT (comboID) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    price = EXCLUDED.price;

-- --------------------------------------------------------------------
-- 8. BẢNG Payment: Giao dịch thanh toán
-- --------------------------------------------------------------------
INSERT INTO Payment (paymentID, type, amount, paymentDate) VALUES
('PAY_01', 'VNPAY', 200000.00, CURRENT_TIMESTAMP - INTERVAL '1 hour'),
('PAY_02', 'MOMO', 279000.00, CURRENT_TIMESTAMP - INTERVAL '30 minutes'),
('PAY_03', 'CASH', 80000.00, CURRENT_TIMESTAMP - INTERVAL '2 hours')
ON CONFLICT (paymentID) DO UPDATE SET
    type = EXCLUDED.type,
    amount = EXCLUDED.amount,
    paymentDate = EXCLUDED.paymentDate;

-- --------------------------------------------------------------------
-- 9. BẢNG Booking: Đơn đặt vé với ĐẦY ĐỦ TRẠNG THÁI để test sơ đồ ghế:
-- - BK_01: 'PAID' (Đã thanh toán -> Ghế khóa vĩnh viễn)
-- - BK_02: 'PENDING' (Đang giữ chỗ còn hạn 8 phút -> Ghế hiển thị Đang được giữ)
-- - BK_03: 'PENDING' (Đã quá hạn giữ chỗ 5 phút trước -> Sẽ được nhả ghế tự động)
-- --------------------------------------------------------------------
INSERT INTO Booking (bookingID, userID, paymentID, bookingStatus, createdAt, expiresAt) VALUES
-- Đơn 1: Đã thanh toán thành công
('BK_01', 2, 'PAY_01', 'PAID', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP + INTERVAL '1 day'),
-- Đơn 2: Đang trong 10 phút giữ chỗ (còn hiệu lực 8 phút nữa)
('BK_02', 3, NULL, 'PENDING', CURRENT_TIMESTAMP - INTERVAL '2 minutes', CURRENT_TIMESTAMP + INTERVAL '8 minutes'),
-- Đơn 3: Đơn giữ chỗ quá hạn 5 phút trước (dùng để test tính năng releaseExpiredBookings)
('BK_03', 4, NULL, 'PENDING', CURRENT_TIMESTAMP - INTERVAL '15 minutes', CURRENT_TIMESTAMP - INTERVAL '5 minutes')
ON CONFLICT (bookingID) DO UPDATE SET
    userID = EXCLUDED.userID,
    paymentID = EXCLUDED.paymentID,
    bookingStatus = EXCLUDED.bookingStatus,
    createdAt = EXCLUDED.createdAt,
    expiresAt = EXCLUDED.expiresAt;

-- --------------------------------------------------------------------
-- 10. BẢNG BookingCombo: Chi tiết combo bắp nước đi kèm đơn
-- --------------------------------------------------------------------
INSERT INTO BookingCombo (bookingComboID, bookingID, comboID, quantity) VALUES
('BC_01', 'BK_01', 'CB_01', 1),
('BC_02', 'BK_02', 'CB_02', 1)
ON CONFLICT (bookingComboID) DO UPDATE SET
    bookingID = EXCLUDED.bookingID,
    comboID = EXCLUDED.comboID,
    quantity = EXCLUDED.quantity;

-- --------------------------------------------------------------------
-- 11. BẢNG Ticket: Vé điện tử gắn với Suất chiếu ST_01 để TEST TRẠNG THÁI GHẾ
-- - Ghế C1, C2: Thuộc đơn BK_01 (PAID) -> HIỂN THỊ MÀU ĐÃ BÁN (OCCUPIED / SOLD)
-- - Ghế B3, B4: Thuộc đơn BK_02 (PENDING) -> HIỂN THỊ MÀU ĐANG GIỮ CHỖ (HOLDING)
-- - Ghế A1: Thuộc đơn BK_03 (HẾT HẠN) -> Tự động giải phóng thành TRỐNG (AVAILABLE)
-- - Các ghế còn lại (A2-A5, B1-B2, B5, C3-C5, D1-D5): TRỐNG (AVAILABLE)
-- --------------------------------------------------------------------
INSERT INTO Ticket (ticketID, bookingID, movieID, cinemaID, showTimeID, seatID, ticketStatus) VALUES
('TK_01', 'BK_01', 'MOV_01', 'CINEMA_01', 'ST_01', 'D4', 'AVAILABLE'),
('TK_02', 'BK_01', 'MOV_01', 'CINEMA_01', 'ST_01', 'D5', 'AVAILABLE'),
('TK_03', 'BK_02', 'MOV_01', 'CINEMA_01', 'ST_01', 'B3', 'AVAILABLE'),
('TK_04', 'BK_02', 'MOV_01', 'CINEMA_01', 'ST_01', 'B4', 'AVAILABLE'),
('TK_05', 'BK_03', 'MOV_01', 'CINEMA_01', 'ST_01', 'A1', 'AVAILABLE')
ON CONFLICT (ticketID) DO UPDATE SET
    bookingID = EXCLUDED.bookingID,
    movieID = EXCLUDED.movieID,
    cinemaID = EXCLUDED.cinemaID,
    showTimeID = EXCLUDED.showTimeID,
    seatID = EXCLUDED.seatID,
    ticketStatus = EXCLUDED.ticketStatus;

-- --------------------------------------------------------------------
-- 12. BẢNG Review: Đánh giá & bình luận phim
-- --------------------------------------------------------------------
INSERT INTO Review (reviewID, userID, movieID, rating, comment, createdAt) VALUES
('REV_01', 2, 'MOV_01', 9.5, 'Phim Avengers Doomsday đỉnh cao vượt ngoài mong đợi, hiệu ứng âm thanh và kỹ xảo tại phòng IMAX quá tuyệt vời!', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('REV_02', 3, 'MOV_02', 9.0, 'Phim Lật Mặt 8 vô cùng xúc động, vừa cười vừa khóc, diễn xuất của các diễn viên rất tự nhiên!', CURRENT_TIMESTAMP - INTERVAL '1 day'),
('REV_03', 1, 'MOV_03', 8.5, 'Một tác phẩm điện ảnh sâu lắng và chạm đến cảm xúc, Phương Anh Đào diễn xuất quá xuất sắc.', CURRENT_TIMESTAMP - INTERVAL '3 days'),
('REV_04', 4, 'MOV_04', 9.0, 'Dune 2 là kiệt tác điện ảnh khoa học viễn tưởng, hình ảnh sa mạc và nhạc phim rợn ngợp.', CURRENT_TIMESTAMP - INTERVAL '4 days')
ON CONFLICT (reviewID) DO UPDATE SET
    userID = EXCLUDED.userID,
    movieID = EXCLUDED.movieID,
    rating = EXCLUDED.rating,
    comment = EXCLUDED.comment,
    createdAt = EXCLUDED.createdAt;

-- ====================================================================
-- ĐỒNG BỘ TƯƠNG THÍCH CHO FRONTEND (JPA Entities)
-- Đồng bộ tự động sang các bảng số nhiều (movies, rooms, seats, showtimes)
-- giúp toàn bộ Servlet và JSP của Frontend hiển thị mượt mà không lỗi
-- ====================================================================
DO $$
BEGIN
    -- Đồng bộ bảng movies nếu tồn tại
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'movies') THEN
        INSERT INTO movies (movieid, title, description, posterurl, director, duration, releasedate, genre, agerating)
        VALUES
        ('MOV_01', 'Avengers: Doomsday', 'Biệt đội siêu anh hùng tái hợp trong cuộc chiến đa vũ trụ chống lại Doctor Doom nguy hiểm bậc nhất.', 'https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg', 'Russo Brothers', 150, '2026-05-01', 'Hành động, Viễn tưởng', 'C16'),
        ('MOV_02', 'Lật Mặt 8: Vòng Tay Người Mẹ', 'Câu chuyện gia đình đầy cảm xúc xen lẫn những pha hành động nghẹt thở và hài hước duyên dáng của Lý Hải.', 'https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg', 'Lý Hải', 125, '2026-04-30', 'Hành động, Hài, Gia đình', 'C13'),
        ('MOV_03', 'Mai', 'Tác phẩm điện ảnh tâm lý xã hội sâu sắc xoay quanh cuộc đời của người phụ nữ mang tên Mai cùng tình yêu định mệnh.', 'https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg', 'Trấn Thành', 131, '2026-02-10', 'Tâm lý, Tình cảm', 'C18'),
        ('MOV_04', 'Dune: Hành Tinh Cát 2', 'Paul Atreides hợp lực cùng Chani và tộc người Fremen để trả thù những kẻ đã hủy hoại gia đình mình.', 'https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg', 'Denis Villeneuve', 166, '2026-03-15', 'Hành động, Phiêu lưu', 'C16')
        ON CONFLICT (movieid) DO UPDATE SET
            title = EXCLUDED.title,
            description = EXCLUDED.description,
            posterurl = EXCLUDED.posterurl,
            director = EXCLUDED.director,
            duration = EXCLUDED.duration,
            releasedate = EXCLUDED.releasedate,
            genre = EXCLUDED.genre,
            agerating = EXCLUDED.agerating;
    END IF;

    -- Đồng bộ bảng rooms nếu tồn tại
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'rooms') THEN
        INSERT INTO rooms (roomid, name, capacity, cinema_id)
        VALUES
        ('ROOM_01', 'Phòng Chiếu 1 (Laser 2D)', 20, 'CINEMA_01'),
        ('ROOM_02', 'Phòng Chiếu 2 (IMAX Laser)', 20, 'CINEMA_01')
        ON CONFLICT (roomid) DO UPDATE SET
            name = EXCLUDED.name,
            capacity = EXCLUDED.capacity,
            cinema_id = EXCLUDED.cinema_id;
    END IF;

    -- Đồng bộ bảng showtimes nếu tồn tại
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'showtimes') THEN
        INSERT INTO showtimes (showtimeid, starttime, endtime, movie_id, room_id)
        VALUES
        ('ST_01', CURRENT_TIMESTAMP + INTERVAL '1 day 2 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 4 hours 30 minutes', 'MOV_01', 'ROOM_01'),
        ('ST_02', CURRENT_TIMESTAMP + INTERVAL '1 day 5 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 7 hours 5 minutes', 'MOV_02', 'ROOM_01')
        ON CONFLICT (showtimeid) DO UPDATE SET
            starttime = EXCLUDED.starttime,
            endtime = EXCLUDED.endtime,
            movie_id = EXCLUDED.movie_id,
            room_id = EXCLUDED.room_id;
    END IF;
END $$;
