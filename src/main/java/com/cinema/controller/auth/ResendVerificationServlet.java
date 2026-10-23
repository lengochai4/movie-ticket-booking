package com.cinema.controller.auth;

import com.cinema.dao.auth.UserDAO;
import com.cinema.entity.User;
import com.cinema.util.MailUtilGmail;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Controller xử lý Gửi lại email kích hoạt tài khoản (Module Auth - Ân).
 * Hỗ trợ cả GET (click từ link thông báo) và POST (submit từ form).
 */
@WebServlet("/auth/resend-verification")
public class ResendVerificationServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleResend(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleResend(request, response);
    }

    private void handleResend(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");

        // 1. Kiểm tra email đầu vào
        if (email == null || email.isBlank()) {
            request.setAttribute("errorMessage", "Vui lòng cung cấp địa chỉ email để gửi lại liên kết kích hoạt.");
            request.getRequestDispatcher("/WEB-INF/views/auth/verify-notice.jsp").forward(request, response);
            return;
        }

        // 2. Tìm tài khoản trong cơ sở dữ liệu
        User user = userDAO.findByEmail(email.trim().toLowerCase());
        if (user == null) {
            request.setAttribute("errorMessage", "Không tìm thấy tài khoản với email này trong hệ thống.");
            request.getRequestDispatcher("/WEB-INF/views/auth/verify-notice.jsp").forward(request, response);
            return;
        }

        // 3. Nếu tài khoản đã kích hoạt từ trước -> chuyển đến trang đăng nhập
        if (!user.isPending() && user.isEmailVerified()) {
            response.sendRedirect(request.getContextPath() + "/auth/login?verified=true");
            return;
        }

        // 4. Cấp mã kích hoạt mới và gia hạn 24 giờ
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusHours(24));
        userDAO.update(user);

        // 5. Gửi email kích hoạt qua Gmail SMTPS
        try {
            String verifyUrl = buildVerificationUrl(request, token);
            String subject = "[Cinetik Cinema] Gửi lại liên kết kích hoạt tài khoản";
            String body = MailUtilGmail.buildVerificationEmail(user.getFullName(), verifyUrl);

            MailUtilGmail.sendMail(user.getEmail(), subject, body, true);
            request.setAttribute("resendSuccess", true);
        } catch (Exception e) {
            System.err.println("[ResendVerificationServlet] Lỗi gửi email: " + e.getMessage());
            request.setAttribute("emailError", "Không thể gửi email tự động vào lúc này. Vui lòng thử lại sau.");
        }

        // 6. Hiển thị lại trang thông báo
        request.setAttribute("user", user);
        request.setAttribute("email", user.getEmail());
        request.setAttribute("fullName", user.getFullName());
        request.getRequestDispatcher("/WEB-INF/views/auth/verify-notice.jsp").forward(request, response);
    }

    private String buildVerificationUrl(HttpServletRequest request, String token) {
        String baseUrl = request.getScheme() + "://" + request.getServerName();
        int port = request.getServerPort();
        if ((request.getScheme().equals("http") && port != 80) ||
            (request.getScheme().equals("https") && port != 443)) {
            baseUrl += ":" + port;
        }
        return baseUrl + request.getContextPath() + "/auth/verify?token=" + token;
    }
}

