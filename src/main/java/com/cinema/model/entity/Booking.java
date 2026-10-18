package com.cinema.model.entity;

import com.cinema.model.enums.BookingStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    private String bookingID;
    private int userID;
    private String paymentID;
    private BookingStatus bookingStatus;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public Booking() {
        this.bookingStatus = BookingStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public Booking(String bookingID, int userID, String paymentID, BookingStatus bookingStatus, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.bookingID = bookingID;
        this.userID = userID;
        this.paymentID = paymentID;
        this.bookingStatus = bookingStatus != null ? bookingStatus : BookingStatus.PENDING;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    public Booking(String bookingID, int userID, String paymentID, String bookingStatusStr, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.bookingID = bookingID;
        this.userID = userID;
        this.paymentID = paymentID;
        try {
            this.bookingStatus = bookingStatusStr != null ? BookingStatus.valueOf(bookingStatusStr) : BookingStatus.PENDING;
        } catch (IllegalArgumentException e) {
            this.bookingStatus = BookingStatus.PENDING;
        }
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    public String getBookingID() {
        return bookingID;
    }

    public void setBookingID(String bookingID) {
        this.bookingID = bookingID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(String paymentID) {
        this.paymentID = paymentID;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public void setBookingStatus(String bookingStatusStr) {
        try {
            this.bookingStatus = bookingStatusStr != null ? BookingStatus.valueOf(bookingStatusStr) : BookingStatus.PENDING;
        } catch (IllegalArgumentException e) {
            this.bookingStatus = BookingStatus.PENDING;
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(bookingID, booking.bookingID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingID);
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingID='" + bookingID + '\'' +
                ", userID=" + userID +
                ", paymentID='" + paymentID + '\'' +
                ", bookingStatus=" + bookingStatus +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
