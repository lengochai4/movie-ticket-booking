package com.cinema.controller.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/auth/profile")
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // 1. TẠO DỮ LIỆU GIẢ LẬP (MOCK DATA) ĐỂ TEST GIAO DIỆN
        // Nếu chưa có loginUser trong session, ta tự tạo 1 User mẫu
        if (session.getAttribute("loginUser") == null) {
            Map<String, String> mockUser = new HashMap<>();
            mockUser.put("id", "1");
            mockUser.put("fullName", "Nguyễn Thị Lan Phương");
            mockUser.put("email", "thilanphuong24@gmail.com");
            mockUser.put("phone", "0988123456");
            mockUser.put("role", "ADMIN");
            session.setAttribute("loginUser", mockUser);
        }

        // 2. Chuyển hướng sang giao diện profile.jsp trong WEB-INF
        request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Giả lập xử lý khi nhấn nút "Lưu thay đổi" hoặc "Đổi mật khẩu"
        request.setCharacterEncoding("UTF-8");
        request.setAttribute("successMessage", "Cập nhật dữ liệu thành công (Chế độ Test)!");

        request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
    }
}