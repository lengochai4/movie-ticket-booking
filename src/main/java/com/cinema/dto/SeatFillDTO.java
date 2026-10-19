package com.cinema.dto;

import java.io.Serializable;
import java.util.Locale;

/**
 * Số vé đã bán / tổng số ghế của các suất chiếu trong một khoảng thời gian.
 */
public class SeatFillDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private long soldTickets;
    private long totalSeats;

    public SeatFillDTO() {
    }

    public long getSoldTickets() {
        return soldTickets;
    }

    public void setSoldTickets(long soldTickets) {
        this.soldTickets = soldTickets;
    }

    public long getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(long totalSeats) {
        this.totalSeats = totalSeats;
    }

    // Tỷ lệ lấp đầy dạng "12.5%"
    public String getFillRateText() {
        return totalSeats > 0
                ? String.format(Locale.US, "%.1f%%", soldTickets * 100.0 / totalSeats)
                : "0%";
    }
}
