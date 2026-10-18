package com.cinema.controller.catalog;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/admin/combos", "/admin/combos/save", "/admin/combos/delete"})
public class AdminComboServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        // Chuyển tiếp tới giao diện quản lý combo bắp nước
        request.getRequestDispatcher("/WEB-INF/views/admin/combos.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        if ("/admin/combos/save".equals(path)) {
            String comboId = request.getParameter("comboID");
            String name = request.getParameter("name");
            String price = request.getParameter("price");
            System.out.println("[ADMIN COMBO] Đã lưu combo: " + name + " (ID: " + comboId + ", Giá: " + price + ")");
            response.sendRedirect(request.getContextPath() + "/admin/combos");
            return;
        } else if ("/admin/combos/delete".equals(path)) {
            String comboId = request.getParameter("comboID");
            System.out.println("[ADMIN COMBO] Đã xóa combo ID: " + comboId);
            response.sendRedirect(request.getContextPath() + "/admin/combos");
            return;
        }

        doGet(request, response);
    }
}
