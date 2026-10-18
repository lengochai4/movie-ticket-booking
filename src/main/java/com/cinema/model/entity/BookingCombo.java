package com.cinema.model.entity;

import java.io.Serializable;
import java.util.Objects;

public class BookingCombo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String bookingComboID;
    private String bookingID;
    private String comboID;
    private int quantity;

    public BookingCombo() {
        this.quantity = 1;
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
        return comboID;
    }

    public void setComboID(String comboID) {
        this.comboID = comboID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingCombo that = (BookingCombo) o;
        return Objects.equals(bookingComboID, that.bookingComboID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingComboID);
    }

    @Override
    public String toString() {
        return "BookingCombo{" +
                "bookingComboID='" + bookingComboID + '\'' +
                ", bookingID='" + bookingID + '\'' +
                ", comboID='" + comboID + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
