package com.cinema.filter;

import com.cinema.dao.auth.UserDAO;
import com.cinema.entity.User;
import com.cinema.order.LoginUserSupport;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * AuthFilter - Kiểm tra người dùng đã đăng nhập chưa đối với các trang yêu cầu xác thực.
 * Các URL bảo vệ:
 *   - /auth/profile, /auth/profile/*
 *   - /booking/*
 *   - /order/*, /orders/*
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {
        "/auth/profile",
        "/auth/profile/*",
        "/booking/*",
        "/order/*",
        "/orders/*"
})
public class AuthFilter implements Filter {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        Object loginUserObj = (session != null) ? session.getAttribute("loginUser") : null;

        // 1. Chưa đăng nhập -> chuyển hướng về trang đăng nhập kèm redirect
        if (loginUserObj == null) {
            String requestURI = httpRequest.getRequestURI();
            String queryString = httpRequest.getQueryString();
            String redirectUrl = (queryString != null) ? (requestURI + "?" + queryString) : requestURI;

            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth/login?redirect=" +
                    URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8));
            return;
        }

        // 2. Đọc lại trạng thái mới nhất từ database (xử lý A4: tài khoản bị khóa bị hủy phiên ngay)
        String userId = LoginUserSupport.getUserId(loginUserObj);
        User freshUser = (userId != null) ? userDAO.findById(userId) : null;

        if (freshUser == null || freshUser.isLocked() || "LOCKED".equalsIgnoreCase(freshUser.getStatus())) {
            if (session != null) {
                session.invalidate();
            }
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth/login?error=account_locked");
            return;
        }

        // Cập nhật lại đối tượng user mới nhất vào session
        session.setAttribute("loginUser", freshUser);

        // 3. Đã đăng nhập và tài khoản hoạt động -> cho qua filter chain
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
