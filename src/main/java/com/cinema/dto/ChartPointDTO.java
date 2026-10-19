package com.cinema.dto;

import java.io.Serializable;

/**
 * Một cột của biểu đồ doanh thu: nhãn (giờ / ngày / tháng) và giá trị (triệu VNĐ, dạng chuỗi "12.35").
 */
public class ChartPointDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String label;
    private String value;

    public ChartPointDTO() {
    }

    public ChartPointDTO(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
