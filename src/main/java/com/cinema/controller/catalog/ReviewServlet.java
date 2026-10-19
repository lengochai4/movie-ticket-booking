package com.cinema.controller.catalog;

import com.cinema.dao.review.ReviewDAO;
import com.cinema.entity.Review;
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

@WebServlet("/review/add")
public class ReviewServlet extends HttpServlet {

    private final ReviewDAO reviewDAO = new ReviewDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        Object loginUser = session != null ? session.getAttribute("loginUser") : null;

        // Nếu chưa đăng nhập, chuyển hướng đến trang đăng nhập
        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String movieID = request.getParameter("movieID");
        String ratingStr = request.getParameter("rating");
        String comment = request.getParameter("comment");

        if (movieID == null || movieID.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/movies");
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

        if (comment == null || comment.trim().length() < 5) {
            response.sendRedirect(request.getContextPath() + "/movie-detail?id=" + movieID.trim());
            return;
        }

        String userId = LoginUserSupport.getUserId(loginUser);
        String userName = LoginUserSupport.getFullName(loginUser);
        if (userName == null || userName.isBlank()) {
            userName = "Khán giả Cinetik";
        }

        Review review = new Review();
        review.setId(UUID.randomUUID().toString());
        review.setMovieID(movieID.trim());
        review.setUserId(userId);
        review.setUserName(userName);
        review.setRating(rating);
        review.setComment(comment.trim());
        review.setHelpfulCount(0);
        review.setCreatedAt(LocalDateTime.now());

        reviewDAO.insert(review);

        // Quay lại trang chi tiết phim
        response.sendRedirect(request.getContextPath() + "/movie-detail?id=" + movieID.trim());
    }
}
