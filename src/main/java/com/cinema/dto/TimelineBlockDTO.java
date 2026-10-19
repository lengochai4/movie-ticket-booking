package com.cinema.dto;

import java.io.Serializable;

/**
 * Một khối suất chiếu trên Timeline: vị trí và độ rộng tính theo % trục 08:00 - 24:00.
 */
public class TimelineBlockDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private ShowtimeDTO showtime;
    private String left;
    private String width;

    public TimelineBlockDTO() {
    }

    public ShowtimeDTO getShowtime() {
        return showtime;
    }

    public void setShowtime(ShowtimeDTO showtime) {
        this.showtime = showtime;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }
}
