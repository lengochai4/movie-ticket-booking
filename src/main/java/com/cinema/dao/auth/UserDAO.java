package com.cinema.dao.auth;

import com.cinema.model.entity.User;
import com.cinema.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO - Truy cập dữ liệu bảng User.
 * Cung cấp các thao tác CRUD cơ bản và tìm kiếm theo email.
 */
public class UserDAO {

    /**
     * Ánh xạ ResultSet → User entity.
     */
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserID(rs.getInt("userID"));
        user.setFullName(rs.getString("fullName"));
        user.setPasswordHash(rs.getString("passwordHash"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getInt("role"));
        user.setLoyaltyPoints(rs.getInt("loyaltyPoints"));
        user.setLocked(rs.getBoolean("isLocked"));
        return user;
    }

    /**
     * Tìm User theo địa chỉ email.
     *
     * @param email Email cần tìm
     * @return User hoặc null nếu không tồn tại
     */
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM User WHERE email = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Tìm User theo userID.
     *
     * @param userID ID cần tìm
     * @return User hoặc null
     */
    public User findById(int userID) throws SQLException {
        String sql = "SELECT * FROM User WHERE userID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Kiểm tra email đã tồn tại trong hệ thống chưa.
     *
     * @param email Email cần kiểm tra
     * @return true nếu email đã được sử dụng
     */
    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM User WHERE email = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Thêm User mới vào DB.
     *
     * @param user Đối tượng User cần lưu
     * @return true nếu insert thành công
     */
    public boolean insert(User user) throws SQLException {
        String sql = "INSERT INTO User (fullName, passwordHash, email, phone, role, loyaltyPoints, isLocked) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhone());
            ps.setInt(5, user.getRole());
            ps.setInt(6, user.getLoyaltyPoints());
            ps.setBoolean(7, user.isLocked());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật thông tin hồ sơ (fullName, phone).
     *
     * @param userID   ID người dùng
     * @param fullName Tên mới
     * @param phone    Số điện thoại mới
     * @return true nếu cập nhật thành công
     */
    public boolean updateProfile(int userID, String fullName, String phone) throws SQLException {
        String sql = "UPDATE User SET fullName = ?, phone = ? WHERE userID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setInt(3, userID);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật mật khẩu (lưu hash mới).
     *
     * @param userID      ID người dùng
     * @param newHashPass Hash mật khẩu mới
     * @return true nếu thành công
     */
    public boolean updatePassword(int userID, String newHashPass) throws SQLException {
        String sql = "UPDATE User SET passwordHash = ? WHERE userID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHashPass);
            ps.setInt(2, userID);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật trạng thái khoá tài khoản.
     *
     * @param userID   ID người dùng
     * @param locked   true để khoá, false để mở khoá
     * @return true nếu thành công
     */
    public boolean setLockedStatus(int userID, boolean locked) throws SQLException {
        String sql = "UPDATE User SET isLocked = ? WHERE userID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, locked);
            ps.setInt(2, userID);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Lấy danh sách tất cả User (cho Admin quản lý).
     *
     * @return Danh sách User
     */
    public List<User> findAllUsers() throws SQLException {
        String sql = "SELECT * FROM User ORDER BY role DESC, userID ASC";
        List<User> users = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        }
        return users;
    }
}
