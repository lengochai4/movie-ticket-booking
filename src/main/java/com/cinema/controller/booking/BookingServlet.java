package com.cinema.controller.booking;

import com.cinema.model.entity.Seat;
import com.cinema.service.booking.BookingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/booking/*")
public class BookingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private BookingService bookingService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.bookingService = new BookingService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        String action = request.getParameter("action");
        if (action != null && !action.trim().isEmpty()) {
            pathInfo = action.startsWith("/") ? action : "/" + action;
        } else if (pathInfo == null || pathInfo.equals("/")) {
            pathInfo = "/seats";
        }

        switch (pathInfo) {
            case "/seats":
                handleGetSeats(request, response);
                break;
            default:
                // Nếu gọi route khác trong /booking, chuyển hướng về xem danh sách phim
                response.sendRedirect(request.getContextPath() + "/movies");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        String action = request.getParameter("action");
        if (action != null && !action.trim().isEmpty()) {
            pathInfo = action.startsWith("/") ? action : "/" + action;
        } else if (pathInfo == null) {
            pathInfo = "";
        }

        switch (pathInfo) {
            case "/hold":
                handlePostHold(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    /**
     * GET /booking/seats?showtimeId=...
     * Lấy danh sách ghế từ Database và forward sang seat-map.jsp
     */
    private void handleGetSeats(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String showtimeId = request.getParameter("showtimeId");
        if (showtimeId == null || showtimeId.trim().isEmpty()) {
            showtimeId = request.getParameter("id"); // Fallback
        }

        if (showtimeId == null || showtimeId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        List<Seat> seats = bookingService.getSeatMap(showtimeId);
        request.setAttribute("seats", seats);
        request.setAttribute("showtimeId", showtimeId);

        String error = request.getParameter("error");
        if (error != null) {
            request.setAttribute("error", error);
        }

        request.getRequestDispatcher("/WEB-INF/views/booking/seat-map.jsp").forward(request, response);
    }

    /**
     * POST /booking/hold
     * Nhận danh sách ghế và giữ chỗ trong 10 phút.
     * Thành công -> redirect sang /booking/combos?bookingId=...
     * Thất bại (trùng ghế) -> redirect về /booking/seats?showtimeId=...&error=occupied
     */
    private void handlePostHold(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String showtimeId = request.getParameter("showtimeId");
        if (showtimeId == null || showtimeId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/movies");
            return;
        }

        // Đọc danh sách ghế từ form (hỗ trợ cả mảng checkbox và chuỗi phân cách bởi dấu phẩy)
        List<String> seatList = new ArrayList<>();
        String[] seatValues = request.getParameterValues("selectedSeats");
        if (seatValues != null && seatValues.length > 0) {
            for (String val : seatValues) {
                if (val != null) {
                    for (String part : val.split(",")) {
                        String trimmed = part.trim();
                        if (!trimmed.isEmpty() && !seatList.contains(trimmed)) {
                            seatList.add(trimmed);
                        }
                    }
                }
            }
        }

        if (seatList.isEmpty()) {
            String singleParam = request.getParameter("selectedSeats");
            if (singleParam != null && !singleParam.trim().isEmpty()) {
                for (String part : singleParam.split(",")) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty() && !seatList.contains(trimmed)) {
                        seatList.add(trimmed);
                    }
                }
            }
        }

        if (seatList.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/booking/seats?showtimeId=" + showtimeId + "&error=no_seats_selected");
            return;
        }

        // Lấy userID: mặc định là 1 (hoặc lấy từ session nếu đã đăng nhập)
        int userID = 1;
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userObj = session.getAttribute("loginUser");
            if (userObj == null) {
                userObj = session.getAttribute("user");
            }
            if (userObj != null) {
                try {
                    java.lang.reflect.Method getIdMethod = userObj.getClass().getMethod("getUserID");
                    Object idVal = getIdMethod.invoke(userObj);
                    if (idVal instanceof Integer) {
                        userID = (Integer) idVal;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        // Thực hiện giữ ghế với transaction chống tranh chấp
        String bookingId = bookingService.holdSeats(userID, showtimeId, seatList);

        if (bookingId != null) {
            // Thành công -> chuyển sang bước chọn bắp nước
            response.sendRedirect(request.getContextPath() + "/booking/combos?bookingId=" + bookingId);
        } else {
            // Thất bại do bị trùng ghế hoặc ghế vừa bị đặt
            response.sendRedirect(request.getContextPath() + "/booking/seats?showtimeId=" + showtimeId + "&error=occupied");
        }
    }
}
