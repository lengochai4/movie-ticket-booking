package com.cinema.model.entity;

import com.cinema.model.enums.TicketStatus;

import java.io.Serializable;

public class Ticket implements Serializable {
    private static final long serialVersionUID = 1L;

    private String ticketID;
    private String bookingID;
    private String movieID;
    private String cinemaID;
    private String showTimeID;
    private String seatID;
    private TicketStatus ticketStatus;

    public Ticket() {
        this.ticketStatus = TicketStatus.AVAILABLE;
    }

    public Ticket(String ticketID, String bookingID, String movieID, String cinemaID, String showTimeID, String seatID, TicketStatus ticketStatus) {
        this.ticketID = ticketID;
        this.bookingID = bookingID;
        this.movieID = movieID;
        this.cinemaID = cinemaID;
        this.showTimeID = showTimeID;
        this.seatID = seatID;
        this.ticketStatus = ticketStatus != null ? ticketStatus : TicketStatus.AVAILABLE;
    }

    public String getTicketID() {
        return ticketID;
    }

    public void setTicketID(String ticketID) {
        this.ticketID = ticketID;
    }

    public String getBookingID() {
        return bookingID;
    }

    public void setBookingID(String bookingID) {
        this.bookingID = bookingID;
    }

    public String getMovieID() {
        return movieID;
    }

    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }

    public String getCinemaID() {
        return cinemaID;
    }

    public void setCinemaID(String cinemaID) {
        this.cinemaID = cinemaID;
    }

    public String getShowTimeID() {
        return showTimeID;
    }

    public void setShowTimeID(String showTimeID) {
        this.showTimeID = showTimeID;
    }

    public String getSeatID() {
        return seatID;
    }

    public void setSeatID(String seatID) {
        this.seatID = seatID;
    }

    public TicketStatus getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(TicketStatus ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "ticketID='" + ticketID + '\'' +
                ", bookingID='" + bookingID + '\'' +
                ", movieID='" + movieID + '\'' +
                ", cinemaID='" + cinemaID + '\'' +
                ", showTimeID='" + showTimeID + '\'' +
                ", seatID='" + seatID + '\'' +
                ", ticketStatus=" + ticketStatus +
                '}';
    }
}
