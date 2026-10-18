package com.cinema.controller.review;

import com.cinema.dao.review.ReviewDAO;
import com.cinema.model.entity.User;
import com.cinema.service.review.ReviewService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * ReviewServlet - Xử lý đánh giá phim tại /reviews/* và /reviews
 * Thuộc sở hữu của Ân (@an).
 * 
 * Các hành động:
 *   GET  /reviews?movieId=...        → Lấy danh sách đánh giá của phim (forward review-section.jsp)
 *   POST /reviews                    → Người dùng gửi đánh giá mới (yêu cầu đăng nhập)
 *   POST /reviews/delete             → Admin xoá đánh giá vi phạm
 *   GET  /reviews/delete?id=...      → Hỗ trợ link xoá nhanh cho Admin
 */
@WebServlet(urlPatterns = {"/reviews", "/reviews/*"})
public class ReviewServlet extends HttpServlet {

    private ReviewService reviewService;

    @Override
    public void init() {
        reviewService = new ReviewService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equals("/delete")) {
            handleDeleteReview(req, resp);
            return;
        }

        // Mặc định: Hiển thị section đánh giá cho phim
        String movieId = req.getParameter("movieId");
        if (movieId == null || movieId.trim().isEmpty()) {
            movieId = req.getParameter("movieID");
        }

        if (movieId != null && !movieId.trim().isEmpty()) {
            List<ReviewDAO.ReviewDetail> reviewList = reviewService.getReviewDetailsByMovie(movieId.trim());
            double avgRating = reviewService.getAverageRating(movieId.trim());
            int reviewCount = reviewService.getReviewCount(movieId.trim());

            req.setAttribute("reviews", reviewList);
            req.setAttribute("averageRating", avgRating);
            req.setAttribute("reviewCount", reviewCount);
            req.setAttribute("currentMovieId", movieId.trim());
        }

        req.getRequestDispatcher("/WEB-INF/views/review/review-section.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equals("/delete")) {
            handleDeleteReview(req, resp);
        } else {
            handleAddReview(req, resp);
        }
    }

    /**
     * Xử lý thêm đánh giá mới từ người dùng.
     */
    private void handleAddReview(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        User loginUser = (session != null) ? (User) session.getAttribute("loginUser") : null;

        String movieId = req.getParameter("movieId");
        if (movieId == null || movieId.trim().isEmpty()) {
            movieId = req.getParameter("movieID");
        }

        if (loginUser == null) {
            // Chưa đăng nhập -> chuyển hướng về trang login
            resp.sendRedirect(req.getContextPath() + "/auth/login?redirect=" + req.getContextPath() + "/movies/detail?id=" + (movieId != null ? movieId : ""));
            return;
        }

        String ratingStr = req.getParameter("rating");
        String comment = req.getParameter("comment");

        if (movieId != null && ratingStr != null && comment != null) {
            try {
                float rating = Float.parseFloat(ratingStr);
                reviewService.addReview(loginUser.getUserID(), movieId.trim(), rating, comment.trim());
            } catch (NumberFormatException e) {
                System.err.println("[ReviewServlet] Điểm rating không hợp lệ: " + ratingStr);
            }
        }

        // Quay lại trang chi tiết phim theo Issue 1.3
        if (movieId != null && !movieId.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/movies/detail?id=" + movieId.trim());
            return;
        }

        String referer = req.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            resp.sendRedirect(referer);
        } else {
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }

    /**
     * Xử lý xoá đánh giá vi phạm (chỉ Admin).
     */
    private void handleDeleteReview(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        User loginUser = (session != null) ? (User) session.getAttribute("loginUser") : null;

        // Kiểm tra quyền Admin
        if (loginUser == null || loginUser.getRole() != 1) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xoá đánh giá.");
            return;
        }

        String reviewId = req.getParameter("id");
        if (reviewId == null || reviewId.trim().isEmpty()) {
            reviewId = req.getParameter("reviewID");
        }

        if (reviewId != null && !reviewId.trim().isEmpty()) {
            reviewService.delete(reviewId.trim());
        }

        // Redirect về trang chi tiết phim theo Issue 1.3
        String movieId = req.getParameter("movieId");
        if (movieId == null || movieId.trim().isEmpty()) {
            movieId = req.getParameter("movieID");
        }
        if (movieId != null && !movieId.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/movies/detail?id=" + movieId.trim());
            return;
        }

        String referer = req.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            resp.sendRedirect(referer);
        } else {
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }
}
