package com.cinema.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class Combo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String comboID;
    private String name;
    private String description;
    private BigDecimal price;

    public Combo() {
        this.price = BigDecimal.ZERO;
    }

    public Combo(String comboID, String name, String description, BigDecimal price) {
        this.comboID = comboID;
        this.name = name;
        this.description = description;
        this.price = price != null ? price : BigDecimal.ZERO;
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
        this.price = price != null ? price : BigDecimal.ZERO;
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
