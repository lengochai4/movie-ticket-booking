package com.cinema.controller.auth;

import com.cinema.dao.auth.UserDAO;
import com.cinema.entity.User;
import com.cinema.service.order.LoyaltyService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

@WebServlet("/auth/verify")
public class VerifyEmailServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String token = request.getParameter("token");

        // ── Bước 1: Kiểm tra mã token hợp lệ ──
        if (token == null || token.isBlank()) {
            forwardResult(request, response, "invalid", "Liên kết không hợp lệ",
                    "Liên kết xác nhận không hợp lệ hoặc thiếu mã xác thực kích hoạt.");
            return;
        }

        // ── Bước 2: Tìm người dùng sở hữu mã token ──
        User user = userDAO.findByVerificationToken(token.trim());
        if (user == null) {
            forwardResult(request, response, "invalid", "Mã xác thực không tồn tại",
                    "Mã xác thực không tồn tại, đã hết hạn hoặc tài khoản này đã được kích hoạt trước đó.");
            return;
        }

        // ── Bước 3: Kiểm tra thời hạn hiệu lực của mã token (24 giờ) ──
        if (user.getTokenExpiry() != null && LocalDateTime.now().isAfter(user.getTokenExpiry())) {
            request.setAttribute("email", user.getEmail());
            forwardResult(request, response, "expired", "Liên kết đã hết hạn",
                    "Liên kết xác thực tài khoản này đã quá hạn (chỉ có hiệu lực trong 24 giờ). Vui lòng yêu cầu gửi lại email mới.");
            return;
        }

        // ── Bước 4: Kích hoạt tài khoản và cập nhật cơ sở dữ liệu ──
        user.setStatus("ACTIVE");
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiry(null);

        boolean updated = userDAO.update(user);
        if (!updated) {
            forwardResult(request, response, "error", "Lỗi kích hoạt",
                    "Có lỗi xảy ra khi lưu trạng thái kích hoạt vào hệ thống. Vui lòng thử lại sau.");
            return;
        }

        // Đồng bộ ví điểm thưởng thành viên
        try {
            LoyaltyService loyaltyService = new LoyaltyService();
            loyaltyService.getPoints(user.getId());
        } catch (Exception ignored) {
        }

        // ── Bước 5: Kích hoạt thành công ──
        request.setAttribute("fullName", user.getFullName());
        request.setAttribute("email", user.getEmail());
        forwardResult(request, response, "success", "Kích hoạt thành công!",
                "Chúc mừng bạn đã kích hoạt tài khoản thành công! Bây giờ bạn có thể đăng nhập để đặt vé xem phim và tích lũy ưu đãi.");
    }

    private void forwardResult(HttpServletRequest request, HttpServletResponse response,
                               String status, String title, String message)
            throws ServletException, IOException {
        request.setAttribute("status", status);
        request.setAttribute("title", title);
        request.setAttribute("message", message);
        request.getRequestDispatcher("/WEB-INF/views/auth/verify-result.jsp").forward(request, response);
    }
}

