package com.cinema.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entity Người Dùng do Ân phụ trách.
 * Đáp ứng đầy đủ các hợp đồng dữ liệu với Phương và Như:
 * - SessionScope: sessionScope.loginUser
 * - Khóa chính ID dạng String
 * - Getters: id, userId, fullName, email, phone, role, status, loyaltyPoints, createdAt
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Id
    @Column(name = "user_id", length = 36)
    private String id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String role = "USER"; // 'ADMIN', 'STAFF', 'CUSTOMER'/'USER'

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE"; // 'ACTIVE', 'LOCKED'

    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {
    }

    public User(String id, String fullName, String email, String phone, String password, String role, String status) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role != null ? role : "USER";
        this.status = status != null ? status : "ACTIVE";
        this.loyaltyPoints = 0;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Bí danh để tương thích linh hoạt với các module khác
    public String getUserId() {
        return id;
    }

    public void setUserId(String userId) {
        this.id = userId;
    }

    public String getUserID() {
        return id;
    }

    public void setUserID(String userID) {
        this.id = userID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getLoyaltyPoints() {
        return loyaltyPoints != null ? loyaltyPoints : 0;
    }

    public void setLoyaltyPoints(Integer loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints != null ? loyaltyPoints : 0;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    // EL Expression ${u.createdAt} gọi getter này để hiển thị ngày định dạng dd/MM/yyyy
    public String getCreatedAt() {
        if (createdAt == null) {
            return "";
        }
        return createdAt.format(DATE_FORMATTER);
    }

    public LocalDateTime getCreatedAtDateTime() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setCreatedAtDateTime(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
