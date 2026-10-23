package com.cinema.controller.catalog;

import com.cinema.dao.review.ReviewDAO;
import com.cinema.entity.Review;
import com.cinema.entity.User;
import com.cinema.order.LoginUserSupport;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Controller xử lý Gửi đánh giá & bình luận phim (Module Review - Ân).
 * - GET: Điều hướng về danh sách phim.
 * - POST: Tiếp nhận số sao và nội dung bình luận từ người dùng đã đăng nhập.
 */
@WebServlet("/review/add")
public class ReviewServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final ReviewDAO reviewDAO = new ReviewDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/movies");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // ── Bước 1: Kiểm tra quyền đăng nhập ──
        HttpSession session = request.getSession(false);
        Object loginUser = session != null ? session.getAttribute("loginUser") : null;
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        // ── Bước 2: Tiếp nhận dữ liệu đánh giá ──
        String movieID = request.getParameter("movieID");
        String ratingStr = request.getParameter("rating");
        String comment = request.getParameter("comment");

        if (movieID == null || movieID.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        String cleanMovieId = movieID.trim();

        // ── Bước 3: Xác thực nội dung đánh giá ──
        if (comment == null || comment.trim().length() < 5) {
            response.sendRedirect(request.getContextPath() + "/movie-detail?id=" + cleanMovieId);
            return;
        }

        int rating = 5;
        try {
            if (ratingStr != null && !ratingStr.isBlank()) {
                rating = Integer.parseInt(ratingStr.trim());
                if (rating < 1) rating = 1;
                if (rating > 5) rating = 5;
            }
        } catch (NumberFormatException ignored) {
            rating = 5;
        }

        // ── Bước 4: Lấy thông tin người đánh giá từ Session ──
        String userId;
        String userName;
        if (loginUser instanceof User u) {
            userId = u.getId();
            userName = u.getFullName();
        } else {
            userId = LoginUserSupport.getUserId(loginUser);
            userName = LoginUserSupport.getFullName(loginUser);
        }

        if (userName == null || userName.isBlank()) {
            userName = "Khán giả Cinetik";
        }

        // ── Bước 5: Tạo đối tượng Review và lưu vào CSDL ──
        Review review = new Review();
        review.setId(UUID.randomUUID().toString());
        review.setMovieID(cleanMovieId);
        review.setUserId(userId);
        review.setUserName(userName);
        review.setRating(rating);
        review.setComment(comment.trim());
        review.setHelpfulCount(0);
        review.setCreatedAt(LocalDateTime.now());

        reviewDAO.insert(review);

        // ── Bước 6: Quay lại trang chi tiết phim ──
        response.sendRedirect(request.getContextPath() + "/movie-detail?id=" + cleanMovieId);
    }
}

