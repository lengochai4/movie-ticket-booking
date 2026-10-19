package com.cinema.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "combos")
public class Combo implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String STATUS_ACTIVE = "ACTIVE";             // Đang mở bán
    public static final String STATUS_OUT_OF_STOCK = "OUT_OF_STOCK"; // Tạm hết hàng

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String comboID;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;       // Giá bán niêm yết (VNĐ)

    @Column(length = 50)
    private String category;        // Combo, Bắp Rang, Nước, Đặc Biệt

    @Column(length = 1000)
    private String imageURL;

    @Column(nullable = false, length = 20)
    private String status = STATUS_ACTIVE;

    public Combo() {
    }

    public Combo(String name, String description, BigDecimal price, String category, String imageURL, String status) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageURL = imageURL;
        this.status = status;
    }

    public Combo(String comboID, String name, String description, BigDecimal price, String category, String imageURL, String status) {
        this.comboID = comboID;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageURL = imageURL;
        this.status = status;
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
