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

@WebServlet("/auth/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 1. Kiểm tra nếu người dùng đã đăng nhập từ trước
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loginUser") != null) {
            Object obj = session.getAttribute("loginUser");
            if (obj instanceof User user) {
                // Kiểm tra trạng thái mới nhất từ DB
                User freshUser = userDAO.findById(user.getId());
                if (freshUser != null && !freshUser.isLocked()) {
                    response.sendRedirect(request.getContextPath() + "/movies");
                    return;
                }
            }
            // Nếu tài khoản không còn hợp lệ, xóa session cũ
            session.invalidate();
        }

        // 2. Chuyển tiếp tới trang giao diện đăng nhập
        request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // ── Bước 1: Tiếp nhận dữ liệu từ form ──
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String redirect = request.getParameter("redirect");

        // ── Bước 2: Kiểm tra dữ liệu đầu vào ──
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            returnWithError(request, response, "Vui lòng nhập đầy đủ email/số điện thoại và mật khẩu.");
            return;
        }

        // ── Bước 3: Tìm tài khoản theo Email hoặc Số điện thoại ──
        User user = userDAO.findByEmailOrPhone(username.trim());
        if (user == null || !PasswordUtil.verify(password, user.getPassword())) {
            returnWithError(request, response, "Email/số điện thoại hoặc mật khẩu không chính xác.");
            return;
        }

        // ── Bước 4: Kiểm tra trạng thái tài khoản ──
        if (user.isLocked()) {
            returnWithError(request, response, "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ ban quản trị.");
            return;
        }

        if (user.isPending() || !user.isEmailVerified()) {
            request.setAttribute("unverifiedEmail", user.getEmail());
            returnWithError(request, response, "Tài khoản chưa được kích hoạt email. Vui lòng kiểm tra hộp thư hoặc bấm gửi lại liên kết kích hoạt.");
            return;
        }

        // ── Bước 5: Đăng nhập thành công -> Lưu thông tin vào Session ──
        // Đồng bộ điểm thưởng thành viên nếu có
        try {
            LoyaltyService loyaltyService = new LoyaltyService();
            user.setLoyaltyPoints(loyaltyService.getPoints(user.getId()));
        } catch (Exception ignored) {
        }

        // Đổi session ID để chống tấn công cố định phiên (Session Fixation)
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute("loginUser", user);

        // ── Bước 6: Điều hướng người dùng sau khi đăng nhập ──
        if (isValidRedirectUrl(request, redirect)) {
            response.sendRedirect(redirect.startsWith("/") ? request.getContextPath() + redirect : redirect);
        } else {
            response.sendRedirect(request.getContextPath() + "/movies");
        }
    }

    private void returnWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("errorMessage", message);
        request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
    }

    /**
     * Kiểm tra URL chuyển hướng an toàn, ngăn chặn Open Redirect
     */
    private boolean isValidRedirectUrl(HttpServletRequest request, String redirect) {
        if (redirect == null || redirect.isBlank()) {
            return false;
        }
        String contextPath = request.getContextPath();
        if (redirect.startsWith(contextPath + "/") || redirect.equals(contextPath)) {
            return true;
        }
        return redirect.startsWith("/") && !redirect.startsWith("//");
    }
}
