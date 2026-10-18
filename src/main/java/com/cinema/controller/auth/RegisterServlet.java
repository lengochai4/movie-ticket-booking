package com.cinema.controller.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/auth/register")
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
    }

    // BỔ SUNG HÀM NÀY ĐỂ HẾT BỊ LỖI 405 KHI BẤM NÚT ĐĂNG KÝ:
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Tạm thời giả lập đăng ký thành công -> chuyển sang trang login
        response.sendRedirect(request.getContextPath() + "/auth/login?registered=true");
    }
}