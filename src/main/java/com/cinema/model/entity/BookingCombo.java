package com.cinema.model.entity;

import java.io.Serializable;

public class BookingCombo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String bookingComboID;
    private String bookingID;
    private String comboID;
    private Combo combo;
    private int quantity;

    public BookingCombo() {
        this.quantity = 1;
    }

    public BookingCombo(String bookingComboID, String bookingID, Combo combo, int quantity) {
        this.bookingComboID = bookingComboID;
        this.bookingID = bookingID;
        this.combo = combo;
        if (combo != null) {
            this.comboID = combo.getComboID();
        }
        this.quantity = quantity;
    }

    public BookingCombo(String bookingComboID, String bookingID, String comboID, int quantity) {
        this.bookingComboID = bookingComboID;
        this.bookingID = bookingID;
        this.comboID = comboID;
        this.quantity = quantity;
    }

    public String getBookingComboID() {
        return bookingComboID;
    }

    public void setBookingComboID(String bookingComboID) {
        this.bookingComboID = bookingComboID;
    }

    public String getBookingID() {
        return bookingID;
    }

    public void setBookingID(String bookingID) {
        this.bookingID = bookingID;
    }

    public String getComboID() {
        if (comboID != null) {
            return comboID;
        }
        return combo != null ? combo.getComboID() : null;
    }

    public void setComboID(String comboID) {
        this.comboID = comboID;
    }

    public Combo getCombo() {
        return combo;
    }

    public void setCombo(Combo combo) {
        this.combo = combo;
        if (combo != null) {
            this.comboID = combo.getComboID();
        }
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "BookingCombo{" +
                "bookingComboID='" + bookingComboID + '\'' +
                ", bookingID='" + bookingID + '\'' +
                ", comboID='" + getComboID() + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
