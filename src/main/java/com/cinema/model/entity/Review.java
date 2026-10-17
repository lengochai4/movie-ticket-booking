package com.cinema.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    private String reviewID;
    private int userID;
    private String movieID;
    private float rating;
    private String comment;
    private LocalDateTime createdAt;

    public Review() {
        this.createdAt = LocalDateTime.now();
    }

    public Review(String reviewID, int userID, String movieID, float rating, String comment, LocalDateTime createdAt) {
        this.reviewID = reviewID;
        this.userID = userID;
        this.movieID = movieID;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public String getReviewID() {
        return reviewID;
    }

    public void setReviewID(String reviewID) {
        this.reviewID = reviewID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getMovieID() {
        return movieID;
    }

    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Review{" +
                "reviewID='" + reviewID + '\'' +
                ", userID=" + userID +
                ", movieID='" + movieID + '\'' +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
