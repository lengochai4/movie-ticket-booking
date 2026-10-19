-- ====================================================================
-- DỮ LIỆU MẪU ĐẦY ĐỦ: Cinema Booking System (PostgreSQL - Cloud Render)
-- Áp dụng cho cơ sở dữ liệu chung Oregon
-- Tên bảng: chữ thường, số nhiều
-- ====================================================================

-- --------------------------------------------------------------------
-- 1. BẢNG users (Tài khoản người dùng & Admin)
-- Mật khẩu hash demo bằng BCrypt tương ứng với: 123456
-- --------------------------------------------------------------------
INSERT INTO users (user_id, full_name, password, email, phone, role, status, loyalty_points) VALUES
('usr-admin-01', 'Admin Cinema', '$2a$10$abcdefghijklmnopqrstuvwxyzA1B2C3D4E5F6G7H8I9J0K', 'admin@cinema.com', '0901234567', 'ADMIN', 'ACTIVE', 500),
('usr-hai-02', 'Lê Ngọc Hải', '$2a$10$abcdefghijklmnopqrstuvwxyzA1B2C3D4E5F6G7H8I9J0K', 'hai@gmail.com', '0987654321', 'USER', 'ACTIVE', 150),
('usr-phuong-03', 'Trần Thị Phương', '$2a$10$abcdefghijklmnopqrstuvwxyzA1B2C3D4E5F6G7H8I9J0K', 'phuong@gmail.com', '0912345678', 'USER', 'ACTIVE', 80),
('usr-an-04', 'Nguyễn Văn An', '$2a$10$abcdefghijklmnopqrstuvwxyzA1B2C3D4E5F6G7H8I9J0K', 'an.nguyen@gmail.com', '0933445566', 'USER', 'ACTIVE', 20)
ON CONFLICT (user_id) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    email = EXCLUDED.email,
    phone = EXCLUDED.phone,
    role = EXCLUDED.role,
    loyalty_points = EXCLUDED.loyalty_points;

-- Đồng bộ điểm thưởng vào bảng loyalty_accounts
INSERT INTO loyalty_accounts (userid, points) VALUES
('usr-admin-01', 500),
('usr-hai-02', 150),
('usr-phuong-03', 80),
('usr-an-04', 20)
ON CONFLICT (userid) DO UPDATE SET
    points = EXCLUDED.points;

-- --------------------------------------------------------------------
-- 2. BẢNG cinema: Cụm rạp tiêu biểu
-- --------------------------------------------------------------------
INSERT INTO cinema (cinemaid, name, address) VALUES
('CINEMA_01', 'N3 Cinema Đồng Nai Center', '123 Phạm Văn Thuận, Biên Hòa, Đồng Nai'),
('a0375eaa-a6c6-45ba-a182-6ef54a84922c', 'CGV Vincom Landmark 81', 'Tầng B1, TTTM Vincom Center Landmark 81, 720A Điện Biên Phủ, Phường 22, Bình Thạnh, TP.HCM'),
('4cf67543-74a8-49e4-af6a-28f2fd47207e', 'Beta Cinemas Biên Hòa', 'Tầng 6, Trung tâm Pegasus Plaza, 53-55 Võ Thị Sáu, Quyết Thắng, Biên Hòa, Đồng Nai')
ON CONFLICT (cinemaid) DO UPDATE SET
    name = EXCLUDED.name,
    address = EXCLUDED.address;

-- --------------------------------------------------------------------
-- 3. BẢNG rooms (Phòng chiếu phim)
-- --------------------------------------------------------------------
INSERT INTO rooms (roomid, name, capacity, cinema_id) VALUES
('ROOM_01', 'Phòng Chiếu 1 (Laser 2D)', 20, 'CINEMA_01'),
('ROOM_02', 'Phòng Chiếu 2 (IMAX Laser)', 20, 'CINEMA_01')
ON CONFLICT (roomid) DO UPDATE SET
    name = EXCLUDED.name,
    capacity = EXCLUDED.capacity,
    cinema_id = EXCLUDED.cinema_id;

