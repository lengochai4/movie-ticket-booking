package com.cinema.dto;

import java.io.Serializable;

/**
 * Dữ liệu Hải có thể truyền vào sau bước thanh toán mà không cần Như sửa BookingFlowServlet.
 * selectedSeats: chuỗi ghế cách nhau bởi dấu phẩy (mã ghế hoặc seatID).
 */
public class PlaceBookingRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String customerName;
    private String phone;
    private String email;
    private String showtimeId;
    private String selectedSeats;
    private String combosRaw;
    private Double ticketTotal;
    private Double comboTotal;
    private Double finalTotal;
    private String movieFormat;
    private boolean paid;

    public PlaceBookingRequest() {
        this.paid = true;
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

    public String getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(String showtimeId) {
        this.showtimeId = showtimeId;
    }

    public String getSelectedSeats() {
        return selectedSeats;
    }

    public void setSelectedSeats(String selectedSeats) {
        this.selectedSeats = selectedSeats;
    }

    public String getCombosRaw() {
        return combosRaw;
    }

    public void setCombosRaw(String combosRaw) {
        this.combosRaw = combosRaw;
    }

    public Double getTicketTotal() {
        return ticketTotal;
    }

    public void setTicketTotal(Double ticketTotal) {
        this.ticketTotal = ticketTotal;
    }

    public Double getComboTotal() {
        return comboTotal;
    }

    public void setComboTotal(Double comboTotal) {
        this.comboTotal = comboTotal;
    }

    public Double getFinalTotal() {
        return finalTotal;
    }

    public void setFinalTotal(Double finalTotal) {
        this.finalTotal = finalTotal;
    }

    public String getMovieFormat() {
        return movieFormat;
    }

    public void setMovieFormat(String movieFormat) {
        this.movieFormat = movieFormat;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
