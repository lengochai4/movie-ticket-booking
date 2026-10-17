package com.cinema.model.entity;

import java.io.Serializable;
import java.time.LocalDate;

public class Movie implements Serializable {
    private static final long serialVersionUID = 1L;

    private String movieID;
    private String title;
    private String description;
    private String posterURL;
    private String trailerURL;
    private String director;
    private String cast;
    private String genre;
    private String duration;
    private LocalDate releaseDate;
    private String language;
    private String ageRating; // P, C13, C16, C18
    private String status;    // NOW_SHOWING, COMING_SOON

    public Movie() {
    }

    public Movie(String movieID, String title, String description, String posterURL, String trailerURL,
                 String director, String cast, String genre, String duration, LocalDate releaseDate,
                 String language, String ageRating, String status) {
        this.movieID = movieID;
        this.title = title;
        this.description = description;
        this.posterURL = posterURL;
        this.trailerURL = trailerURL;
        this.director = director;
        this.cast = cast;
        this.genre = genre;
        this.duration = duration;
        this.releaseDate = releaseDate;
        this.language = language;
        this.ageRating = ageRating;
        this.status = status;
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

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "movieID='" + movieID + '\'' +
                ", title='" + title + '\'' +
                ", genre='" + genre + '\'' +
                ", duration='" + duration + '\'' +
                ", releaseDate=" + releaseDate +
                ", ageRating='" + ageRating + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
