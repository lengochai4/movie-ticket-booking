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

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String token = request.getParameter("token");

        // 1. Kiểm tra token rỗng
        if (token == null || token.isBlank()) {
            request.setAttribute("status", "invalid");
            request.setAttribute("title", "Liên kết không hợp lệ");
            request.setAttribute("message", "Liên kết xác nhận không hợp lệ hoặc thiếu mã xác thực.");
            request.getRequestDispatcher("/WEB-INF/views/auth/verify-result.jsp").forward(request, response);
            return;
        }

        // 2. Tìm người dùng sở hữu token
        User user = userDAO.findByVerificationToken(token.trim());
        if (user == null) {
            request.setAttribute("status", "invalid");
            request.setAttribute("title", "Mã xác thực không tồn tại");
            request.setAttribute("message", "Mã xác thực không tồn tại, đã hết hạn hoặc tài khoản này đã được kích hoạt trước đó.");
            request.getRequestDispatcher("/WEB-INF/views/auth/verify-result.jsp").forward(request, response);
            return;
        }

        // 3. Kiểm tra hạn sử dụng (24 giờ)
        if (user.getTokenExpiry() != null && LocalDateTime.now().isAfter(user.getTokenExpiry())) {
            request.setAttribute("status", "expired");
            request.setAttribute("title", "Liên kết đã hết hạn");
            request.setAttribute("email", user.getEmail());
            request.setAttribute("message", "Liên kết xác thực tài khoản này đã quá hạn (chỉ có hiệu lực trong 24 giờ). Vui lòng yêu cầu gửi lại email mới.");
            request.getRequestDispatcher("/WEB-INF/views/auth/verify-result.jsp").forward(request, response);
            return;
        }

        // 4. Kích hoạt tài khoản và lưu vào Database
        user.setStatus("ACTIVE");
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiry(null);

        boolean updated = userDAO.update(user);
        if (!updated) {
            request.setAttribute("status", "error");
            request.setAttribute("title", "Lỗi kích hoạt");
            request.setAttribute("message", "Có lỗi xảy ra khi lưu trạng thái tài khoản vào cơ sở dữ liệu. Vui lòng thử lại sau.");
            request.getRequestDispatcher("/WEB-INF/views/auth/verify-result.jsp").forward(request, response);
            return;
        }

        // Đồng bộ điểm thưởng
        try {
            LoyaltyService loyaltyService = new LoyaltyService();
            loyaltyService.getPoints(user.getId());
        } catch (Exception ignored) {
        }

        // 5. Hiển thị thông báo kích hoạt thành công
        request.setAttribute("status", "success");
        request.setAttribute("title", "Kích hoạt thành công!");
        request.setAttribute("fullName", user.getFullName());
        request.setAttribute("email", user.getEmail());
        request.setAttribute("message", "Chúc mừng bạn đã kích hoạt tài khoản thành công! Bây giờ bạn đã có thể đăng nhập để đặt vé xem phim và nhận ưu đãi.");
        request.getRequestDispatcher("/WEB-INF/views/auth/verify-result.jsp").forward(request, response);
    }
}
