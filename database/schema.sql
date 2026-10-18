CREATE DATABASE IF NOT EXISTS cinema_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cinema_db;

-- 1. User: Tai khoan nguoi dung va quan tri vien
CREATE TABLE IF NOT EXISTS User (
    userID INT AUTO_INCREMENT PRIMARY KEY,
    fullName VARCHAR(255) NOT NULL,
    passwordHash VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    role INT DEFAULT 0, -- 0: User, 1: Admin
    loyaltyPoints INT DEFAULT 0,
    isLocked TINYINT(1) DEFAULT 0 -- 0: active, 1: locked
);

-- 2. Cinema: Rap chieu phim
CREATE TABLE IF NOT EXISTS Cinema (
    cinemaID VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL
);

-- 3. Room: Phong chieu truc thuoc rap
CREATE TABLE IF NOT EXISTS Room (
    roomID VARCHAR(50) PRIMARY KEY,
    roomName VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    roomType VARCHAR(50), -- 2D, 3D, IMAX
    cinemaID VARCHAR(50) NOT NULL,
    FOREIGN KEY (cinemaID) REFERENCES Cinema(cinemaID) ON DELETE CASCADE
);

-- 4. Seat: Ghe ngoi trong tung phong chieu
CREATE TABLE IF NOT EXISTS Seat (
    seatID VARCHAR(50) NOT NULL, -- Vi du: A1, A2, B1
    roomID VARCHAR(50) NOT NULL,
    `row` VARCHAR(10) NOT NULL,
    `column` VARCHAR(10) NOT NULL,
    `type` VARCHAR(50) DEFAULT 'STANDARD', -- STANDARD, VIP, COUPLE
    extraPrice DECIMAL(10, 2) DEFAULT 0.00,
    PRIMARY KEY (seatID, roomID),
    FOREIGN KEY (roomID) REFERENCES Room(roomID) ON DELETE CASCADE
);

-- 5. Movie: Thong tin bo phim
CREATE TABLE IF NOT EXISTS Movie (
    movieID VARCHAR(50) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    posterURL VARCHAR(500),
    trailerURL VARCHAR(500),
    director VARCHAR(255),
    cast TEXT,
    genre VARCHAR(100),
    duration VARCHAR(50),
    releaseDate DATE,
    language VARCHAR(100),
    ageRating VARCHAR(50), -- P, C13, C16, C18
    status VARCHAR(50) -- NOW_SHOWING, COMING_SOON
);

-- 6. Showtime: Suat chieu cu the
CREATE TABLE IF NOT EXISTS Showtime (
    showTimeID VARCHAR(50) PRIMARY KEY,
    movieID VARCHAR(50) NOT NULL,
    roomID VARCHAR(50) NOT NULL,
    startTime DATETIME NOT NULL,
    endTime DATETIME NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (movieID) REFERENCES Movie(movieID) ON DELETE CASCADE,
    FOREIGN KEY (roomID) REFERENCES Room(roomID) ON DELETE CASCADE
);

-- 7. Combo: Combo bap nuoc an uong
CREATE TABLE IF NOT EXISTS Combo (
    comboID VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL
);

-- 8. Payment: Thong tin thanh toan
CREATE TABLE IF NOT EXISTS Payment (
    paymentID VARCHAR(50) PRIMARY KEY,
    `type` VARCHAR(50) NOT NULL, -- CASH, VNPAY, MOMO
    amount DECIMAL(10, 2) NOT NULL,
    paymentDate DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 9. Booking: Don dat ve giu cho
CREATE TABLE IF NOT EXISTS Booking (
    bookingID VARCHAR(50) PRIMARY KEY,
    userID INT NOT NULL,
    paymentID VARCHAR(50) NULL,
    bookingStatus VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, PAID, CANCELLED
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    expiresAt DATETIME NOT NULL,
    FOREIGN KEY (userID) REFERENCES User(userID) ON DELETE CASCADE,
    FOREIGN KEY (paymentID) REFERENCES Payment(paymentID) ON DELETE SET NULL
);

-- 10. BookingCombo: Chi tiet combo di kem don booking
CREATE TABLE IF NOT EXISTS BookingCombo (
    bookingComboID VARCHAR(50) PRIMARY KEY,
    bookingID VARCHAR(50) NOT NULL,
    comboID VARCHAR(50) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (bookingID) REFERENCES Booking(bookingID) ON DELETE CASCADE,
    FOREIGN KEY (comboID) REFERENCES Combo(comboID) ON DELETE CASCADE
);

-- 11. Ticket: Ve dien tu cu the theo tung ghe
CREATE TABLE IF NOT EXISTS Ticket (
    ticketID VARCHAR(50) PRIMARY KEY,
    bookingID VARCHAR(50) NOT NULL,
    movieID VARCHAR(50) NOT NULL,
    cinemaID VARCHAR(50) NOT NULL,
    showTimeID VARCHAR(50) NOT NULL,
    seatID VARCHAR(50) NOT NULL,
    ticketStatus VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, USED, CANCELLED
    FOREIGN KEY (bookingID) REFERENCES Booking(bookingID) ON DELETE CASCADE,
    FOREIGN KEY (movieID) REFERENCES Movie(movieID),
    FOREIGN KEY (cinemaID) REFERENCES Cinema(cinemaID),
    FOREIGN KEY (showTimeID) REFERENCES Showtime(showTimeID)
);

-- 12. Review: Danh gia va binh luan phim
CREATE TABLE IF NOT EXISTS Review (
    reviewID VARCHAR(50) PRIMARY KEY,
    userID INT NOT NULL,
    movieID VARCHAR(50) NOT NULL,
    rating FLOAT NOT NULL,
    comment TEXT,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userID) REFERENCES User(userID) ON DELETE CASCADE,
    FOREIGN KEY (movieID) REFERENCES Movie(movieID) ON DELETE CASCADE
);
