package com.cinema.dto;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Dữ liệu phim truyền giữa form admin, Servlet, DAO (JDBC) và JSP của Vinh.
 */
public class MovieDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String movieID;
    private String title;
    private String description;
    private String posterURL;
    private String trailerURL;
    private String director;
    private int duration;          // phút
    private LocalDate releaseDate;
    private String genre;
    private String ageRating;      // P, K, T13, T16, T18

    public MovieDTO() {
    }

    public String getMovieID() {
        return movieID;
    }

    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }

    // Bí danh cho JSP đang dùng ${m.posterUrl}
    public String getPosterUrl() {
        return posterURL;
    }

    public String getTrailerURL() {
        return trailerURL;
    }

    public void setTrailerURL(String trailerURL) {
        this.trailerURL = trailerURL;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }
}
