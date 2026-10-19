package com.cinema.controller.booking;

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

@WebServlet(urlPatterns = {"/order/history", "/order/ticket-detail", "/booking/history", "/order/cancel"})
public class OrderServlet extends HttpServlet {

    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        prepare(request, response);
        if ("/order/ticket-detail".equals(request.getServletPath())) {
            showTicket(request, response);
        } else {
            showHistory(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        prepare(request, response);
        if ("/order/cancel".equals(request.getServletPath())) {
            cancel(request, response);
            return;
        }
        doGet(request, response);
    }

    private void showHistory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Object loginUser = LoginUserSupport.getLoginUser(session);
        if (loginUser == null) {
            request.setAttribute("errorMessage", "Vui lòng đăng nhập để xem lịch sử đặt vé.");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }
        String userId = LoginUserSupport.getUserId(loginUser);
        List<OrderView> orderList = new ArrayList<>();
        int loyaltyPoints = 0;
        if (userId == null) {
            request.setAttribute("errorMessage", "Tài khoản chưa có mã thành viên nên chưa xem được lịch sử đặt vé.");
        } else {
            try {
                orderList = orderService.findHistory(userId);
                loyaltyPoints = orderService.ensurePoints(userId, LoginUserSupport.getLoyaltyPoints(loginUser));
                LoginUserSupport.setLoyaltyPoints(loginUser, loyaltyPoints);
                session.setAttribute("loginUser", loginUser);
            } catch (Exception e) {
                request.setAttribute("errorMessage", "Không tải được lịch sử đặt vé. Vui lòng thử lại sau.");
            }
        }
        request.setAttribute("orderList", orderList);
        request.setAttribute("orderCount", orderList.size());
        request.setAttribute("ordersBound", Boolean.TRUE);
        request.setAttribute("loyaltyPoints", loyaltyPoints);
        request.getRequestDispatcher("/WEB-INF/views/order/history.jsp").forward(request, response);
    }

    private void showTicket(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Object loginUser = LoginUserSupport.getLoginUser(session);
        if (loginUser == null) {
            request.setAttribute("errorMessage", "Vui lòng đăng nhập để xem vé điện tử.");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
            return;
        }
        String key = request.getParameter("bookingId");
        if (key == null || key.isBlank()) {
            key = request.getParameter("id");
        }
        if (key == null || key.isBlank()) {
            flash(session, "errorMessage", "Thiếu mã vé điện tử.");
            response.sendRedirect(request.getContextPath() + "/order/history");
            return;
        }
        try {
            OrderView order = orderService.findTicket(key);
            if (order == null) {
                flash(session, "errorMessage", "Không tìm thấy vé điện tử.");
                response.sendRedirect(request.getContextPath() + "/order/history");
                return;
            }
            String userId = LoginUserSupport.getUserId(loginUser);
            boolean owner = userId != null && userId.equals(order.getUserId());
            if (!owner && !LoginUserSupport.isAdminOrStaff(loginUser)) {
                flash(session, "errorMessage", "Bạn không có quyền xem vé này.");
                response.sendRedirect(request.getContextPath() + "/order/history");
                return;
            }
            int loyaltyPoints = 0;
            if (userId != null) {
                loyaltyPoints = orderService.ensurePoints(userId, LoginUserSupport.getLoyaltyPoints(loginUser));
                LoginUserSupport.setLoyaltyPoints(loginUser, loyaltyPoints);
                session.setAttribute("loginUser", loginUser);
            }
            request.setAttribute("order", order);
            request.setAttribute("ticketBound", Boolean.TRUE);
            request.setAttribute("loyaltyPoints", loyaltyPoints);
            request.getRequestDispatcher("/WEB-INF/views/order/ticket-detail.jsp").forward(request, response);
        } catch (Exception e) {
            flash(session, "errorMessage", "Không tải được vé điện tử. Vui lòng thử lại sau.");
            response.sendRedirect(request.getContextPath() + "/order/history");
        }
    }

    private void cancel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        Object loginUser = LoginUserSupport.getLoginUser(session);
        if (loginUser == null) {
            flash(session, "errorMessage", "Vui lòng đăng nhập để hủy vé.");
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        String userId = LoginUserSupport.getUserId(loginUser);
        OrderActionResult result = orderService.cancelForCustomer(userId, request.getParameter("orderCode"));
        applyResult(session, loginUser, result);
        response.sendRedirect(request.getContextPath() + "/order/history");
    }

    private void applyResult(HttpSession session, Object loginUser, OrderActionResult result) {
        if (result == null) {
            flash(session, "errorMessage", "Không xử lý được yêu cầu.");
            return;
        }
        flash(session, result.isSuccess() ? "successMessage" : "errorMessage", result.getMessage());
        if (result.getLoyaltyPoints() != null && loginUser != null) {
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
