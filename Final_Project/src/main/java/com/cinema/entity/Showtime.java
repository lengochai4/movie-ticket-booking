package com.cinema.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "showtimes")

public class Showtime {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String showtimeID;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    public Showtime() {
    }

    public Showtime(String showtimeID, LocalDateTime startTime, LocalDateTime endTime, Movie movie, Room room) {
        this.showtimeID = showtimeID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.movie = movie;
        this.room = room;
    }

    public String getShowtimeID() {
        return showtimeID;
    }

    public void setShowtimeID(String showtimeID) {
        this.showtimeID = showtimeID;
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

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getFormattedStartTime() {
        return startTime != null ? startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) : "";
    }

    public String getFormattedEndTime() {
        return endTime != null ? endTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) : "";
    }

    public String getFormattedDate() {
        return startTime != null ? startTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }

    public String getFormattedDateIso() {
        return startTime != null ? startTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
    }

    public String getTimeRange() {
        return getFormattedStartTime() + " - " + getFormattedEndTime();
    }
}
