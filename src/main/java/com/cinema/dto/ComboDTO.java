package com.cinema.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Combo bắp nước truyền giữa form admin, Servlet, DAO (JDBC) và JSP của Vinh.
 * Hải dùng ComboDAO.findActive() để lấy danh sách combo đang bán cho bước chọn combo.
 */
public class ComboDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String STATUS_ACTIVE = "ACTIVE";             // Đang mở bán
    public static final String STATUS_OUT_OF_STOCK = "OUT_OF_STOCK"; // Tạm hết hàng

    private String comboID;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private String imageURL;
    private String status;

    public ComboDTO() {
    }

    public String getComboID() {
        return comboID;
    }

    public void setComboID(String comboID) {
        this.comboID = comboID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }
}
