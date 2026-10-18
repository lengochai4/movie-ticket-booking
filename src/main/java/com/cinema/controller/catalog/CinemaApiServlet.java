package com.cinema.controller.catalog;

import com.cinema.dao.catalog.CinemaDAO;
import com.cinema.entity.Cinema;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "CinemaApiServlet", urlPatterns = {"/api/cinemas"})
public class CinemaApiServlet extends HttpServlet {

    private final CinemaDAO cinemaDAO = new CinemaDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        List<Cinema> cinemas = cinemaDAO.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Cinema c : cinemas) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getCinemaID());
            map.put("name", c.getName());
            map.put("address", c.getAddress());

            // Nhận diện thương hiệu
            String brand = "Cinetik";
            String brandBadge = "badge-cinetik";
            String brandIcon = "🏛️";
            String nameLower = c.getName().toLowerCase();
            if (nameLower.contains("beta")) {
                brand = "Beta Cinemas";
                brandBadge = "badge-beta";
                brandIcon = "🍿";
            } else if (nameLower.contains("cgv")) {
                brand = "CGV Cinemas";
                brandBadge = "badge-cgv";
                brandIcon = "🎬";
            } else if (nameLower.contains("galaxy")) {
                brand = "Galaxy Cinema";
                brandBadge = "badge-galaxy";
                brandIcon = "✨";
            }

            // Nhận diện tỉnh thành / khu vực từ địa chỉ
            String city = "Khác";
            String addr = c.getAddress() != null ? c.getAddress() : "";
            if (addr.contains("Hồ Chí Minh") || addr.contains("TP.HCM") || addr.contains("TP. Hồ Chí Minh")) {
                city = "TP. Hồ Chí Minh";
            } else if (addr.contains("Hà Nội")) {
                city = "Hà Nội";
            } else if (addr.contains("Đà Nẵng")) {
                city = "Đà Nẵng";
            } else if (addr.contains("Bình Dương")) {
                city = "Bình Dương";
            } else if (addr.contains("Đồng Nai") || addr.contains("Biên Hòa")) {
                city = "Đồng Nai";
            } else if (addr.contains("Cần Thơ")) {
                city = "Cần Thơ";
            } else if (addr.contains("Nha Trang") || addr.contains("Khánh Hòa")) {
                city = "Khánh Hòa";
            } else if (addr.contains("Hạ Long") || addr.contains("Quảng Ninh")) {
                city = "Quảng Ninh";
            } else if (addr.contains("Thái Nguyên")) {
                city = "Thái Nguyên";
            } else if (addr.contains("Bến Tre")) {
                city = "Bến Tre";
            }

            map.put("brand", brand);
            map.put("brandBadge", brandBadge);
            map.put("brandIcon", brandIcon);
            map.put("city", city);

            result.add(map);
        }

        String json = gson.toJson(result);
        response.getWriter().write(json);
    }
}
