package com.cinema.model.entity;

import com.cinema.model.enums.BookingStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    private String bookingID;
    private int userID;
    private String paymentID;
    private Payment payment;
    private List<Ticket> tickets;
    private List<BookingCombo> bookingCombos;
    private BookingStatus bookingStatus;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public Booking() {
        this.tickets = new ArrayList<>();
        this.bookingCombos = new ArrayList<>();
        this.bookingStatus = BookingStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public Booking(String bookingID, int userID, String paymentID, Payment payment,
                   List<Ticket> tickets, List<BookingCombo> bookingCombos,
                   BookingStatus bookingStatus, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.bookingID = bookingID;
        this.userID = userID;
        this.paymentID = paymentID;
        this.payment = payment;
        this.tickets = tickets != null ? tickets : new ArrayList<>();
        this.bookingCombos = bookingCombos != null ? bookingCombos : new ArrayList<>();
        this.bookingStatus = bookingStatus != null ? bookingStatus : BookingStatus.PENDING;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    public Booking(String bookingID, int userID, Payment payment,
                   List<Ticket> tickets, List<BookingCombo> bookingCombos,
                   BookingStatus bookingStatus, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this(bookingID, userID, payment != null ? payment.getPaymentID() : null, payment,
             tickets, bookingCombos, bookingStatus, createdAt, expiresAt);
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
        if (paymentID != null) {
            return paymentID;
        }
        return payment != null ? payment.getPaymentID() : null;
    }

    public void setPaymentID(String paymentID) {
        this.paymentID = paymentID;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
        if (payment != null) {
            this.paymentID = payment.getPaymentID();
        }
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets != null ? tickets : new ArrayList<>();
    }

    public List<BookingCombo> getBookingCombos() {
        return bookingCombos;
    }

    public void setBookingCombos(List<BookingCombo> bookingCombos) {
        this.bookingCombos = bookingCombos != null ? bookingCombos : new ArrayList<>();
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
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
    public String toString() {
        return "Booking{" +
                "bookingID='" + bookingID + '\'' +
                ", userID=" + userID +
                ", paymentID='" + getPaymentID() + '\'' +
                ", bookingStatus=" + bookingStatus +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", ticketCount=" + (tickets != null ? tickets.size() : 0) +
                '}';
    }
}
