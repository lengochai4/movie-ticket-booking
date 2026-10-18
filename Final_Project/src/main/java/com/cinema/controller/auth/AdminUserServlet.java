package com.cinema.controller.auth;

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

@WebServlet(urlPatterns = {"/admin/users", "/admin/users/toggle-status", "/admin/users/update-role"})
public class AdminUserServlet extends HttpServlet {

    // Danh sách người dùng giả lập trong bộ nhớ để test giao diện
    private static final List<Map<String, String>> MOCK_USERS = new ArrayList<>();

    static {
        addUser("1", "Nguyễn Thị Lan Phương", "thilanphuong24@gmail.com", "0988123456", "ADMIN", "ACTIVE", "01/10/2026");
        addUser("2", "Trần Hải Đăng", "haidang.cinetik@gmail.com", "0912345678", "USER", "ACTIVE", "05/10/2026");
        addUser("3", "Lê Gia Ân", "giaan.film@gmail.com", "0909876543", "USER", "ACTIVE", "08/10/2026");
        addUser("4", "Phạm Hoàng Nam", "hoangnam99@yahoo.com", "0934567890", "USER", "ACTIVE", "10/10/2026");
        addUser("5", "Vũ Thị Mai Chi", "maichi.vu@gmail.com", "0977889900", "USER", "LOCKED", "12/10/2026");
        addUser("6", "Đặng Quốc Huy", "quochuy.cinema@outlook.com", "0965432109", "USER", "ACTIVE", "14/10/2026");
    }

    private static void addUser(String id, String name, String email, String phone, String role, String status, String createdAt) {
        Map<String, String> u = new HashMap<>();
        u.put("id", id);
        u.put("fullName", name);
        u.put("email", email);
        u.put("phone", phone);
        u.put("role", role);
        u.put("status", status);
        u.put("createdAt", createdAt);
        MOCK_USERS.add(u);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("keyword");
        String roleFilter = request.getParameter("role");
        String statusFilter = request.getParameter("status");

        List<Map<String, String>> filteredList = new ArrayList<>();
        int userCount = 0;
        int adminCount = 0;
        int lockedCount = 0;

        for (Map<String, String> u : MOCK_USERS) {
            // Đếm thống kê chỉ cho 2 role: ADMIN và USER
            if ("ADMIN".equalsIgnoreCase(u.get("role"))) {
                adminCount++;
            } else {
                userCount++;
            }
            if ("LOCKED".equalsIgnoreCase(u.get("status"))) {
                lockedCount++;
            }

            // Lọc dữ liệu
            boolean matchKeyword = true;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.toLowerCase().trim();
                matchKeyword = u.get("fullName").toLowerCase().contains(kw)
                        || u.get("email").toLowerCase().contains(kw)
                        || u.get("phone").contains(kw);
            }

            boolean matchRole = true;
            if (roleFilter != null && !roleFilter.trim().isEmpty()) {
                matchRole = roleFilter.equalsIgnoreCase(u.get("role"));
            }

            boolean matchStatus = true;
            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                matchStatus = statusFilter.equalsIgnoreCase(u.get("status"));
            }

            if (matchKeyword && matchRole && matchStatus) {
                filteredList.add(u);
            }
        }

        request.setAttribute("userList", filteredList);
        request.setAttribute("totalUsers", MOCK_USERS.size());
        request.setAttribute("userCount", userCount);
        request.setAttribute("adminCount", adminCount);
        request.setAttribute("lockedCount", lockedCount);

        request.getRequestDispatcher("/WEB-INF/views/auth/admin/users.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        // 1. Xử lý Khóa / Mở khóa
        if ("/admin/users/toggle-status".equals(path)) {
            String userId = request.getParameter("userId");
            for (Map<String, String> u : MOCK_USERS) {
                if (u.get("id").equals(userId)) {
                    String current = u.get("status");
                    String next = "ACTIVE".equalsIgnoreCase(current) ? "LOCKED" : "ACTIVE";
                    u.put("status", next);
                    request.getSession().setAttribute("successMessage", 
                            "Đã " + (next.equals("ACTIVE") ? "MỞ KHÓA" : "KHÓA") + " tài khoản [" + u.get("fullName") + "] thành công!");
                    break;
                }
            }
        }
        // 2. Xử lý Cập nhật vai trò (Phân quyền) & Trạng thái từ Modal
        else if ("/admin/users/update-role".equals(path)) {
            String userId = request.getParameter("userId");
            String newRole = request.getParameter("role");
            String newStatus = request.getParameter("status");

            for (Map<String, String> u : MOCK_USERS) {
                if (u.get("id").equals(userId)) {
                    u.put("role", newRole);
                    u.put("status", newStatus);
                    request.getSession().setAttribute("successMessage", 
                            "Cập nhật quyền và trạng thái cho [" + u.get("fullName") + "] thành công!");
                    break;
                }
            }
        }

        // Chuyển hướng quay lại danh sách
        response.sendRedirect(request.getContextPath() + "/admin/users");
    }
}
