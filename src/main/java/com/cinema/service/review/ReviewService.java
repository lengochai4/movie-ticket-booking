package com.cinema.service.review;

import com.cinema.dao.review.ReviewDAO;
import com.cinema.model.entity.Review;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ReviewService - Service xử lý nghiệp vụ Đánh giá phim.
 * <p>
 * NOTE: Signature delete(String reviewID) được chốt từ Ngày 1.
 */
public class ReviewService {

    private final ReviewDAO reviewDAO;

    public ReviewService() {
        this.reviewDAO = new ReviewDAO();
    }

    // ==================== NGÀY 1: SIGNATURE CHỐT ====================

    /**
     * Xoá đánh giá vi phạm (Admin gọi).
     * Kiểm tra reviewID hợp lệ trước khi xoá.
     *
     * @param reviewID ID đánh giá cần xoá
     * @return true nếu xoá thành công
     */
    public boolean delete(String reviewID) {
        if (reviewID == null || reviewID.trim().isEmpty()) {
            return false;
        }
        try {
            return reviewDAO.delete(reviewID.trim());
        } catch (SQLException e) {
            System.err.println("[ReviewService] Lỗi xoá reviewID=" + reviewID + ": " + e.getMessage());
            return false;
        }
    }

    // ==================== NGÀY 2: FULL IMPLEMENTATION ====================

    /**
     * Lấy danh sách đánh giá theo phim, kèm tên người đánh giá.
     *
     * @param movieID ID phim
     * @return Danh sách Review
     */
    public List<Review> getReviewsByMovie(String movieID) {
        if (movieID == null || movieID.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return reviewDAO.findByMovieId(movieID.trim());
        } catch (SQLException e) {
            System.err.println("[ReviewService] Lỗi lấy review movieID=" + movieID + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Thêm đánh giá mới cho phim.
     *
     * @param userID  ID người dùng
     * @param movieID ID phim
     * @param rating  Điểm đánh giá (1.0 - 5.0)
     * @param comment Nội dung bình luận
     * @return true nếu lưu thành công, false nếu validate thất bại hoặc lỗi
     */
    public boolean addReview(int userID, String movieID, float rating, String comment) {
        if (movieID == null || movieID.trim().isEmpty()) return false;
        if (rating < 1.0f || rating > 5.0f) return false;
        if (comment == null || comment.trim().isEmpty()) return false;

        Review review = new Review();
        review.setReviewID("REV_" + System.currentTimeMillis());
        review.setUserID(userID);
        review.setMovieID(movieID.trim());
        review.setRating(rating);
        review.setComment(comment.trim());

        try {
            return reviewDAO.insert(review);
        } catch (SQLException e) {
            System.err.println("[ReviewService] Lỗi thêm review: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy danh sách đánh giá kèm tên người dùng và tiêu đề phim.
     */
    public List<ReviewDAO.ReviewDetail> getReviewDetailsByMovie(String movieID) {
        if (movieID == null || movieID.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return reviewDAO.findDetailsByMovieId(movieID.trim());
        } catch (SQLException e) {
            System.err.println("[ReviewService] Lỗi lấy review details movieID=" + movieID + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Lấy điểm đánh giá trung bình.
     */
    public double getAverageRating(String movieID) {
        if (movieID == null || movieID.trim().isEmpty()) return 0.0;
        try {
            return reviewDAO.getAverageRating(movieID.trim());
        } catch (SQLException e) {
            System.err.println("[ReviewService] Lỗi tính average rating movieID=" + movieID + ": " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Lấy tổng số đánh giá của một phim.
     */
    public int getReviewCount(String movieID) {
        if (movieID == null || movieID.trim().isEmpty()) return 0;
        try {
            return reviewDAO.countByMovieId(movieID.trim());
        } catch (SQLException e) {
            System.err.println("[ReviewService] Lỗi đếm review: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Lấy tất cả review kèm thông tin chi tiết phục vụ admin quản lý / kiểm duyệt.
     */
    public List<ReviewDAO.ReviewDetail> getAllReviewDetails() {
        try {
            return reviewDAO.findAllWithDetails();
        } catch (SQLException e) {
            System.err.println("[ReviewService] Lỗi lấy toàn bộ review: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Tìm review theo ID.
     */
    public Review findById(String reviewID) {
        if (reviewID == null || reviewID.trim().isEmpty()) return null;
        try {
            return reviewDAO.findById(reviewID.trim());
        } catch (SQLException e) {
            System.err.println("[ReviewService] Lỗi tìm review theo ID: " + e.getMessage());
            return null;
        }
    }
}
