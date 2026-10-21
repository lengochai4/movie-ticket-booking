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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@WebServlet("/auth/register")
public class RegisterServlet extends HttpServlet {

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

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String agreeTerms = request.getParameter("agreeTerms");

        // 1. Kiểm tra thông tin rỗng
        if (fullName == null || fullName.isBlank() ||
                email == null || email.isBlank() ||
                phone == null || phone.isBlank() ||
                password == null || password.isBlank() ||
                confirmPassword == null || confirmPassword.isBlank()) {
            request.setAttribute("errorMessage", "Vui lòng điền đầy đủ các thông tin bắt buộc.");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
            return;
        }

        // 2. Kiểm tra điều khoản
        if (agreeTerms == null || agreeTerms.isBlank()) {
            request.setAttribute("errorMessage", "Bạn cần đồng ý với Điều khoản & Chính sách của rạp để đăng ký.");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
            return;
        }

        // 3. Kiểm tra số điện thoại (10 chữ số)
        if (!phone.trim().matches("^[0-9]{10}$")) {
            request.setAttribute("errorMessage", "Số điện thoại phải gồm đúng 10 chữ số.");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
            return;
        }

        // 4. Kiểm tra xác nhận mật khẩu
        if (!password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Mật khẩu xác nhận không trùng khớp!");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
            return;
        }

        // 5. Kiểm tra độ mạnh của mật khẩu
        if (!PasswordUtil.isStrong(password)) {
            request.setAttribute("errorMessage", "Mật khẩu phải có tối thiểu 8 ký tự, gồm cả chữ hoa, chữ thường, số và ký tự đặc biệt.");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
            return;
        }

        // 6. Kiểm tra email đã tồn tại hay chưa
        User existingUser = userDAO.findByEmail(email.trim());
        if (existingUser != null) {
            // Nếu tài khoản đã kích hoạt -> báo lỗi đã tồn tại
            if (!"PENDING".equalsIgnoreCase(existingUser.getStatus()) && existingUser.isEmailVerified()) {
                request.setAttribute("errorMessage", "Email này đã được đăng ký. Vui lòng đăng nhập hoặc sử dụng email khác.");
                request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
                return;
            }

            // Nếu tài khoản đang PENDING -> cấp lại token mới và gửi lại email
            String token = UUID.randomUUID().toString();
            existingUser.setFullName(fullName.trim());
            existingUser.setPhone(phone.trim());
            existingUser.setPassword(PasswordUtil.hash(password));
            existingUser.setVerificationToken(token);
            existingUser.setTokenExpiry(LocalDateTime.now().plusHours(24));
            userDAO.update(existingUser);

            sendVerificationEmail(request, existingUser, token);
            request.setAttribute("user", existingUser);
            request.setAttribute("email", existingUser.getEmail());
            request.setAttribute("fullName", existingUser.getFullName());
            request.getRequestDispatcher("/WEB-INF/views/auth/verify-notice.jsp").forward(request, response);
            return;
        }

        // 7. Tạo mới người dùng ở trạng thái PENDING chờ xác thực email
        String token = UUID.randomUUID().toString();
        User newUser = new User();
        newUser.setId(UUID.randomUUID().toString());
        newUser.setFullName(fullName.trim());
        newUser.setEmail(email.trim().toLowerCase());
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
            request.setAttribute("errorMessage", "Có lỗi xảy ra trong quá trình tạo tài khoản. Vui lòng thử lại sau.");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
            return;
        }

        // Khởi tạo sổ điểm thưởng nếu có LoyaltyService
        try {
            LoyaltyService loyaltyService = new LoyaltyService();
            loyaltyService.getPoints(newUser.getId());
        } catch (Exception ignored) {
        }

        // 8. Gửi Gmail xác nhận tài khoản
        sendVerificationEmail(request, newUser, token);

        request.setAttribute("user", newUser);
        request.setAttribute("email", newUser.getEmail());
        request.setAttribute("fullName", newUser.getFullName());
        request.getRequestDispatcher("/WEB-INF/views/auth/verify-notice.jsp").forward(request, response);
    }

    private void sendVerificationEmail(HttpServletRequest request, User user, String token) {
        try {
            String baseUrl = request.getScheme() + "://" + request.getServerName();
            int port = request.getServerPort();
            if ((request.getScheme().equals("http") && port != 80) ||
                (request.getScheme().equals("https") && port != 443)) {
                baseUrl += ":" + port;
            }
            baseUrl += request.getContextPath();

            String verifyUrl = baseUrl + "/auth/verify?token=" + token;
            String to = user.getEmail();
            String from = "thienantranit@gmail.com";
            String subject = "[Cinetik Cinema] Xác nhận kích hoạt tài khoản của bạn";
            String body = com.cinema.util.MailUtilGmail.buildVerificationEmail(user.getFullName(), verifyUrl);

            com.cinema.util.MailUtilGmail.sendMail(to, from, subject, body, true);
            request.setAttribute("emailSent", true);
        } catch (Exception e) {
            System.err.println("[RegisterServlet] Lỗi gửi email xác thực: " + e.getMessage());
            request.setAttribute("emailError", "Không thể gửi email kích hoạt tự động. Bạn có thể bấm gửi lại.");
        }
    }
}