-- --------------------------------------------------------------------
-- 4. BẢNG seats: Ghế ngồi trong phòng chiếu
-- --------------------------------------------------------------------
INSERT INTO seats (seatid, room_id, seatcode, seatrow, seatcol, seattype, status) VALUES
('A1', 'ROOM_01', 'A1', 'A', 1, 'STANDARD', 'ACTIVE'),
('A2', 'ROOM_01', 'A2', 'A', 2, 'STANDARD', 'ACTIVE'),
('A3', 'ROOM_01', 'A3', 'A', 3, 'STANDARD', 'ACTIVE'),
('A4', 'ROOM_01', 'A4', 'A', 4, 'STANDARD', 'ACTIVE'),
('A5', 'ROOM_01', 'A5', 'A', 5, 'STANDARD', 'ACTIVE'),
('B1', 'ROOM_01', 'B1', 'B', 1, 'STANDARD', 'ACTIVE'),
('B2', 'ROOM_01', 'B2', 'B', 2, 'STANDARD', 'ACTIVE'),
('B3', 'ROOM_01', 'B3', 'B', 3, 'STANDARD', 'ACTIVE'),
('B4', 'ROOM_01', 'B4', 'B', 4, 'STANDARD', 'ACTIVE'),
('B5', 'ROOM_01', 'B5', 'B', 5, 'STANDARD', 'ACTIVE'),
('C1', 'ROOM_01', 'C1', 'C', 1, 'VIP', 'ACTIVE'),
('C2', 'ROOM_01', 'C2', 'C', 2, 'VIP', 'ACTIVE'),
('C3', 'ROOM_01', 'C3', 'C', 3, 'VIP', 'ACTIVE'),
('C4', 'ROOM_01', 'C4', 'C', 4, 'VIP', 'ACTIVE'),
('C5', 'ROOM_01', 'C5', 'C', 5, 'VIP', 'ACTIVE'),
('D1', 'ROOM_01', 'D1', 'D', 1, 'COUPLE', 'ACTIVE'),
('D2', 'ROOM_01', 'D2', 'D', 2, 'COUPLE', 'ACTIVE'),
('D3', 'ROOM_01', 'D3', 'D', 3, 'COUPLE', 'ACTIVE'),
('D4', 'ROOM_01', 'D4', 'D', 4, 'COUPLE', 'ACTIVE'),
('D5', 'ROOM_01', 'D5', 'D', 5, 'COUPLE', 'ACTIVE')
ON CONFLICT (seatid) DO UPDATE SET
    seatcode = EXCLUDED.seatcode,
    seatrow = EXCLUDED.seatrow,
    seatcol = EXCLUDED.seatcol,
    seattype = EXCLUDED.seattype,
    status = EXCLUDED.status;

-- --------------------------------------------------------------------
-- 5. BẢNG combos: Bắp nước mở bán
-- --------------------------------------------------------------------
INSERT INTO combos (comboid, name, description, price, category, imageurl, status) VALUES
('CB_01', 'Combo Solo Bắp Nước', '1 Bắp ngọt lớn (60oz) + 1 Nước ngọt có ga (22oz)', 79000.00, 'Combo', 'https://images.unsplash.com/photo-1572177812156-58036aae439c?w=500', 'ACTIVE'),
('CB_02', 'Combo Couple Đôi Bạn', '1 Bắp phô mai lớn (60oz) + 2 Nước ngọt có ga (22oz)', 119000.00, 'Combo', 'https://images.unsplash.com/photo-1585647347384-2593bc35786b?w=500', 'ACTIVE')
ON CONFLICT (comboid) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    price = EXCLUDED.price,
    category = EXCLUDED.category,
    imageurl = EXCLUDED.imageurl,
    status = EXCLUDED.status;
