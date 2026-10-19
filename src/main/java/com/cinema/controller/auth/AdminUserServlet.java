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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet(urlPatterns = {"/admin/users", "/admin/users/toggle-status", "/admin/users/update-role"})
public class AdminUserServlet extends HttpServlet {

    private static final Set<String> ALLOWED_ROLES = new HashSet<>(Arrays.asList("ADMIN", "STAFF", "USER"));
    private static final Set<String> ALLOWED_STATUSES = new HashSet<>(Arrays.asList("ACTIVE", "LOCKED"));

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
        int staffCount = userDAO.countByRole("STAFF");
        int adminCount = userDAO.countByRole("ADMIN");
        int lockedCount = userDAO.countByStatus("LOCKED");

        request.setAttribute("userList", userList);
        request.setAttribute("totalUsers", totalUsers);
        request.setAttribute("userCount", userCount);
        request.setAttribute("staffCount", staffCount);
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

        String operatorId = LoginUserSupport.getUserId(loginUser);
        String operatorRole = LoginUserSupport.getRole(loginUser);
        boolean isOperatorAdmin = "ADMIN".equalsIgnoreCase(operatorRole);

        String path = request.getServletPath();

        // 1. Xử lý Khóa / Mở khóa
        if ("/admin/users/toggle-status".equals(path)) {
            String userId = request.getParameter("userId");
            if (userId != null && !userId.isBlank()) {
                String targetId = userId.trim();

                // A3: Không cho phép tự khóa chính mình
                if (targetId.equals(operatorId)) {
                    session.setAttribute("errorMessage", "Bạn không thể tự khóa tài khoản của chính mình!");
                    response.sendRedirect(request.getContextPath() + "/admin/users");
                    return;
                }

                User user = userDAO.findById(targetId);
                if (user != null) {
                    // Nhân viên (STAFF) không được phép thay đổi trạng thái của Quản trị viên (ADMIN)
                    if (!isOperatorAdmin && "ADMIN".equalsIgnoreCase(user.getRole())) {
                        session.setAttribute("errorMessage", "Nhân viên (STAFF) không có quyền thay đổi trạng thái của Quản trị viên (ADMIN)!");
                        response.sendRedirect(request.getContextPath() + "/admin/users");
                        return;
                    }

                    String current = user.getStatus();
                    String next = "ACTIVE".equalsIgnoreCase(current) ? "LOCKED" : "ACTIVE";

                    // A3: Không cho phép khóa ADMIN duy nhất còn lại
                    if ("LOCKED".equals(next) && "ADMIN".equalsIgnoreCase(user.getRole())) {
                        if (userDAO.countActiveAdmins() <= 1) {
                            session.setAttribute("errorMessage", "Không thể khóa Quản trị viên (ADMIN) duy nhất còn lại trong hệ thống!");
                            response.sendRedirect(request.getContextPath() + "/admin/users");
                            return;
                        }
                    }

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
            // A3: Chỉ ADMIN được đổi vai trò
            if (!isOperatorAdmin) {
                session.setAttribute("errorMessage", "Chỉ Quản trị viên (ADMIN) mới có quyền thay đổi vai trò người dùng!");
                response.sendRedirect(request.getContextPath() + "/admin/users");
                return;
            }

            String userId = request.getParameter("userId");
            String newRole = request.getParameter("role");
            String newStatus = request.getParameter("status");

            if (userId != null && !userId.isBlank()) {
                String targetId = userId.trim();
                User user = userDAO.findById(targetId);

                if (user == null) {
                    session.setAttribute("errorMessage", "Không tìm thấy người dùng cần cập nhật.");
                    response.sendRedirect(request.getContextPath() + "/admin/users");
                    return;
                }

                boolean isSelf = targetId.equals(operatorId);

                // A3: Giá trị role phải thuộc danh sách cố định (ADMIN, STAFF, USER)
                String normalizedRole = (newRole != null) ? newRole.trim().toUpperCase() : user.getRole();
                if ("CUSTOMER".equals(normalizedRole)) {
                    normalizedRole = "USER";
                }
                if (!ALLOWED_ROLES.contains(normalizedRole)) {
                    session.setAttribute("errorMessage", "Giá trị vai trò không hợp lệ! Chỉ chấp nhận ADMIN, STAFF, USER.");
                    response.sendRedirect(request.getContextPath() + "/admin/users");
                    return;
                }

                // A3: Giá trị status phải thuộc danh sách hợp lệ (ACTIVE, LOCKED)
                String normalizedStatus = (newStatus != null) ? newStatus.trim().toUpperCase() : user.getStatus();
                if (!ALLOWED_STATUSES.contains(normalizedStatus)) {
                    session.setAttribute("errorMessage", "Trạng thái không hợp lệ! Chỉ chấp nhận ACTIVE hoặc LOCKED.");
                    response.sendRedirect(request.getContextPath() + "/admin/users");
                    return;
                }

                // A3: Không cho tự hạ quyền chính mình
                if (isSelf && !"ADMIN".equals(normalizedRole)) {
                    session.setAttribute("errorMessage", "Bạn không thể tự hạ quyền Quản trị viên của chính mình!");
                    response.sendRedirect(request.getContextPath() + "/admin/users");
                    return;
                }

                // A3: Không cho tự khóa chính mình
                if (isSelf && "LOCKED".equals(normalizedStatus)) {
                    session.setAttribute("errorMessage", "Bạn không thể tự khóa tài khoản của chính mình!");
                    response.sendRedirect(request.getContextPath() + "/admin/users");
                    return;
                }

                // A3: Không cho hạ quyền ADMIN cuối cùng
                if ("ADMIN".equalsIgnoreCase(user.getRole()) && !"ADMIN".equals(normalizedRole)) {
                    if (userDAO.countActiveAdmins() <= 1) {
                        session.setAttribute("errorMessage", "Không thể hạ quyền Quản trị viên (ADMIN) duy nhất còn lại trong hệ thống!");
                        response.sendRedirect(request.getContextPath() + "/admin/users");
                        return;
                    }
                }

                // A3: Không cho khóa ADMIN cuối cùng
                if ("ADMIN".equalsIgnoreCase(user.getRole()) && "LOCKED".equals(normalizedStatus) && !"LOCKED".equalsIgnoreCase(user.getStatus())) {
                    if (userDAO.countActiveAdmins() <= 1) {
                        session.setAttribute("errorMessage", "Không thể khóa Quản trị viên (ADMIN) duy nhất còn lại trong hệ thống!");
                        response.sendRedirect(request.getContextPath() + "/admin/users");
                        return;
                    }
                }

                user.setRole(normalizedRole);
                user.setStatus(normalizedStatus);
                userDAO.update(user);

                session.setAttribute("successMessage",
                        "Cập nhật quyền và trạng thái cho [" + user.getFullName() + "] thành công!");
            }
        }

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }
}
