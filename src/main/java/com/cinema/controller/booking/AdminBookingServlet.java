package com.cinema.controller.booking;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/admin/bookings", "/admin/bookings/cancel"})
public class AdminBookingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        // Chuyển tiếp tới giao diện bookings.jsp
        request.getRequestDispatcher("/WEB-INF/views/order/admin/bookings.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        if ("/admin/bookings/cancel".equals(path)) {
            String orderCode = request.getParameter("orderCode");
            String reason = request.getParameter("cancelReason");
            System.out.println("[ADMIN] Đã xác nhận hủy vé: " + orderCode + " - Lý do: " + reason);
            response.sendRedirect(request.getContextPath() + "/admin/bookings");
            return;
        }

        doGet(request, response);
    }
}
