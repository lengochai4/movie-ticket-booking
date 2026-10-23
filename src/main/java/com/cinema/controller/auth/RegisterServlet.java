package com.cinema.controller.auth;

import com.cinema.dao.auth.UserDAO;
import com.cinema.entity.User;
import com.cinema.util.MailUtilGmail;
import com.cinema.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@WebServlet("/auth/register")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // ── Bước 1: Tiếp nhận dữ liệu từ form ──
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String agreeTerms = request.getParameter("agreeTerms");

        // ── Bước 2: Kiểm tra tính hợp lệ dữ liệu ──
        if (isBlank(fullName) || isBlank(email) || isBlank(phone) || isBlank(password) || isBlank(confirmPassword)) {
            returnWithError(request, response, "Vui lòng điền đầy đủ các thông tin bắt buộc.");
            return;
        }

        if (isBlank(agreeTerms)) {
            returnWithError(request, response, "Bạn cần đồng ý với Điều khoản & Chính sách của rạp để đăng ký.");
            return;
        }

        if (!phone.trim().matches("^[0-9]{10}$")) {
            returnWithError(request, response, "Số điện thoại phải gồm đúng 10 chữ số.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            returnWithError(request, response, "Mật khẩu xác nhận không trùng khớp!");
            return;
        }

        if (!PasswordUtil.isStrong(password)) {
            returnWithError(request, response, "Mật khẩu phải có tối thiểu 8 ký tự, gồm cả chữ hoa, chữ thường, số và ký tự đặc biệt.");
            return;
        }

        // ── Bước 3: Kiểm tra sự tồn tại của Email trong hệ thống ──
        String cleanEmail = email.trim().toLowerCase();
        User existingUser = userDAO.findByEmail(cleanEmail);

        if (existingUser != null) {
            // Nếu tài khoản đã được kích hoạt -> không cho đăng ký đè
            if (!existingUser.isPending() && existingUser.isEmailVerified()) {
                returnWithError(request, response, "Email này đã được đăng ký. Vui lòng đăng nhập hoặc sử dụng email khác.");
                return;
            }

            // Nếu tài khoản vẫn ở trạng thái PENDING -> Cấp lại mã token mới và gửi lại email
            String token = UUID.randomUUID().toString();
            existingUser.setFullName(fullName.trim());
            existingUser.setPhone(phone.trim());
            existingUser.setPassword(PasswordUtil.hash(password));
            existingUser.setVerificationToken(token);
            existingUser.setTokenExpiry(LocalDateTime.now().plusHours(24));
            userDAO.update(existingUser);

            sendVerificationEmail(request, existingUser, token);
            forwardToNotice(request, response, existingUser);
            return;
        }

        // ── Bước 4: Tạo tài khoản mới ở trạng thái PENDING ──
        String token = UUID.randomUUID().toString();
        User newUser = new User();
        newUser.setId(UUID.randomUUID().toString());
        newUser.setFullName(fullName.trim());
        newUser.setEmail(cleanEmail);
        newUser.setPhone(phone.trim());
        newUser.setPassword(PasswordUtil.hash(password));
        newUser.setRole("USER");
        newUser.setStatus("PENDING");
        newUser.setEmailVerified(false);
        newUser.setVerificationToken(token);
        newUser.setTokenExpiry(LocalDateTime.now().plusHours(24));
        newUser.setLoyaltyPoints(0);
        newUser.setCreatedAt(LocalDateTime.now());

        boolean ok = userDAO.insert(newUser);
        if (!ok) {
            returnWithError(request, response, "Có lỗi xảy ra trong quá trình tạo tài khoản. Vui lòng thử lại sau.");
            return;
        }

        // ── Bước 5: Gửi email kích hoạt và chuyển sang trang thông báo ──
        sendVerificationEmail(request, newUser, token);
        forwardToNotice(request, response, newUser);
    }

    private void sendVerificationEmail(HttpServletRequest request, User user, String token) {
        try {
            String verifyUrl = buildVerificationUrl(request, token);
            String subject = "[Cinetik Cinema] Xác nhận kích hoạt tài khoản của bạn";
            String body = MailUtilGmail.buildVerificationEmail(user.getFullName(), verifyUrl);

            MailUtilGmail.sendMail(user.getEmail(), subject, body, true);
            request.setAttribute("emailSent", true);
        } catch (Exception e) {
            System.err.println("[RegisterServlet] Lỗi gửi email xác thực: " + e.getMessage());
            request.setAttribute("emailError", "Không thể gửi email kích hoạt tự động. Bạn có thể bấm gửi lại.");
        }
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

    private void returnWithError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("errorMessage", message);
        request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
    }

    private void forwardToNotice(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        request.setAttribute("user", user);
        request.setAttribute("email", user.getEmail());
        request.setAttribute("fullName", user.getFullName());
        request.getRequestDispatcher("/WEB-INF/views/auth/verify-notice.jsp").forward(request, response);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
