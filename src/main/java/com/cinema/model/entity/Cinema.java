package com.cinema.model.entity;

import java.io.Serializable;

public class Cinema implements Serializable {
    private static final long serialVersionUID = 1L;

    private String cinemaID;
    private String name;
    private String location;

    public Cinema() {
    }

    public Cinema(String cinemaID, String name, String location) {
        this.cinemaID = cinemaID;
        this.name = name;
        this.location = location;
    }

    public String getCinemaID() {
        return cinemaID;
    }

    public void setCinemaID(String cinemaID) {
        this.cinemaID = cinemaID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Cinema{" +
                "cinemaID='" + cinemaID + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
