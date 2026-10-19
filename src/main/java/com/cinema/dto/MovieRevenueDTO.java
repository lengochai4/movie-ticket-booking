package com.cinema.dto;

import java.io.Serializable;

/**
 * Một dòng trong bảng Top phim theo doanh thu của Dashboard.
 */
public class MovieRevenueDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String movieID;
    private String title;
    private String genre;
    private int duration;
    private String posterUrl;
    private long showtimesCount;    // số suất có bán vé
    private long ticketsSold;
    private long revenue;           // VNĐ
    private String contributionPercent;

    public MovieRevenueDTO() {
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

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public long getShowtimesCount() {
        return showtimesCount;
    }

    public void setShowtimesCount(long showtimesCount) {
        this.showtimesCount = showtimesCount;
    }

    public long getTicketsSold() {
        return ticketsSold;
    }

    public void setTicketsSold(long ticketsSold) {
        this.ticketsSold = ticketsSold;
    }

    public long getRevenue() {
        return revenue;
    }

    public void setRevenue(long revenue) {
        this.revenue = revenue;
    }

    public String getContributionPercent() {
        return contributionPercent;
    }

    public void setContributionPercent(String contributionPercent) {
        this.contributionPercent = contributionPercent;
    }
}
