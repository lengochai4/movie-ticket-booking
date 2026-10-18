package com.cinema.filter;

import com.cinema.model.entity.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * AdminFilter - Phân quyền quản trị viên cho toàn bộ route /admin/*
 * Áp dụng cho tất cả servlet admin của nhóm:
 *   - /admin/users (Ân)
 *   - /admin/cinemas, /admin/rooms, /admin/seats (Phương)
 *   - /admin/bookings (Như)
 *   - /admin/* (Vinh: phim, suất chiếu, combo, thống kê)
 * 
 * Thuộc sở hữu của Ân (@an).
 */
@WebFilter(filterName = "AdminFilter", urlPatterns = {"/admin/*"})
public class AdminFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Khởi tạo AdminFilter
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        User loginUser = (session != null) ? (User) session.getAttribute("loginUser") : null;

        // 1. Chưa đăng nhập -> chuyển hướng về trang đăng nhập
        if (loginUser == null) {
            String requestURI = httpRequest.getRequestURI();
            String queryString = httpRequest.getQueryString();
            String redirectUrl = (queryString != null) ? (requestURI + "?" + queryString) : requestURI;

            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth/login?redirect=" + redirectUrl);
            return;
        }

        // 2. Tài khoản bị khoá -> huỷ phiên và chuyển hướng
        if (loginUser.isLocked()) {
            if (session != null) {
                session.invalidate();
            }
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth/login?error=account_locked");
            return;
        }

        // 3. Đã đăng nhập nhưng không phải Admin (role != 1) -> 403 Forbidden
        if (loginUser.getRole() != 1) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, 
                    "Bạn không có quyền truy cập khu vực Quản trị viên (Admin).");
            return;
        }

        // 4. Đúng quyền Admin và tài khoản hoạt động -> cho qua
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Hủy filter
    }
}
