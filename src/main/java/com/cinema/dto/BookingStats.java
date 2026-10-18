package com.cinema.dto;

import java.io.Serializable;

public class BookingStats implements Serializable {

    private static final long serialVersionUID = 1L;

    private int totalOrders;
    private int paidOrders;
    private int pendingOrders;
    private int cancelledOrders;
    private String totalRevenue;

    public BookingStats() {
        this.totalRevenue = "0đ";
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getPaidOrders() {
        return paidOrders;
    }

    public void setPaidOrders(int paidOrders) {
        this.paidOrders = paidOrders;
    }

    public int getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(int pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public int getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(int cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public String getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(String totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
