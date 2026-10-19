package com.cinema.dto;

import java.io.Serializable;

/**
 * Phòng chiếu (kèm tên rạp) dùng cho trang Suất chiếu của Vinh.
 */
public class RoomDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomID;
    private String name;
    private int capacity;
    private String cinemaID;
    private String cinemaName;

    public RoomDTO() {
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getCinemaID() {
        return cinemaID;
    }

    public void setCinemaID(String cinemaID) {
        this.cinemaID = cinemaID;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }
}
