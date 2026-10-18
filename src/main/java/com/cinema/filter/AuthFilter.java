package com.cinema.filter;

import com.cinema.model.entity.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AuthFilter - Kiểm tra người dùng đã đăng nhập chưa đối với các trang yêu cầu xác thực.
 * Thuộc sở hữu của Ân (@an).
 * 
 * Các URL bảo vệ:
 *   - /auth/profile
 *   - /auth/change-password
 *   - /booking/*
 *   - /orders/*
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {
        "/auth/profile",
        "/auth/change-password",
        "/booking/*",
        "/orders/*"
})
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Khởi tạo filter
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        User loginUser = (session != null) ? (User) session.getAttribute("loginUser") : null;

        if (loginUser == null) {
            // Lưu lại đường dẫn mong muốn chuyển tiếp sau khi đăng nhập
            String requestURI = httpRequest.getRequestURI();
            String queryString = httpRequest.getQueryString();
            String redirectUrl = (queryString != null) ? (requestURI + "?" + queryString) : requestURI;

            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth/login?redirect=" + redirectUrl);
            return;
        }

        // Nếu tài khoản bị khoá trong khi có phiên đăng nhập: huỷ phiên hiện tại
        if (loginUser.isLocked()) {
            if (session != null) {
                session.invalidate();
            }
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth/login?error=account_locked");
            return;
        }

        // Đã đăng nhập và hợp lệ, cho phép tiếp tục
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Hủy filter
    }
}
