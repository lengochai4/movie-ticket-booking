package com.cinema.service;

import com.cinema.service.auth.AuthService;
import com.cinema.service.auth.UserService;
import com.cinema.service.review.ReviewService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationLogicTest {

    @Test
    public void testAuthServiceRegistrationValidation() {
        AuthService authService = new AuthService();

        // Tên rỗng -> từ chối
        assertFalse(authService.register("", "test@cinema.com", "0901234567", "123456"));
        // Email không hợp lệ -> từ chối
        assertFalse(authService.register("User Test", "invalid-email", "0901234567", "123456"));
        // Password ngắn hơn 6 ký tự -> từ chối
        assertFalse(authService.register("User Test", "test@cinema.com", "0901234567", "123"));
    }

    @Test
    public void testReviewServiceValidation() {
        ReviewService reviewService = new ReviewService();

        // Rating ngoài khoảng 1.0 - 5.0 -> từ chối
        assertFalse(reviewService.addReview(1, "MOV_01", 0.5f, "Phim tệ"));
        assertFalse(reviewService.addReview(1, "MOV_01", 5.5f, "Phim đỉnh"));
        // Comment rỗng -> từ chối
        assertFalse(reviewService.addReview(1, "MOV_01", 4.0f, ""));
        assertFalse(reviewService.addReview(1, "", 4.0f, "Hay"));
    }

    @Test
    public void testUserServiceValidation() {
        UserService userService = new UserService();

        // Update profile tên rỗng -> từ chối
        assertNull(userService.updateProfile(1, "", "0901234567"));
        // Đổi password mới ngắn hơn 6 ký tự -> từ chối
        assertFalse(userService.changePassword(1, "oldpass", "123"));
    }
}
