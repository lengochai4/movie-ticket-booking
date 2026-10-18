package com.cinema.service.auth;

import com.cinema.dao.auth.UserDAO;
import com.cinema.model.entity.User;
import com.cinema.util.PasswordUtil;

import java.sql.SQLException;

/**
 * AuthService - Service xử lý nghiệp vụ xác thực người dùng.
 * Bao gồm: Đăng ký, Đăng nhập.
 */
public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Đăng ký tài khoản mới.
     * Kiểm tra trùng email, băm mật khẩu, lưu User với role=0 và loyaltyPoints=0.
     *
     * @param fullName Họ và tên
     * @param email    Email (unique)
     * @param phone    Số điện thoại
     * @param password Mật khẩu thuần văn bản
     * @return true nếu đăng ký thành công, false nếu email đã tồn tại hoặc lỗi validate
     */
    public boolean register(String fullName, String email, String phone, String password) {
        // Validate đầu vào
        if (fullName == null || fullName.trim().isEmpty()) return false;
        if (email == null || email.trim().isEmpty() || !email.contains("@")) return false;
        if (password == null || password.length() < 6) return false;

        try {
            // Kiểm tra trùng email
            if (userDAO.existsByEmail(email.trim().toLowerCase())) {
                return false;
            }
            // Tạo User mới
            User newUser = new User();
            newUser.setFullName(fullName.trim());
            newUser.setEmail(email.trim().toLowerCase());
            newUser.setPhone(phone != null ? phone.trim() : "");
            newUser.setPasswordHash(PasswordUtil.hashPassword(password));
            newUser.setRole(0); // User
            newUser.setLoyaltyPoints(0);
            newUser.setLocked(false);

            return userDAO.insert(newUser);
        } catch (SQLException e) {
            System.err.println("[AuthService] Lỗi đăng ký: " + e.getMessage());
            return false;
        }
    }

    /**
     * Đăng nhập: kiểm tra email và mật khẩu, trả về đối tượng User.
     * Kiểm tra thêm tài khoản có bị khoá không.
     *
     * @param email    Email người dùng
     * @param password Mật khẩu thuần văn bản
     * @return User nếu đăng nhập thành công, null nếu sai hoặc bị khoá
     */
    public User login(String email, String password) {
        if (email == null || password == null) return null;
        try {
            User user = userDAO.findByEmail(email.trim().toLowerCase());
            if (user == null) return null;
            if (user.isLocked()) return null; // Tài khoản bị khoá
            if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) return null;
            return user;
        } catch (SQLException e) {
            System.err.println("[AuthService] Lỗi đăng nhập: " + e.getMessage());
            return null;
        }
    }
}
