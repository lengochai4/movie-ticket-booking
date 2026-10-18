package com.cinema.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name= "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Tạm dùng để hệ thống thêm mã String ngẫu nhiên
    @Column(length = 36)
    private String movieID;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")  // Dùng TEXT vì mô tả phim có thể rất dài
    private String description;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Showtime> showtimes;

    public List<Showtime> getShowtimes() {
        return showtimes;
    }

    public void setShowtimes(List<Showtime> showtimes) {
        this.showtimes = showtimes;
    }

    private String posterURL;
    private String director;
    private int duration;
    private LocalDate releaseDate;

    @Column(length = 100)
    private String genre;           // Thể loại phim (VD: "Tâm lý, Tình cảm")

    @Column(length = 10)
    private String ageRating;       // Phân loại độ tuổi (VD: "P", "T13", "T16", "T18", "K")

    public Movie() {}

    public Movie(String title, String description, String posterURL, String director, int duration, LocalDate releaseDate){
        this.title = title;
        this.description = description;
        this.posterURL = posterURL;
        this.director = director;
        this.duration = duration;
        this.releaseDate = releaseDate;
    }

    public String getMovieID() {return movieID;}
    public void setMovieID(String movieID) {this.movieID = movieID;}
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}
    public String getPosterURL() {return posterURL;}
    public void setPosterURL(String posterURL) {this.posterURL = posterURL;}
    public String getPosterUrl() {return posterURL;}
    public void setPosterUrl(String posterUrl) {this.posterURL = posterUrl;}
    public String getDirector() {return director;}
    public void setDirector(String director) {this.director = director;}
    public int getDuration() {return duration;}
    public void setDuration(int duration) {this.duration = duration;}
    public LocalDate getReleaseDate() {return releaseDate;}
    public void setReleaseDate(LocalDate releaseDate) {this.releaseDate = releaseDate;}
    public String getGenre() {return genre;}
    public void setGenre(String genre) {this.genre = genre;}
    public String getAgeRating() {return ageRating;}
    public void setAgeRating(String ageRating) {this.ageRating = ageRating;}
    public String getRating() {return ageRating;}
    public void setRating(String rating) {this.ageRating = rating;}
}