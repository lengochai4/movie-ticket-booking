package com.cinema.model.entity;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userID;
    private String fullName;
    private String passwordHash;
    private String email;
    private String phone;
    private int role; // 0: User, 1: Admin
    private int loyaltyPoints;
    private boolean isLocked; // false: active, true: locked

    public User() {
    }

    public User(int userID, String fullName, String passwordHash, String email, String phone, int role, int loyaltyPoints) {
        this.userID = userID;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.loyaltyPoints = loyaltyPoints;
        this.isLocked = false;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    @Override
    public String toString() {
        return "User{" +
                "userID=" + userID +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", role=" + role +
                ", loyaltyPoints=" + loyaltyPoints +
                ", isLocked=" + isLocked +
                '}';
    }
}
