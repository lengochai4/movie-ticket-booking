package com.cinema.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Suất chiếu kèm phim, phòng và số liệu bán vé, dùng cho trang Suất chiếu của Vinh.
 */
public class ShowtimeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String showtimeID;
    private MovieDTO movie;
    private RoomDTO room;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double price;           // giá vé gốc (VNĐ), có thể null với dữ liệu cũ

    // Số liệu hiển thị (không lưu DB)
    private String status;          // OPEN, SOON, ENDED
    private long soldTickets;
    private int fillPercent;

    public ShowtimeDTO() {
    }

    public String getShowtimeID() {
        return showtimeID;
    }

    public void setShowtimeID(String showtimeID) {
        this.showtimeID = showtimeID;
    }

    public MovieDTO getMovie() {
        return movie;
    }

    public void setMovie(MovieDTO movie) {
        this.movie = movie;
    }

    public RoomDTO getRoom() {
        return room;
    }

    public void setRoom(RoomDTO room) {
        this.room = room;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getSoldTickets() {
        return soldTickets;
    }

    public void setSoldTickets(long soldTickets) {
        this.soldTickets = soldTickets;
    }

    public int getFillPercent() {
        return fillPercent;
    }

    public void setFillPercent(int fillPercent) {
        this.fillPercent = fillPercent;
    }

    public String getFormattedStartTime() {
        return startTime != null ? startTime.format(TIME) : "";
    }

    public String getFormattedEndTime() {
        return endTime != null ? endTime.format(TIME) : "";
    }

    public String getFormattedDate() {
        return startTime != null ? startTime.format(DATE) : "";
    }

    public String getFormattedDateIso() {
        return startTime != null ? startTime.format(DATE_ISO) : "";
    }

    public String getTimeRange() {
        return getFormattedStartTime() + " - " + getFormattedEndTime();
    }
}
