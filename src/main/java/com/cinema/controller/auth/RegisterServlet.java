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
        if (userDAO.findByEmail(email.trim()) != null) {
            request.setAttribute("errorMessage", "Email này đã được đăng ký. Vui lòng đăng nhập hoặc sử dụng email khác.");
            request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
            return;
        }

        // 7. Tạo mới người dùng
        User newUser = new User();
        newUser.setId(UUID.randomUUID().toString());
        newUser.setFullName(fullName.trim());
        newUser.setEmail(email.trim().toLowerCase());
        newUser.setPhone(phone.trim());
        newUser.setPassword(PasswordUtil.hash(password));
        newUser.setRole("USER");
        newUser.setStatus("ACTIVE");
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

        // Đăng ký thành công -> chuyển sang trang đăng nhập kèm cờ thông báo
        response.sendRedirect(request.getContextPath() + "/auth/login?registered=true");
    }
}