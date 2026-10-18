package com.cinema.entity;

import com.cinema.enums.BookingStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "booking_id", length = 36)
    private String bookingID;

    @Column(name = "order_code", nullable = false, unique = true, length = 40)
    private String orderCode;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id")
    private Showtime showtime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "ticket_price")
    private Double ticketPrice;

    @Column(name = "combo_price")
    private Double comboPrice;

    @Column(name = "points_earned")
    private Integer pointsEarned;

    @Column(name = "points_used")
    private Integer pointsUsed;

    @Column(name = "points_reversed", nullable = false)
    private boolean pointsReversed;

    @Column(name = "movie_title", length = 255)
    private String movieTitle;

    @Column(name = "poster_url", length = 1000)
    private String posterURL;

    @Column(name = "age_rating", length = 10)
    private String ageRating;

    @Column(name = "movie_format", length = 80)
    private String movieFormat;

    @Column(name = "cinema_name", length = 255)
    private String cinemaName;

    @Column(name = "cinema_address", length = 500)
    private String cinemaAddress;

    @Column(name = "room_name", length = 100)
    private String roomName;

    @Column(name = "show_start")
    private LocalDateTime showStart;

    @Column(length = 500)
    private String seats;

    @Column(columnDefinition = "TEXT")
    private String combos;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();

    public Booking() {
    }

    public void addTicket(Ticket ticket) {
        if (ticket == null) {
            return;
        }
        if (tickets == null) {
            tickets = new ArrayList<>();
        }
        ticket.setBooking(this);
        tickets.add(ticket);
    }

    public String getBookingID() {
        return bookingID;
    }

    public void setBookingID(String bookingID) {
        this.bookingID = bookingID;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public void setShowtime(Showtime showtime) {
        this.showtime = showtime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(Double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public Double getComboPrice() {
        return comboPrice;
    }

    public void setComboPrice(Double comboPrice) {
        this.comboPrice = comboPrice;
    }

    public Integer getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(Integer pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public Integer getPointsUsed() {
        return pointsUsed;
    }

    public void setPointsUsed(Integer pointsUsed) {
        this.pointsUsed = pointsUsed;
    }

    public boolean isPointsReversed() {
        return pointsReversed;
    }

    public void setPointsReversed(boolean pointsReversed) {
        this.pointsReversed = pointsReversed;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public String getMovieFormat() {
        return movieFormat;
    }

    public void setMovieFormat(String movieFormat) {
        this.movieFormat = movieFormat;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public String getCinemaAddress() {
        return cinemaAddress;
    }

    public void setCinemaAddress(String cinemaAddress) {
        this.cinemaAddress = cinemaAddress;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public LocalDateTime getShowStart() {
        return showStart;
    }

    public void setShowStart(LocalDateTime showStart) {
        this.showStart = showStart;
    }

    public String getSeats() {
        return seats;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }

    public String getCombos() {
        return combos;
    }

    public void setCombos(String combos) {
        this.combos = combos;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public List<Ticket> getTickets() {
        if (tickets == null) {
            tickets = new ArrayList<>();
        }
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets == null ? new ArrayList<>() : tickets;
    }
}
