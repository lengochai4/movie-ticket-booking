package com.cinema.dto;

import java.io.Serializable;

/**
 * Doanh thu của một cụm rạp (biểu đồ donut trên Dashboard).
 */
public class CinemaRevenueDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String cinemaName;
    private double revenue;

    public CinemaRevenueDTO() {
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
}
