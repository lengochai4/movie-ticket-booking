package com.cinema.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Thời điểm đặt và tổng tiền của một đơn đã thanh toán, để chia doanh thu theo giờ / ngày / tháng.
 */
public class RevenueRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDateTime createdAt;
    private double totalPrice;

    public RevenueRecordDTO() {
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
