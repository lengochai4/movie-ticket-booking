package com.cinema.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Sổ điểm thưởng do module của Như quản lý, khóa bằng userID.
 * Không sửa entity User của Ân: servlet đồng bộ số điểm sang sessionScope.loginUser
 * khi object trên session có setLoyaltyPoints.
 */
@Entity
@Table(name = "loyalty_accounts")
public class LoyaltyAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "userid", length = 36)
    private String userID;

    @Column(nullable = false)
    private Integer points;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;

    public LoyaltyAccount() {
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
