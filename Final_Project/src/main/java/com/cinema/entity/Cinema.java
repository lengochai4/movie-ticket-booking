package com.cinema.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "cinema")

public class Cinema {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String cinemaID;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @OneToMany(mappedBy = "cinema", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Room> rooms;

    public Cinema() {
    }

    public Cinema(String cinemaID, String name, String address, List<Room> rooms) {
        this.cinemaID = cinemaID;
        this.name = name;
        this.address = address;
        this.rooms = rooms;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }
}
