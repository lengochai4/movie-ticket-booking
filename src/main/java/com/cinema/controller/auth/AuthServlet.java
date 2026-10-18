package com.cinema.controller.auth;

import com.cinema.model.entity.User;
import com.cinema.service.auth.AuthService;
import com.cinema.service.auth.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AuthServlet - Xử lý các yêu cầu xác thực tại /auth/*
 * Các action:
 *   GET  /auth/login    → Hiển thị trang đăng nhập
 *   POST /auth/login    → Xử lý đăng nhập
 *   GET  /auth/register → Hiển thị trang đăng ký
 *   POST /auth/register → Xử lý đăng ký
 *   GET  /auth/logout   → Đăng xuất
 *   GET  /auth/profile  → Hiển thị trang hồ sơ
 *   POST /auth/profile  → Cập nhật hồ sơ
 *   POST /auth/change-password → Đổi mật khẩu
 */
@WebServlet("/auth/*")
public class AuthServlet extends HttpServlet {

    private AuthService authService;
    private UserService userService;

    @Override
    public void init() {
        authService = new AuthService();
        userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/login";

        switch (path) {
            case "/login":
                req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
                break;
            case "/register":
                req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
                break;
            case "/logout":
                handleLogout(req, resp);
                break;
            case "/profile":
                handleGetProfile(req, resp);
                break;
            default:
                resp.sendRedirect(req.getContextPath() + "/auth/login");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        if (path == null) path = "/login";

        switch (path) {
            case "/login":
                handleLogin(req, resp);
                break;
            case "/register":
                handleRegister(req, resp);
                break;
            case "/profile":
                handleUpdateProfile(req, resp);
                break;
            case "/change-password":
                handleChangePassword(req, resp);
                break;
            default:
                resp.sendRedirect(req.getContextPath() + "/auth/login");
        }
    }

    // ==================== HANDLERS ====================

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        User user = authService.login(email, password);
        if (user != null) {
            HttpSession session = req.getSession(true);
            session.setAttribute("loginUser", user);
            session.setMaxInactiveInterval(30 * 60); // 30 phút

            String redirect = req.getParameter("redirect");
            if (redirect != null && !redirect.trim().isEmpty() && !redirect.contains("/auth/login") && !redirect.contains("/auth/register")) {
                resp.sendRedirect(redirect);
            } else if (user.getRole() == 1) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
            } else {
                resp.sendRedirect(req.getContextPath() + "/");
            }
        } else {
            req.setAttribute("errorMessage", "Email hoặc mật khẩu không đúng, hoặc tài khoản đã bị khoá.");
            req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String password = req.getParameter("password");

        boolean success = authService.register(fullName, email, phone, password);
        if (success) {
            req.setAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
        } else {
            req.setAttribute("errorMessage", "Đăng ký thất bại. Email đã tồn tại hoặc thông tin không hợp lệ.");
            req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/auth/login");
    }

    private void handleGetProfile(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loginUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(req, resp);
    }

    private void handleUpdateProfile(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loginUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        User currentUser = (User) session.getAttribute("loginUser");
        String fullName = req.getParameter("fullName");
        String phone = req.getParameter("phone");

        User updatedUser = userService.updateProfile(currentUser.getUserID(), fullName, phone);
        if (updatedUser != null) {
            // Cập nhật lại session với thông tin mới
            session.setAttribute("loginUser", updatedUser);
            req.setAttribute("successMessage", "Cập nhật hồ sơ thành công!");
        } else {
            req.setAttribute("errorMessage", "Cập nhật thất bại. Vui lòng thử lại.");
        }
        req.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(req, resp);
    }

    private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loginUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        User currentUser = (User) session.getAttribute("loginUser");
        String oldPass = req.getParameter("oldPassword");
        String newPass = req.getParameter("newPassword");

        boolean success = userService.changePassword(currentUser.getUserID(), oldPass, newPass);
        if (success) {
            req.setAttribute("successMessage", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
            session.invalidate();
            resp.sendRedirect(req.getContextPath() + "/auth/login");
        } else {
            req.setAttribute("errorMessage", "Mật khẩu cũ không đúng hoặc mật khẩu mới không hợp lệ (tối thiểu 6 ký tự).");
            req.getRequestDispatcher("/WEB-INF/views/auth/profile.jsp").forward(req, resp);
        }
    }
}
