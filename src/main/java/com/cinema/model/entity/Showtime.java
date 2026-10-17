package com.cinema.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Showtime implements Serializable {
    private static final long serialVersionUID = 1L;

    private String showTimeID;
    private String movieID;
    private String roomID;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;

    public Showtime() {
        this.price = BigDecimal.ZERO;
    }

    public Showtime(String showTimeID, String movieID, String roomID, LocalDateTime startTime, LocalDateTime endTime, BigDecimal price) {
        this.showTimeID = showTimeID;
        this.movieID = movieID;
        this.roomID = roomID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price != null ? price : BigDecimal.ZERO;
    }

    public String getShowTimeID() {
        return showTimeID;
    }

    public void setShowTimeID(String showTimeID) {
        this.showTimeID = showTimeID;
    }

    public String getMovieID() {
        return movieID;
    }

    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price != null ? price : BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "Showtime{" +
                "showTimeID='" + showTimeID + '\'' +
                ", movieID='" + movieID + '\'' +
                ", roomID='" + roomID + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", price=" + price +
                '}';
    }
}
