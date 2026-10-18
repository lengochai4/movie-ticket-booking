package com.cinema.service.auth;

import com.cinema.dao.auth.UserDAO;
import com.cinema.model.entity.User;
import com.cinema.util.PasswordUtil;

import java.sql.SQLException;

/**
 * UserService - Service xử lý nghiệp vụ liên quan đến User.
 * Bao gồm: khoá/mở khoá tài khoản, cập nhật hồ sơ, đổi mật khẩu.
 * <p>
 * NOTE: Các signature lock/unlock được chốt từ Ngày 1 để Ân dùng ở Ngày 3.
 */
public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    // ==================== NGÀY 1: SIGNATURE CHỐT ====================

    /**
     * Khoá tài khoản người dùng (ngăn đăng nhập).
     * Admin không thể tự khoá chính mình.
     *
     * @param userID ID người dùng cần khoá
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean lock(int userID) {
        try {
            return userDAO.setLockedStatus(userID, true);
        } catch (SQLException e) {
            System.err.println("[UserService] Lỗi khoá tài khoản userID=" + userID + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Mở khoá tài khoản người dùng (cho phép đăng nhập lại).
     *
     * @param userID ID người dùng cần mở khoá
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean unlock(int userID) {
        try {
            return userDAO.setLockedStatus(userID, false);
        } catch (SQLException e) {
            System.err.println("[UserService] Lỗi mở khoá tài khoản userID=" + userID + ": " + e.getMessage());
            return false;
        }
    }

    // ==================== NGÀY 2: PROFILE & PASSWORD ====================

    /**
     * Cập nhật thông tin hồ sơ cá nhân (fullName, phone).
     *
     * @param userID   ID người dùng
     * @param fullName Tên hiển thị mới
     * @param phone    Số điện thoại mới
     * @return User đã cập nhật, hoặc null nếu thất bại
     */
    public User updateProfile(int userID, String fullName, String phone) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return null;
        }
        try {
            User user = userDAO.findById(userID);
            if (user == null) return null;
            user.setFullName(fullName.trim());
            user.setPhone(phone != null ? phone.trim() : "");
            boolean success = userDAO.updateProfile(userID, fullName.trim(), phone != null ? phone.trim() : "");
            return success ? userDAO.findById(userID) : null;
        } catch (SQLException e) {
            System.err.println("[UserService] Lỗi cập nhật profile userID=" + userID + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Đổi mật khẩu: xác thực mật khẩu cũ rồi băm mật khẩu mới.
     *
     * @param userID  ID người dùng
     * @param oldPass Mật khẩu cũ (plain text)
     * @param newPass Mật khẩu mới (plain text)
     * @return true nếu đổi thành công, false nếu sai mật khẩu cũ hoặc lỗi
     */
    public boolean changePassword(int userID, String oldPass, String newPass) {
        if (oldPass == null || newPass == null || newPass.length() < 6) {
            return false;
        }
        try {
            User user = userDAO.findById(userID);
            if (user == null) return false;
            if (!PasswordUtil.verifyPassword(oldPass, user.getPasswordHash())) {
                return false; // Sai mật khẩu cũ
            }
            String newHash = PasswordUtil.hashPassword(newPass);
            return userDAO.updatePassword(userID, newHash);
        } catch (SQLException e) {
            System.err.println("[UserService] Lỗi đổi mật khẩu userID=" + userID + ": " + e.getMessage());
            return false;
        }
    }

    // ==================== NGÀY 3: ADMIN USER MANAGEMENT ====================

    /**
     * Lấy danh sách tất cả người dùng trong hệ thống.
     *
     * @return Danh sách User
     */
    public java.util.List<User> getAllUsers() {
        try {
            return userDAO.findAllUsers();
        } catch (SQLException e) {
            System.err.println("[UserService] Lỗi lấy danh sách user: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Tìm User theo ID.
     *
     * @param userID ID người dùng
     * @return User hoặc null
     */
    public User findById(int userID) {
        try {
            return userDAO.findById(userID);
        } catch (SQLException e) {
            System.err.println("[UserService] Lỗi tìm user by ID: " + e.getMessage());
            return null;
        }
    }
}
