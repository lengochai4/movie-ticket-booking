package com.cinema.controller.auth;

import com.cinema.dao.auth.UserDAO;
import com.cinema.entity.User;
import com.cinema.service.order.LoyaltyService;
import com.cinema.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(urlPatterns = {"/auth/profile", "/auth/profile/update", "/auth/profile/change-password"})
public class ProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        Object loginUserObj = session != null ? session.getAttribute("loginUser") : null;

        if (loginUserObj == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // Tải thông tin mới nhất từ CSDL và đồng bộ điểm thưởng
        if (loginUserObj instanceof User) {
            User current = (User) loginUserObj;
            User fresh = userDAO.findById(current.getId());
            if (fresh != null) {
                try {
                    LoyaltyService loyaltyService = new LoyaltyService();
                    fresh.setLoyaltyPoints(loyaltyService.getPoints(fresh.getId()));
                } catch (Exception ignored) {
                }
                session.setAttribute("loginUser", fresh);
            }
        }
        request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        Object loginUserObj = session != null ? session.getAttribute("loginUser") : null;

        if (loginUserObj == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String path = request.getServletPath();
        String userId = null;
        if (loginUserObj instanceof User) {
            userId = ((User) loginUserObj).getId();
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // 1. Xử lý Cập nhật thông tin cá nhân
        if ("/auth/profile/update".equals(path)) {
            String fullName = request.getParameter("fullName");
            String phone = request.getParameter("phone");

            if (fullName == null || fullName.isBlank()) {
                request.setAttribute("errorMessage", "Họ và tên không được để trống.");
                request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
                return;
            }

            if (phone != null && !phone.isBlank() && !phone.trim().matches("^[0-9]{10}$")) {
                request.setAttribute("errorMessage", "Số điện thoại không hợp lệ (yêu cầu 10 chữ số).");
                request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
                return;
            }

            user.setFullName(fullName.trim());
            user.setPhone(phone != null ? phone.trim() : null);

            boolean ok = userDAO.update(user);
            if (ok) {
                try {
                    LoyaltyService loyaltyService = new LoyaltyService();
                    user.setLoyaltyPoints(loyaltyService.getPoints(user.getId()));
                } catch (Exception ignored) {
                }
                session.setAttribute("loginUser", user);
                request.setAttribute("successMessage", "Cập nhật thông tin tài khoản thành công!");
            } else {
                request.setAttribute("errorMessage", "Không thể lưu thay đổi. Vui lòng thử lại!");
            }
            request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
            return;
        }

        // 2. Xử lý Đổi mật khẩu
        if ("/auth/profile/change-password".equals(path)) {
            String currentPassword = request.getParameter("currentPassword");
            String newPassword = request.getParameter("newPassword");
            String confirmNewPassword = request.getParameter("confirmNewPassword");

            if (currentPassword == null || currentPassword.isBlank() ||
                    newPassword == null || newPassword.isBlank() ||
                    confirmNewPassword == null || confirmNewPassword.isBlank()) {
                request.setAttribute("errorMessage", "Vui lòng nhập đầy đủ các thông tin đổi mật khẩu.");
                request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
                return;
            }

            if (!PasswordUtil.verify(currentPassword, user.getPassword())) {
                request.setAttribute("errorMessage", "Mật khẩu hiện tại không chính xác!");
                request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
                return;
            }

            if (!newPassword.equals(confirmNewPassword)) {
                request.setAttribute("errorMessage", "Mật khẩu xác nhận không trùng khớp!");
                request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
                return;
            }

            if (!PasswordUtil.isStrong(newPassword)) {
                request.setAttribute("errorMessage", "Mật khẩu mới chưa đáp ứng tiêu chuẩn (tối thiểu 8 ký tự, gồm chữ hoa, thường, số và ký tự đặc biệt)!");
                request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
                return;
            }

            user.setPassword(PasswordUtil.hash(newPassword));
            boolean ok = userDAO.update(user);
            if (ok) {
                session.setAttribute("loginUser", user);
                request.setAttribute("successMessage", "Đổi mật khẩu thành công!");
            } else {
                request.setAttribute("errorMessage", "Không thể cập nhật mật khẩu. Vui lòng thử lại sau!");
            }
            request.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/auth/profile");
    }
}