package com.cinema.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Entity
@Table(name = "reviews")
public class Review implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "movie_id", nullable = false, length = 36)
    private String movieID;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "user_name", length = 150)
    private String userName;

    @Column(nullable = false)
    private int rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Review() {
    }

    public Review(String id, String movieID, String userId, String userName, int rating, String comment) {
        this.id = id;
        this.movieID = movieID;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMovieID() {
        return movieID;
    }

    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }

    public String getMovieId() {
        return movieID;
    }

    public void setMovieId(String movieId) {
        this.movieID = movieId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    // Hiển thị thời gian thân thiện trên JSP: "Vừa xong", "1 giờ trước", "dd/MM/yyyy HH:mm"
    public String getCreatedAt() {
        if (createdAt == null) {
            return "Vừa xong";
        }
        Duration diff = Duration.between(createdAt, LocalDateTime.now());
        long minutes = diff.toMinutes();
        if (minutes < 1) {
            return "Vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else if (diff.toHours() < 24) {
            return diff.toHours() + " giờ trước";
        } else if (diff.toDays() <= 7) {
            return diff.toDays() + " ngày trước";
        }
        return createdAt.format(TIME_FORMATTER);
    }

    public LocalDateTime getCreatedAtDateTime() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
