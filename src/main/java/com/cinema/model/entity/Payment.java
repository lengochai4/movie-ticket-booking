package com.cinema.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;

    private String paymentID;
    private String type; // CASH, VNPAY, MOMO
    private BigDecimal amount;
    private LocalDateTime paymentDate;

    public Payment() {
        this.amount = BigDecimal.ZERO;
        this.paymentDate = LocalDateTime.now();
    }

    public Payment(String paymentID, String type, BigDecimal amount, LocalDateTime paymentDate) {
        this.paymentID = paymentID;
        this.type = type;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.paymentDate = paymentDate != null ? paymentDate : LocalDateTime.now();
    }

    public String getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(String paymentID) {
        this.paymentID = paymentID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount != null ? amount : BigDecimal.ZERO;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentID='" + paymentID + '\'' +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", paymentDate=" + paymentDate +
                '}';
    }
}
