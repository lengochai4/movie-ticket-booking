package com.cinema.model.entity;

import java.io.Serializable;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomID;
    private String roomName;
    private int capacity;
    private String roomType; // 2D, 3D, IMAX
    private String cinemaID;

    public Room() {
    }

    public Room(String roomID, String roomName, int capacity, String roomType, String cinemaID) {
        this.roomID = roomID;
        this.roomName = roomName;
        this.capacity = capacity;
        this.roomType = roomType;
        this.cinemaID = cinemaID;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getCinemaID() {
        return cinemaID;
    }

    public void setCinemaID(String cinemaID) {
        this.cinemaID = cinemaID;
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomID='" + roomID + '\'' +
                ", roomName='" + roomName + '\'' +
                ", capacity=" + capacity +
                ", roomType='" + roomType + '\'' +
                ", cinemaID='" + cinemaID + '\'' +
                '}';
    }
}
