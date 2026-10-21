package com.cinema.controller.auth;

import com.cinema.dao.auth.UserDAO;
import com.cinema.entity.User;
import com.cinema.util.MailUtil;
import com.cinema.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.SecureRandom;

@WebServlet("/auth/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loginUser") != null) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        // Nếu người dùng chọn đổi tài khoản hoặc bắt đầu lại
        String restart = request.getParameter("restart");
        if ("true".equalsIgnoreCase(restart) && session != null) {
            clearResetSession(session);
        }

        prepareStepView(request);
        request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(true);
        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "send-otp";
        }

        switch (action) {
            case "send-otp" -> handleSendOtp(request, response, session);
            case "resend-otp" -> handleResendOtp(request, response, session);
            case "reset-password" -> handleResetPassword(request, response, session);
            default -> {
                prepareStepView(request);
                request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            }
        }
    }

    private void handleSendOtp(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ServletException, IOException {
        String identifier = request.getParameter("identifier");
        if (identifier == null || identifier.isBlank()) {
            request.setAttribute("errorMessage", "Vui lòng nhập email hoặc số điện thoại đã đăng ký.");
            request.setAttribute("step", "email");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        User user = userDAO.findByEmailOrPhone(identifier.trim());
        if (user == null) {
            request.setAttribute("errorMessage", "Không tìm thấy tài khoản tương ứng với thông tin đã nhập.");
            request.setAttribute("step", "email");
            request.setAttribute("identifier", identifier.trim());
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        if (user.isLocked() || "LOCKED".equalsIgnoreCase(user.getStatus())) {
            request.setAttribute("errorMessage", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ ban quản trị để được hỗ trợ.");
            request.setAttribute("step", "email");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        String email = user.getEmail();
        if (email == null || email.isBlank() || !email.contains("@")) {
            request.setAttribute("errorMessage", "Tài khoản chưa có địa chỉ email hợp lệ để nhận mã xác thực.");
            request.setAttribute("step", "email");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        // Tạo mã OTP ngẫu nhiên 6 chữ số
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        long expiry = System.currentTimeMillis() + (10 * 60 * 1000L); // Hiệu lực 10 phút

        session.setAttribute("resetUserId", user.getId());
        session.setAttribute("resetEmail", email.trim().toLowerCase());
        session.setAttribute("resetOtp", otp);
        session.setAttribute("resetOtpExpiry", expiry);

        // Gửi email chứa mã OTP
        sendOtpEmail(email, user.getFullName(), otp);

        // In log ra server console để hỗ trợ kiểm thử nhanh
        System.out.println("🔐 [ForgotPassword] Mã OTP cho " + email + ": " + otp);

        String masked = maskEmail(email);
        request.setAttribute("step", "verify");
        request.setAttribute("maskedEmail", masked);
        request.setAttribute("successMessage", "Mã xác thực OTP gồm 6 chữ số đã được gửi đến email " + masked + ". Vui lòng kiểm tra hộp thư (hoặc mục Spam).");
        request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
    }

    private void handleResendOtp(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ServletException, IOException {
        String userId = (String) session.getAttribute("resetUserId");
        String email = (String) session.getAttribute("resetEmail");

        if (userId == null || email == null) {
            request.setAttribute("errorMessage", "Phiên làm việc đã hết hạn. Vui lòng nhập lại thông tin tài khoản.");
            request.setAttribute("step", "email");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        User user = userDAO.findById(userId);
        String fullName = (user != null) ? user.getFullName() : "Quý khách";

        String newOtp = String.format("%06d", secureRandom.nextInt(1_000_000));
        long expiry = System.currentTimeMillis() + (10 * 60 * 1000L);

        session.setAttribute("resetOtp", newOtp);
        session.setAttribute("resetOtpExpiry", expiry);

        sendOtpEmail(email, fullName, newOtp);
        System.out.println("🔐 [ForgotPassword] Gửi lại mã OTP mới cho " + email + ": " + newOtp);

        request.setAttribute("step", "verify");
        request.setAttribute("maskedEmail", maskEmail(email));
        request.setAttribute("successMessage", "Mã OTP mới đã được gửi lại vào email " + maskEmail(email) + ".");
        request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws ServletException, IOException {
        String userId = (String) session.getAttribute("resetUserId");
        String email = (String) session.getAttribute("resetEmail");
        String expectedOtp = (String) session.getAttribute("resetOtp");
        Long expiry = (Long) session.getAttribute("resetOtpExpiry");

        if (userId == null || email == null || expectedOtp == null || expiry == null) {
            request.setAttribute("errorMessage", "Phiên xác thực đã hết hạn. Vui lòng thực hiện lại từ bước đầu.");
            clearResetSession(session);
            request.setAttribute("step", "email");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        String masked = maskEmail(email);
        request.setAttribute("maskedEmail", masked);
        request.setAttribute("step", "verify");

        // Kiểm tra thời gian hết hạn của OTP
        if (System.currentTimeMillis() > expiry) {
            request.setAttribute("errorMessage", "Mã OTP đã hết hiệu lực (quá 10 phút). Vui lòng nhấn 'Gửi lại mã OTP'.");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        String otpInput = request.getParameter("otp");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (otpInput == null || otpInput.isBlank() ||
                newPassword == null || newPassword.isBlank() ||
                confirmPassword == null || confirmPassword.isBlank()) {
            request.setAttribute("errorMessage", "Vui lòng nhập đầy đủ mã OTP và mật khẩu mới.");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        if (!expectedOtp.equals(otpInput.trim())) {
            request.setAttribute("errorMessage", "Mã OTP không chính xác. Vui lòng kiểm tra lại.");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Mật khẩu xác nhận không trùng khớp!");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        if (!PasswordUtil.isStrong(newPassword)) {
            request.setAttribute("errorMessage", "Mật khẩu mới phải có tối thiểu 8 ký tự, bao gồm cả chữ hoa, chữ thường, chữ số và ký tự đặc biệt.");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            request.setAttribute("errorMessage", "Không tìm thấy thông tin tài khoản người dùng.");
            clearResetSession(session);
            request.setAttribute("step", "email");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        // Cập nhật mật khẩu mới đã băm
        user.setPassword(PasswordUtil.hash(newPassword));
        boolean success = userDAO.update(user);

        if (!success) {
            request.setAttribute("errorMessage", "Không thể cập nhật mật khẩu mới vào cơ sở dữ liệu. Vui lòng thử lại sau.");
            request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        // Xóa thông tin reset khỏi session
        clearResetSession(session);

        // Chuyển hướng về trang đăng nhập kèm cờ reset thành công
        response.sendRedirect(request.getContextPath() + "/auth/login?reset=success");
    }

    private void prepareStepView(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("resetUserId") != null && session.getAttribute("resetOtp") != null) {
            String email = (String) session.getAttribute("resetEmail");
            request.setAttribute("step", "verify");
            request.setAttribute("maskedEmail", maskEmail(email));
        } else {
            request.setAttribute("step", "email");
        }
    }

    private void clearResetSession(HttpSession session) {
        session.removeAttribute("resetUserId");
        session.removeAttribute("resetEmail");
        session.removeAttribute("resetOtp");
        session.removeAttribute("resetOtpExpiry");
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@", 2);
        String name = parts[0];
        String domain = parts[1];
        if (name.length() <= 2) {
            return name.charAt(0) + "***@" + domain;
        }
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + "@" + domain;
    }

    private void sendOtpEmail(String toEmail, String fullName, String otp) {
        String subject = "[Cinetik Cinema] Mã xác nhận đặt lại mật khẩu";
        String displayName = (fullName != null && !fullName.isBlank()) ? fullName : "Quý khách";

        String htmlBody = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="margin: 0; padding: 20px; background-color: #0d0b0a; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                    <table width="100%%" border="0" cellspacing="0" cellpadding="0" style="max-width: 540px; margin: 0 auto; background-color: #161413; border: 1px solid rgba(252, 202, 70, 0.3); border-radius: 12px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.7);">
                        <tr>
                            <td style="background-color: #780000; padding: 24px; text-align: center; border-bottom: 2px solid #fcca46;">
                                <h1 style="color: #fcca46; margin: 0; font-size: 26px; letter-spacing: 2px; text-transform: uppercase;">CINETIK CINEMA</h1>
                                <p style="color: #f6ead2; margin: 6px 0 0 0; font-size: 13px; opacity: 0.9;">Hệ Thống Đặt Vé Xem Phim Trực Tuyến</p>
                            </td>
                        </tr>
                        <tr>
                            <td style="padding: 32px 28px; color: #f6ead2; line-height: 1.6;">
                                <p style="font-size: 16px; margin: 0 0 16px 0;">Xin chào <strong>%s</strong>,</p>
                                <p style="font-size: 14px; color: #b5a995; margin: 0 0 24px 0;">
                                    Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản Cinetik của bạn. Vui lòng nhập mã xác thực (OTP) dưới đây để tiến hành thiết lập mật khẩu mới:
                                </p>
                                <div style="text-align: center; margin: 28px 0;">
                                    <div style="display: inline-block; padding: 14px 28px; background-color: #23201e; border: 2px dashed #fcca46; border-radius: 10px;">
                                        <span style="font-size: 32px; font-weight: 700; letter-spacing: 8px; color: #fcca46; font-family: monospace;">%s</span>
                                    </div>
                                </div>
                                <div style="background-color: rgba(120, 0, 0, 0.2); border-left: 4px solid #c92a2a; padding: 12px 14px; border-radius: 4px; margin-bottom: 20px;">
                                    <p style="margin: 0; font-size: 13px; color: #ff8787;">
                                        ⏳ Mã xác thực có hiệu lực trong vòng <strong>10 phút</strong>. Tuyệt đối không chia sẻ mã này với bất kỳ ai để bảo vệ an toàn cho tài khoản của bạn.
                                    </p>
                                </div>
                                <p style="font-size: 13px; color: #8c8275; margin: 0;">
                                    Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email hoặc đổi mật khẩu ngay để đảm bảo an toàn.
                                </p>
                            </td>
                        </tr>
                        <tr>
                            <td style="background-color: #12100f; padding: 16px; text-align: center; border-top: 1px solid rgba(255,255,255,0.06);">
                                <p style="margin: 0; font-size: 12px; color: #6e6459;">
                                    © Cinetik Cinema. Email tự động, vui lòng không phản hồi.
                                </p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(displayName, otp);

        try {
            MailUtil.send(toEmail, subject, htmlBody);
        } catch (Exception e) {
            System.err.println("[ForgotPassword] Lỗi gửi email OTP: " + e.getMessage());
        }
    }
}
