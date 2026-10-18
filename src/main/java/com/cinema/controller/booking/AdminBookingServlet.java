package com.cinema.controller.booking;

import com.cinema.dto.BookingStats;
import com.cinema.dto.OrderActionResult;
import com.cinema.dto.OrderView;
import com.cinema.order.LoginUserSupport;
import com.cinema.service.order.OrderService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/admin/bookings", "/admin/bookings/cancel", "/admin/bookings/status"})
public class AdminBookingServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        prepare(request, response);
        if (!canManage(request, response)) {
            return;
        }
        showList(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        prepare(request, response);
        HttpSession session = request.getSession();
        Object loginUser = LoginUserSupport.getLoginUser(session);
        if (loginUser == null || !LoginUserSupport.isAdminOrStaff(loginUser)) {
            flash(session, "errorMessage", "Bạn không có quyền quản lý đơn vé.");
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String path = request.getServletPath();
        OrderActionResult result;
        if ("/admin/bookings/cancel".equals(path)) {
            result = orderService.cancelForAdmin(
                    request.getParameter("orderCode"),
                    request.getParameter("cancelReason"));
        } else if ("/admin/bookings/status".equals(path)) {
            result = orderService.updateStatus(
                    request.getParameter("orderCode"),
                    request.getParameter("status"),
                    request.getParameter("cancelReason"));
        } else {
            doGet(request, response);
            return;
        }
        applyResult(session, loginUser, result);
        response.sendRedirect(request.getContextPath() + "/admin/bookings");
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");
        if (status != null && !status.isBlank() && orderService.parseStatus(status) == null) {
            request.setAttribute("errorMessage", "Trạng thái lọc không hợp lệ.");
            status = null;
        }
        List<OrderView> orderList = new ArrayList<>();
        BookingStats stats = new BookingStats();
        try {
            orderList = orderService.searchForAdmin(keyword, status);
            stats = orderService.stats();
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Không tải được danh sách đơn vé. Vui lòng thử lại sau.");
        }
        request.setAttribute("orderList", orderList);
        request.setAttribute("ordersBound", Boolean.TRUE);
        request.setAttribute("totalOrders", stats.getTotalOrders());
        request.setAttribute("paidOrders", stats.getPaidOrders());
        request.setAttribute("cancelledOrders", stats.getCancelledOrders());
        request.setAttribute("totalRevenue", stats.getTotalRevenue());
        request.getRequestDispatcher("/WEB-INF/views/order/admin/bookings.jsp").forward(request, response);
    }

    private boolean canManage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object loginUser = LoginUserSupport.getLoginUser(request.getSession());
        if (loginUser == null) {
            request.setAttribute("errorMessage", "Vui lòng đăng nhập bằng tài khoản quản trị.");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return false;
        }
        if (!LoginUserSupport.isAdminOrStaff(loginUser)) {
            request.getSession().setAttribute("errorMessage", "Bạn không có quyền quản lý đơn vé.");
            response.sendRedirect(request.getContextPath() + "/order/history");
            return false;
        }
        return true;
    }

    private void applyResult(HttpSession session, Object loginUser, OrderActionResult result) {
        if (result == null) {
            flash(session, "errorMessage", "Không xử lý được yêu cầu.");
            return;
        }
        flash(session, result.isSuccess() ? "successMessage" : "errorMessage", result.getMessage());
        if (result.getLoyaltyPoints() != null) {
            String currentId = LoginUserSupport.getUserId(loginUser);
            if (currentId != null && currentId.equals(result.getUserId())) {
                LoginUserSupport.setLoyaltyPoints(loginUser, result.getLoyaltyPoints());
                session.setAttribute("loginUser", loginUser);
            }
        }
    }

    private void flash(HttpSession session, String name, String message) {
        session.setAttribute(name, message);
    }

    private void prepare(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
    }
}
