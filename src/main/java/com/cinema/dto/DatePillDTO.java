package com.cinema.dto;

import java.io.Serializable;

/**
 * Một nút trên thanh chọn ngày của trang Suất chiếu.
 */
public class DatePillDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String iso;      // yyyy-MM-dd
    private String dayName;  // Hôm nay, Thứ 2 ...
    private String dayNum;   // dd/MM

    public DatePillDTO() {
    }

    public DatePillDTO(String iso, String dayName, String dayNum) {
        this.iso = iso;
        this.dayName = dayName;
        this.dayNum = dayNum;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public String getDayNum() {
        return dayNum;
    }

    public void setDayNum(String dayNum) {
        this.dayNum = dayNum;
    }
}
