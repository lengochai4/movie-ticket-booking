package com.cinema.controller.auth;

import com.cinema.model.entity.User;
import com.cinema.service.auth.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * AdminUserServlet - Quản trị viên quản lý danh sách người dùng, khoá/mở tài khoản.
 * URL: /admin/users, /admin/users/*
 * Thuộc sở hữu của Ân (@an).
 */
@WebServlet(urlPatterns = {"/admin/users", "/admin/users/*"})
public class AdminUserServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() {
        userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String action = req.getParameter("action");

        if ("lock".equals(action) || (pathInfo != null && pathInfo.equals("/lock"))) {
            handleLock(req, resp);
            return;
        }

        if ("unlock".equals(action) || (pathInfo != null && pathInfo.equals("/unlock"))) {
            handleUnlock(req, resp);
            return;
        }

        // Mặc định: Hiển thị danh sách user
        List<User> userList = userService.getAllUsers();
        req.setAttribute("users", userList);
        req.getRequestDispatcher("/WEB-INF/views/auth/admin/users.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String pathInfo = req.getPathInfo();
        String action = req.getParameter("action");

        if ("lock".equals(action) || (pathInfo != null && pathInfo.equals("/lock"))) {
            handleLock(req, resp);
        } else if ("unlock".equals(action) || (pathInfo != null && pathInfo.equals("/unlock"))) {
            handleUnlock(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/users");
        }
    }

    private void handleLock(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        User loginUser = (session != null) ? (User) session.getAttribute("loginUser") : null;

        String idStr = req.getParameter("id");
        if (idStr != null) {
            try {
                int targetId = Integer.parseInt(idStr);
                // Không cho phép admin tự khoá chính mình
                if (loginUser != null && loginUser.getUserID() == targetId) {
                    session.setAttribute("adminError", "Bạn không thể tự khoá tài khoản của chính mình!");
                } else {
                    boolean success = userService.lock(targetId);
                    if (success) {
                        session.setAttribute("adminSuccess", "Đã khoá tài khoản thành công!");
                    } else {
                        session.setAttribute("adminError", "Không thể khoá tài khoản.");
                    }
                }
            } catch (NumberFormatException e) {
                session.setAttribute("adminError", "ID người dùng không hợp lệ.");
            }
        }
        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }

    private void handleUnlock(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        String idStr = req.getParameter("id");
        if (idStr != null) {
            try {
                int targetId = Integer.parseInt(idStr);
                boolean success = userService.unlock(targetId);
                if (success) {
                    session.setAttribute("adminSuccess", "Đã mở khoá tài khoản thành công!");
                } else {
                    session.setAttribute("adminError", "Không thể mở khoá tài khoản.");
                }
            } catch (NumberFormatException e) {
                session.setAttribute("adminError", "ID người dùng không hợp lệ.");
            }
        }
        resp.sendRedirect(req.getContextPath() + "/admin/users");
    }
}
