package com.cinema.controller.auth;

import com.cinema.dao.auth.UserDAO;
import com.cinema.entity.User;
import com.cinema.order.LoginUserSupport;
import com.cinema.service.order.LoyaltyService;
import com.cinema.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/auth/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loginUser") != null) {
            Object loginUserObj = session.getAttribute("loginUser");
            String userId = LoginUserSupport.getUserId(loginUserObj);
            User fresh = (userId != null) ? userDAO.findById(userId) : null;
            if (fresh != null && !fresh.isLocked() && !"LOCKED".equalsIgnoreCase(fresh.getStatus())) {
                response.sendRedirect(request.getContextPath() + "/movies");
                return;
            } else {
                session.invalidate();
            }
        }

        request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String redirect = request.getParameter("redirect");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            request.setAttribute("errorMessage", "Vui lòng nhập đầy đủ email và mật khẩu.");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }

        User user = userDAO.findByEmailOrPhone(username.trim());
        if (user == null || !PasswordUtil.verify(password, user.getPassword())) {
            request.setAttribute("errorMessage", "Email hoặc mật khẩu không chính xác.");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }

        if (user.isLocked() || "LOCKED".equalsIgnoreCase(user.getStatus())) {
            request.setAttribute("errorMessage", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ ban quản trị.");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }

        if (user.isPending() || "PENDING".equalsIgnoreCase(user.getStatus()) || Boolean.FALSE.equals(user.getEmailVerified())) {
            request.setAttribute("errorMessage", "Tài khoản chưa được xác thực email. Vui lòng kiểm tra hộp thư hoặc bấm gửi lại liên kết xác thực.");
            request.setAttribute("unverifiedEmail", user.getEmail());
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }

        // Đồng bộ điểm thưởng từ module của Như sang session
        try {
            LoyaltyService loyaltyService = new LoyaltyService();
            int points = loyaltyService.getPoints(user.getId());
            user.setLoyaltyPoints(points);
        } catch (Exception ignored) {
        }

        // A5: Đổi session ID khi đăng nhập để chống Session Fixation
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute("loginUser", user);

        // Chuyển hướng sau khi đăng nhập thành công
        if (redirect != null && !redirect.isBlank()) {
            String contextPath = request.getContextPath();
            if (redirect.startsWith(contextPath + "/") || redirect.equals(contextPath)) {
                response.sendRedirect(redirect);
                return;
            } else if (redirect.startsWith("/") && !redirect.startsWith("//")) {
                response.sendRedirect(contextPath + redirect);
                return;
            }
        }

        response.sendRedirect(request.getContextPath() + "/movies");
    }
}