-- ====================================================================
-- DATABASE SCHEMA: Cinema Booking System (PostgreSQL)
-- Phụ trách: Hải (Backend Developer)
-- ====================================================================

-- 0. Xóa bảng cũ theo thứ tự ràng buộc khóa ngoại (nếu có)
DROP TABLE IF EXISTS Review CASCADE;
DROP TABLE IF EXISTS Ticket CASCADE;
DROP TABLE IF EXISTS BookingCombo CASCADE;
DROP TABLE IF EXISTS Booking CASCADE;
DROP TABLE IF EXISTS Payment CASCADE;
DROP TABLE IF EXISTS Combo CASCADE;
DROP TABLE IF EXISTS Showtime CASCADE;
DROP TABLE IF EXISTS Movie CASCADE;
DROP TABLE IF EXISTS Seat CASCADE;
DROP TABLE IF EXISTS Room CASCADE;
DROP TABLE IF EXISTS Cinema CASCADE;
DROP TABLE IF EXISTS "User" CASCADE;

-- 1. Bảng User: Tài khoản người dùng và quản trị viên
-- Lưu ý: "User" đặt trong dấu nháy kép vì USER là từ khóa hệ thống của PostgreSQL
CREATE TABLE IF NOT EXISTS "User" (
    userID SERIAL PRIMARY KEY,
    fullName VARCHAR(255) NOT NULL,
    passwordHash VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    role INT DEFAULT 0, -- 0: User, 1: Admin
    loyaltyPoints INT DEFAULT 0
);

-- 2. Bảng Cinema: Cụm rạp chiếu phim
CREATE TABLE IF NOT EXISTS Cinema (
    cinemaID VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    address VARCHAR(500)
);

-- 3. Bảng Room: Phòng chiếu phim trực thuộc rạp
CREATE TABLE IF NOT EXISTS Room (
    roomID VARCHAR(50) PRIMARY KEY,
    roomName VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    roomType VARCHAR(50), -- 2D, 3D, IMAX
    cinemaID VARCHAR(50) NOT NULL,
    CONSTRAINT fk_room_cinema FOREIGN KEY (cinemaID) REFERENCES Cinema(cinemaID) ON DELETE CASCADE
);

-- 4. Bảng Seat: Ghế ngồi trong phòng chiếu
-- "row" và "column" đặt trong dấu nháy kép vì là từ khóa trong PostgreSQL
CREATE TABLE IF NOT EXISTS Seat (
    seatID VARCHAR(50) NOT NULL,
    roomID VARCHAR(50) NOT NULL,
    "row" VARCHAR(10) NOT NULL,
    "column" VARCHAR(10) NOT NULL,
    type VARCHAR(50) DEFAULT 'STANDARD', -- STANDARD, VIP, COUPLE
    extraPrice NUMERIC(10, 2) DEFAULT 0.00,
    PRIMARY KEY (seatID, roomID),
    CONSTRAINT fk_seat_room FOREIGN KEY (roomID) REFERENCES Room(roomID) ON DELETE CASCADE
);

-- 5. Bảng Movie: Thông tin bộ phim
CREATE TABLE IF NOT EXISTS Movie (
    movieID VARCHAR(50) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    posterURL VARCHAR(500),
    trailerURL VARCHAR(500),
    director VARCHAR(255),
    castText TEXT,
    genre VARCHAR(100),
    duration VARCHAR(50),
    releaseDate DATE,
    language VARCHAR(100),
    ageRating VARCHAR(50), -- P, C13, C16, C18
    status VARCHAR(50)     -- NOW_SHOWING, COMING_SOON
);

-- 6. Bảng Showtime: Suất chiếu cụ thể theo phim và phòng
CREATE TABLE IF NOT EXISTS Showtime (
    showTimeID VARCHAR(50) PRIMARY KEY,
    movieID VARCHAR(50) NOT NULL,
    roomID VARCHAR(50) NOT NULL,
    startTime TIMESTAMP NOT NULL,
    endTime TIMESTAMP NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    CONSTRAINT fk_showtime_movie FOREIGN KEY (movieID) REFERENCES Movie(movieID) ON DELETE CASCADE,
    CONSTRAINT fk_showtime_room FOREIGN KEY (roomID) REFERENCES Room(roomID) ON DELETE CASCADE
);

-- 7. Bảng Combo: Bắp nước / đồ ăn nhẹ
CREATE TABLE IF NOT EXISTS Combo (
    comboID VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10, 2) NOT NULL
);

-- 8. Bảng Payment: Thông tin giao dịch thanh toán
CREATE TABLE IF NOT EXISTS Payment (
    paymentID VARCHAR(50) PRIMARY KEY,
    type VARCHAR(50), -- CASH, VNPAY, MOMO
    amount NUMERIC(10, 2),
    paymentDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. Bảng Booking: Đơn đặt vé giữ chỗ
CREATE TABLE IF NOT EXISTS Booking (
    bookingID VARCHAR(50) PRIMARY KEY,
    userID INT NOT NULL,
    paymentID VARCHAR(50),
    bookingStatus VARCHAR(50) DEFAULT 'PENDING', -- PENDING, PAID, CANCELLED
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiresAt TIMESTAMP NOT NULL,
    CONSTRAINT fk_booking_user FOREIGN KEY (userID) REFERENCES "User"(userID) ON DELETE CASCADE,
    CONSTRAINT fk_booking_payment FOREIGN KEY (paymentID) REFERENCES Payment(paymentID) ON DELETE SET NULL
);

-- 10. Bảng BookingCombo: Chi tiết combo theo đơn đặt vé
CREATE TABLE IF NOT EXISTS BookingCombo (
    bookingComboID VARCHAR(50) PRIMARY KEY,
    bookingID VARCHAR(50) NOT NULL,
    comboID VARCHAR(50) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_bc_booking FOREIGN KEY (bookingID) REFERENCES Booking(bookingID) ON DELETE CASCADE,
    CONSTRAINT fk_bc_combo FOREIGN KEY (comboID) REFERENCES Combo(comboID) ON DELETE CASCADE
);

-- 11. Bảng Ticket: Vé điện tử cho từng ghế
CREATE TABLE IF NOT EXISTS Ticket (
    ticketID VARCHAR(50) PRIMARY KEY,
    bookingID VARCHAR(50) NOT NULL,
    movieID VARCHAR(50),
    cinemaID VARCHAR(50),
    showTimeID VARCHAR(50),
    seatID VARCHAR(50),
    ticketStatus VARCHAR(50) DEFAULT 'AVAILABLE', -- AVAILABLE, USED, CANCELLED
    CONSTRAINT fk_ticket_booking FOREIGN KEY (bookingID) REFERENCES Booking(bookingID) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_movie FOREIGN KEY (movieID) REFERENCES Movie(movieID),
    CONSTRAINT fk_ticket_cinema FOREIGN KEY (cinemaID) REFERENCES Cinema(cinemaID),
    CONSTRAINT fk_ticket_showtime FOREIGN KEY (showTimeID) REFERENCES Showtime(showTimeID)
);

-- 12. Bảng Review: Đánh giá và bình luận phim
CREATE TABLE IF NOT EXISTS Review (
    reviewID VARCHAR(50) PRIMARY KEY,
    userID INT NOT NULL,
    movieID VARCHAR(50) NOT NULL,
    rating FLOAT NOT NULL,
    comment TEXT,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_user FOREIGN KEY (userID) REFERENCES "User"(userID) ON DELETE CASCADE,
    CONSTRAINT fk_review_movie FOREIGN KEY (movieID) REFERENCES Movie(movieID) ON DELETE CASCADE
);
