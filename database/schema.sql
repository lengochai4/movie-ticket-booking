-- ====================================================================
-- DATABASE SCHEMA: Cinema Booking System (PostgreSQL - Cloud Render)
-- Nhóm 3 - Đồ án Web Đặt Vé Xem Phim
-- Schema chuẩn cho DB Oregon dùng chung (JPA EclipseLink & JDBC)
-- Tên bảng: chữ thường, số nhiều
-- ====================================================================

-- 1. Bảng users: Tài khoản người dùng và quản trị viên
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(50) PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'USER', -- USER, ADMIN, STAFF
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, LOCKED
    loyalty_points INT DEFAULT 0,
    is_email_verified BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(255),
    token_expiry TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bảng loyalty_accounts: Quản lý điểm thưởng tích lũy thành viên
CREATE TABLE IF NOT EXISTS loyalty_accounts (
    userid VARCHAR(50) PRIMARY KEY,
    points INT NOT NULL DEFAULT 0,
    updatedat TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_loyalty_user FOREIGN KEY (userid) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. Bảng cinema: Cụm rạp chiếu phim toàn quốc
CREATE TABLE IF NOT EXISTS cinema (
    cinemaid VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL
);

-- 4. Bảng rooms: Phòng chiếu phim trực thuộc rạp
CREATE TABLE IF NOT EXISTS rooms (
    roomid VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    cinema_id VARCHAR(50) NOT NULL,
    CONSTRAINT fk_rooms_cinema FOREIGN KEY (cinema_id) REFERENCES cinema(cinemaid) ON DELETE CASCADE
);

-- 5. Bảng seats: Ghế ngồi trong phòng chiếu
CREATE TABLE IF NOT EXISTS seats (
    seatid VARCHAR(50) PRIMARY KEY,
    room_id VARCHAR(50) NOT NULL,
    seatcode VARCHAR(20) NOT NULL,
    seatrow VARCHAR(10) NOT NULL,
    seatcol INT NOT NULL,
    seattype VARCHAR(50) DEFAULT 'STANDARD', -- STANDARD, VIP, COUPLE, SWEETBOX
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, MAINTENANCE
    CONSTRAINT fk_seats_room FOREIGN KEY (room_id) REFERENCES rooms(roomid) ON DELETE CASCADE
);

-- 6. Bảng movies: Thông tin phim
CREATE TABLE IF NOT EXISTS movies (
    movieid VARCHAR(50) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    posterurl VARCHAR(1000),
    trailerurl VARCHAR(1000),
    director VARCHAR(255),
    genre VARCHAR(100),
    duration INT, -- Số phút
    releasedate DATE,
    agerating VARCHAR(50) -- P, K, T13, T16, T18, C
);

-- 7. Bảng showtimes: Suất chiếu cụ thể theo phim và phòng
CREATE TABLE IF NOT EXISTS showtimes (
    showtimeid VARCHAR(50) PRIMARY KEY,
    movie_id VARCHAR(50) NOT NULL,
    room_id VARCHAR(50) NOT NULL,
    starttime TIMESTAMP NOT NULL,
    endtime TIMESTAMP NOT NULL,
    price DOUBLE PRECISION DEFAULT 80000,
    CONSTRAINT fk_showtimes_movie FOREIGN KEY (movie_id) REFERENCES movies(movieid) ON DELETE CASCADE,
    CONSTRAINT fk_showtimes_room FOREIGN KEY (room_id) REFERENCES rooms(roomid) ON DELETE CASCADE
);

-- 8. Bảng combos: Bắp nước / Combo ẩm thực
CREATE TABLE IF NOT EXISTS combos (
    comboid VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(12, 2) NOT NULL,
    category VARCHAR(50), -- Combo, Bắp Rang, Nước, Đặc Biệt
    imageurl VARCHAR(1000),
    status VARCHAR(20) DEFAULT 'ACTIVE' -- ACTIVE, OUT_OF_STOCK
);

-- 9. Bảng bookings: Đơn đặt vé giữ chỗ và thanh toán
CREATE TABLE IF NOT EXISTS bookings (
    bookingid VARCHAR(50) PRIMARY KEY,
    ordercode VARCHAR(50) NOT NULL UNIQUE,
    userid VARCHAR(50),
    customername VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(255),
    showtimeid VARCHAR(50),
    bookingstatus VARCHAR(50) DEFAULT 'PENDING', -- PENDING, PAID, CANCELLED
    totalprice DOUBLE PRECISION DEFAULT 0,
    ticketprice DOUBLE PRECISION DEFAULT 0,
    comboprice DOUBLE PRECISION DEFAULT 0,
    combos TEXT,
    seats VARCHAR(500),
    pointsused INT DEFAULT 0,
    pointsearned INT DEFAULT 0,
    pointsreversed BOOLEAN DEFAULT FALSE,
    movietitle VARCHAR(255),
    posterurl VARCHAR(1000),
    cinemaname VARCHAR(255),
    cinemaaddress VARCHAR(500),
    roomname VARCHAR(100),
    movieformat VARCHAR(50) DEFAULT '2D Digital',
    showstart TIMESTAMP,
    agerating VARCHAR(50),
    cancelreason VARCHAR(500),
    cancelledat TIMESTAMP,
    createdat TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. Bảng tickets: Vé điện tử tương ứng từng ghế
CREATE TABLE IF NOT EXISTS tickets (
    ticketid VARCHAR(50) PRIMARY KEY,
    bookingid VARCHAR(50) NOT NULL,
    seatid VARCHAR(50),
    seatlabel VARCHAR(50),
    price DOUBLE PRECISION DEFAULT 0,
    ticketstatus VARCHAR(50) DEFAULT 'HOLD', -- HOLD, BOOKED, USED, CANCELLED
    CONSTRAINT fk_tickets_booking FOREIGN KEY (bookingid) REFERENCES bookings(bookingid) ON DELETE CASCADE
);

-- 11. Bảng reviews: Đánh giá phim của khán giả
CREATE TABLE IF NOT EXISTS reviews (
    id VARCHAR(50) PRIMARY KEY,
    movie_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50),
    user_name VARCHAR(255),
    rating INT NOT NULL,
    comment TEXT,
    helpful_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_movie FOREIGN KEY (movie_id) REFERENCES movies(movieid) ON DELETE CASCADE
);
