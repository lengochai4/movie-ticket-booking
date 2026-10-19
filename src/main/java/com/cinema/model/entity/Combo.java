package com.cinema.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class Combo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String comboID;
    private String name;
    private String description;
    private BigDecimal price;

    private String category;
    private String imageURL;
    private String status;

    public Combo() {
        this.price = BigDecimal.ZERO;
    }

    public Combo(String comboID, String name, String description, BigDecimal price) {
        this.comboID = comboID;
        this.name = name;
        this.description = description;
        this.price = price != null ? price : BigDecimal.ZERO;
    }

    public Combo(String comboID, String name, String description, BigDecimal price, String category, String imageURL, String status) {
        this.comboID = comboID;
        this.name = name;
        this.description = description;
        this.price = price != null ? price : BigDecimal.ZERO;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Combo combo = (Combo) o;
        return Objects.equals(comboID, combo.comboID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(comboID);
    }

    @Override
    public String toString() {
        return "Combo{" +
                "comboID='" + comboID + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                '}';
    }
}
