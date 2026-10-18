package com.cinema.dao.review;

import com.cinema.model.entity.Review;
import com.cinema.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ReviewDAO - Truy xuất cơ sở dữ liệu cho bảng Review.
 * Thuộc sở hữu của Ân (@an).
 */
public class ReviewDAO {

    /**
     * Map ResultSet thành entity Review.
     */
    private Review mapRow(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setReviewID(rs.getString("reviewID"));
        review.setUserID(rs.getInt("userID"));
        review.setMovieID(rs.getString("movieID"));
        review.setRating(rs.getFloat("rating"));
        review.setComment(rs.getString("comment"));

        Timestamp ts = rs.getTimestamp("createdAt");
        if (ts != null) {
            review.setCreatedAt(ts.toLocalDateTime());
        } else {
            review.setCreatedAt(LocalDateTime.now());
        }
        return review;
    }

    /**
     * Thêm một đánh giá mới.
     *
     * @param review Đối tượng Review
     * @return true nếu thêm thành công
     */
    public boolean insert(Review review) throws SQLException {
        String sql = "INSERT INTO Review (reviewID, userID, movieID, rating, comment, createdAt) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, review.getReviewID());
            ps.setInt(2, review.getUserID());
            ps.setString(3, review.getMovieID());
            ps.setFloat(4, review.getRating());
            ps.setString(5, review.getComment());
            ps.setTimestamp(6, Timestamp.valueOf(review.getCreatedAt() != null ? review.getCreatedAt() : LocalDateTime.now()));
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xoá một đánh giá theo ID.
     *
     * @param reviewID Mã đánh giá cần xoá
     * @return true nếu xoá thành công
     */
    public boolean delete(String reviewID) throws SQLException {
        String sql = "DELETE FROM Review WHERE reviewID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reviewID);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Tìm review theo reviewID.
     *
     * @param reviewID Mã đánh giá
     * @return Review hoặc null
     */
    public Review findById(String reviewID) throws SQLException {
        String sql = "SELECT * FROM Review WHERE reviewID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reviewID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Lấy danh sách đánh giá theo mã phim (mới nhất lên đầu).
     *
     * @param movieID Mã phim
     * @return Danh sách Review
     */
    public List<Review> findByMovieId(String movieID) throws SQLException {
        String sql = "SELECT * FROM Review WHERE movieID = ? ORDER BY createdAt DESC";
        List<Review> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, movieID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Tính điểm đánh giá trung bình của phim.
     *
     * @param movieID Mã phim
     * @return Điểm trung bình (0.0 nếu chưa có ai đánh giá)
     */
    public double getAverageRating(String movieID) throws SQLException {
        String sql = "SELECT AVG(rating) FROM Review WHERE movieID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, movieID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    /**
     * Đếm số lượt đánh giá của phim.
     *
     * @param movieID Mã phim
     * @return Tổng số review
     */
    public int countByMovieId(String movieID) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Review WHERE movieID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, movieID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Lấy tất cả đánh giá kèm tên user và tiêu đề phim (hỗ trợ admin kiểm duyệt).
     */
    public List<ReviewDetail> findAllWithDetails() throws SQLException {
        String sql = "SELECT r.*, u.fullName AS userName, u.email AS userEmail, m.title AS movieTitle " +
                     "FROM Review r " +
                     "JOIN User u ON r.userID = u.userID " +
                     "JOIN Movie m ON r.movieID = m.movieID " +
                     "ORDER BY r.createdAt DESC";
        List<ReviewDetail> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Review r = mapRow(rs);
                ReviewDetail detail = new ReviewDetail(r, rs.getString("userName"), rs.getString("userEmail"), rs.getString("movieTitle"));
                list.add(detail);
            }
        }
        return list;
    }

    /**
     * Lấy đánh giá của một phim kèm tên user.
     */
    public List<ReviewDetail> findDetailsByMovieId(String movieID) throws SQLException {
        String sql = "SELECT r.*, u.fullName AS userName, u.email AS userEmail, m.title AS movieTitle " +
                     "FROM Review r " +
                     "JOIN User u ON r.userID = u.userID " +
                     "JOIN Movie m ON r.movieID = m.movieID " +
                     "WHERE r.movieID = ? " +
                     "ORDER BY r.createdAt DESC";
        List<ReviewDetail> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, movieID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = mapRow(rs);
                    ReviewDetail detail = new ReviewDetail(r, rs.getString("userName"), rs.getString("userEmail"), rs.getString("movieTitle"));
                    list.add(detail);
                }
            }
        }
        return list;
    }

    /**
     * DTO hỗ trợ hiển thị đánh giá kèm thông tin user và movie.
     */
    public static class ReviewDetail {
        private final Review review;
        private final String userName;
        private final String userEmail;
        private final String movieTitle;

        public ReviewDetail(Review review, String userName, String userEmail, String movieTitle) {
            this.review = review;
            this.userName = userName;
            this.userEmail = userEmail;
            this.movieTitle = movieTitle;
        }

        public Review getReview() {
            return review;
        }

        public String getUserName() {
            return userName;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public String getMovieTitle() {
            return movieTitle;
        }
    }
}
