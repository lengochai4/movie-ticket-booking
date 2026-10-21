package com.cinema.dto;

import java.io.Serializable;

/**
 * Tổng hợp doanh thu của một khoảng thời gian (chỉ đơn PAID), dùng cho Dashboard.
 */
public class DashboardSummaryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private double totalRevenue;   // tổng tiền đơn
    private double ticketRevenue;  // tiền vé
    private double comboRevenue;   // tiền bắp nước
    private long bookingCount;     // số đơn
    private long ticketCount;      // số vé

    public DashboardSummaryDTO() {
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getTicketRevenue() {
        return ticketRevenue;
    }

    public void setTicketRevenue(double ticketRevenue) {
        this.ticketRevenue = ticketRevenue;
    }

    public double getComboRevenue() {
        return comboRevenue;
    }

    public void setComboRevenue(double comboRevenue) {
        this.comboRevenue = comboRevenue;
    }

    public long getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(long bookingCount) {
        this.bookingCount = bookingCount;
    }

    public long getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(long ticketCount) {
        this.ticketCount = ticketCount;
    }
}
