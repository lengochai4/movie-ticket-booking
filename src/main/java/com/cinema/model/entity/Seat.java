package com.cinema.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class Seat implements Serializable {
    private static final long serialVersionUID = 1L;

    private String seatID;
    private String roomID;
    private String row;
    private String column;
    private String type; // STANDARD, VIP, COUPLE
    private BigDecimal extraPrice;
    private boolean isAvailable;

    public Seat() {
        this.type = "STANDARD";
        this.extraPrice = BigDecimal.ZERO;
        this.isAvailable = true;
    }

    public Seat(String seatID, String roomID, String row, String column, String type, BigDecimal extraPrice) {
        this.seatID = seatID;
        this.roomID = roomID;
        this.row = row;
        this.column = column;
        this.type = type != null ? type : "STANDARD";
        this.extraPrice = extraPrice != null ? extraPrice : BigDecimal.ZERO;
        this.isAvailable = true;
    }

    public Seat(String seatID, String roomID, String row, String column, String type, BigDecimal extraPrice, boolean isAvailable) {
        this.seatID = seatID;
        this.roomID = roomID;
        this.row = row;
        this.column = column;
        this.type = type != null ? type : "STANDARD";
        this.extraPrice = extraPrice != null ? extraPrice : BigDecimal.ZERO;
        this.isAvailable = isAvailable;
    }

    public String getSeatID() {
        return seatID;
    }

    public void setSeatID(String seatID) {
        this.seatID = seatID;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getExtraPrice() {
        return extraPrice;
    }

    public void setExtraPrice(BigDecimal extraPrice) {
        this.extraPrice = extraPrice;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seat seat = (Seat) o;
        return Objects.equals(seatID, seat.seatID) && Objects.equals(roomID, seat.roomID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seatID, roomID);
    }

    @Override
    public String toString() {
        return "Seat{" +
                "seatID='" + seatID + '\'' +
                ", roomID='" + roomID + '\'' +
                ", row='" + row + '\'' +
                ", column='" + column + '\'' +
                ", type='" + type + '\'' +
                ", extraPrice=" + extraPrice +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
