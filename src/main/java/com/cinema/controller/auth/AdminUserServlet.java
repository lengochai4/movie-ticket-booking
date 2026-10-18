package com.cinema.controller.auth;

import com.cinema.dao.auth.UserDAO;
import com.cinema.entity.User;
import com.cinema.order.LoginUserSupport;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/admin/users", "/admin/users/toggle-status", "/admin/users/update-role"})
public class AdminUserServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        Object loginUser = session != null ? session.getAttribute("loginUser") : null;

        // Kiểm tra đăng nhập và quyền Admin/Staff
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        if (!LoginUserSupport.isAdminOrStaff(loginUser)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang quản trị người dùng.");
            return;
        }

        String keyword = request.getParameter("keyword");
        String roleFilter = request.getParameter("role");
        String statusFilter = request.getParameter("status");

        List<User> userList = userDAO.filterUsers(keyword, roleFilter, statusFilter);
        int totalUsers = userDAO.countAll();
        int userCount = userDAO.countByRole("USER");
        int adminCount = userDAO.countByRole("ADMIN");
        int lockedCount = userDAO.countByStatus("LOCKED");

        request.setAttribute("userList", userList);
        request.setAttribute("totalUsers", totalUsers);
        request.setAttribute("userCount", userCount);
        request.setAttribute("adminCount", adminCount);
        request.setAttribute("lockedCount", lockedCount);

        request.getRequestDispatcher("/WEB-INF/views/auth/admin/users.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        Object loginUser = session != null ? session.getAttribute("loginUser") : null;

        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        if (!LoginUserSupport.isAdminOrStaff(loginUser)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thao tác.");
            return;
        }

        String path = request.getServletPath();

        // 1. Xử lý Khóa / Mở khóa
        if ("/admin/users/toggle-status".equals(path)) {
            String userId = request.getParameter("userId");
            if (userId != null && !userId.isBlank()) {
                User user = userDAO.findById(userId.trim());
                if (user != null) {
                    String current = user.getStatus();
                    String next = "ACTIVE".equalsIgnoreCase(current) ? "LOCKED" : "ACTIVE";
                    user.setStatus(next);
                    userDAO.update(user);

                    session.setAttribute("successMessage",
                            "Đã " + (next.equals("ACTIVE") ? "MỞ KHÓA" : "KHÓA") + " tài khoản [" + user.getFullName() + "] thành công!");
                } else {
                    session.setAttribute("errorMessage", "Không tìm thấy người dùng cần thao tác.");
                }
            }
        }
        // 2. Xử lý Cập nhật vai trò & Trạng thái từ Modal
        else if ("/admin/users/update-role".equals(path)) {
            String userId = request.getParameter("userId");
            String newRole = request.getParameter("role");
            String newStatus = request.getParameter("status");

            if (userId != null && !userId.isBlank()) {
                User user = userDAO.findById(userId.trim());
                if (user != null) {
                    if (newRole != null && !newRole.isBlank()) {
                        user.setRole(newRole.trim());
                    }
                    if (newStatus != null && !newStatus.isBlank()) {
                        user.setStatus(newStatus.trim());
                    }
                    userDAO.update(user);

                    session.setAttribute("successMessage",
                            "Cập nhật quyền và trạng thái cho [" + user.getFullName() + "] thành công!");
                } else {
                    session.setAttribute("errorMessage", "Không tìm thấy người dùng cần cập nhật.");
                }
            }
        }

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }
}
