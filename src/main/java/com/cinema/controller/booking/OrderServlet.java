package com.cinema.controller.booking;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/order/history", "/order/ticket-detail", "/booking/history"})
public class OrderServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        if ("/order/ticket-detail".equals(path)) {
            // Khi xem chi tiết vé
            String orderId = request.getParameter("id");
            if (orderId == null || orderId.trim().isEmpty()) {
                orderId = "CNK-2026-88921";
            }
            request.setAttribute("orderCode", orderId);
            request.getRequestDispatcher("/WEB-INF/views/order/ticket-detail.jsp").forward(request, response);
        } else {
            // Mặc định là /order/history hoặc /booking/history
            request.getRequestDispatcher("/WEB-INF/views/order/history.jsp").forward(request, response);
        }
    }
}
