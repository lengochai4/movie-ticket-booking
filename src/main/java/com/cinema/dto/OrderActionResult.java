package com.cinema.dto;

import java.io.Serializable;

public class OrderActionResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private String userId;
    private Integer loyaltyPoints;
    private String orderCode;

    public OrderActionResult() {
    }

    public static OrderActionResult ok(String message) {
        OrderActionResult result = new OrderActionResult();
        result.setSuccess(true);
        result.setMessage(message);
        return result;
    }

    public static OrderActionResult fail(String message) {
        OrderActionResult result = new OrderActionResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(Integer loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }
}
