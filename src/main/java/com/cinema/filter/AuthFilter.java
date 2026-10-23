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
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        Object loginUserObj = (session != null) ? session.getAttribute("loginUser") : null;

        // ── Bước 1: Chưa đăng nhập -> Chuyển hướng về trang đăng nhập kèm redirect URL ──
        if (loginUserObj == null) {
            String targetUrl = getTargetUrl(httpRequest);
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth/login?redirect=" +
                    URLEncoder.encode(targetUrl, StandardCharsets.UTF_8));
            return;
        }

        // ── Bước 2: Kiểm tra trạng thái tài khoản mới nhất trong Database ──
        String userId = (loginUserObj instanceof User u) ? u.getId() : LoginUserSupport.getUserId(loginUserObj);
        User freshUser = (userId != null) ? userDAO.findById(userId) : null;

        if (freshUser == null || freshUser.isLocked()) {
            if (session != null) {
                session.invalidate();
            }
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth/login?error=account_locked");
            return;
        }

        // ── Bước 3: Đồng bộ trạng thái mới nhất vào Session và cho phép đi tiếp ──
        session.setAttribute("loginUser", freshUser);
        chain.doFilter(request, response);
    }

    /**
     * Lấy URL đầy đủ mà người dùng đang cố gắng truy cập
     */
    private String getTargetUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        return (query != null && !query.isBlank()) ? (uri + "?" + query) : uri;
    }
}

