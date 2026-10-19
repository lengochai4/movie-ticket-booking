package com.cinema.controller.booking;

import com.cinema.entity.Cinema;
import com.cinema.entity.Movie;
import com.cinema.entity.Room;
import com.cinema.entity.Showtime;
import com.cinema.service.booking.BookingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/booking/seats", "/booking/combo", "/booking/checkout", "/booking/complete"})
public class BookingFlowServlet extends HttpServlet {

    private final BookingService bookingService = new BookingService();

    private void prepareMockData(HttpServletRequest request) {
        // Phim mẫu
        Movie movie = new Movie(
                "Shaun the Sheep: Quái Lạ Ghé Nhà",
                "Chú cừu Shaun tinh nghịch cùng phi đội nông trại giải cứu một sinh vật ngoài hành tinh dễ thương có siêu năng lực kỳ lạ bị lạc xuống Trái Đất.",
                "https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
                "Will Becher, Richard Phelan",
                86,
                LocalDate.of(2024, 10, 16)
        );
        movie.setMovieID("movie-shaun-01");
        movie.setAgeRating("P");

        // Rạp & Phòng mẫu
        Cinema cinema = new Cinema();
        cinema.setCinemaID("cinema-01");
        cinema.setName("Beta Đan Phượng");

        Room room = new Room();
        room.setRoomID("room-05");
        room.setName("Phòng chiếu P5");
        room.setCinema(cinema);

        // Suất chiếu mẫu
        Showtime showtime = new Showtime();
        showtime.setShowtimeID("st-shaun-0915");
        showtime.setMovie(movie);
        showtime.setRoom(room);
        LocalDateTime startTime = LocalDateTime.of(2026, 10, 16, 9, 15);
        showtime.setStartTime(startTime);

        request.setAttribute("movie", movie);
        request.setAttribute("cinema", cinema);
        request.setAttribute("room", room);
        request.setAttribute("showtime", showtime);

        // Định dạng rõ ràng cho vé xem phim
        request.setAttribute("formattedTime", startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        request.setAttribute("formattedDate", startTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        String roomNumber = room.getName() != null ? room.getName().replaceAll("[^0-9]", "") : "05";
        if (roomNumber.length() == 1) roomNumber = "0" + roomNumber;
        request.setAttribute("roomNumber", !roomNumber.isEmpty() ? roomNumber : "05");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        prepareMockData(request);

        String path = request.getServletPath();

        switch (path) {
            case "/booking/seats":
                request.getRequestDispatcher("/WEB-INF/views/booking/seat-selection.jsp").forward(request, response);
                break;
            case "/booking/combo":
                // Khi người dùng bấm "Tiếp tục · Bắp nước" từ màn hình chọn ghế
                String selectedSeatsStr = request.getParameter("selectedSeats");
                String showtimeId = request.getParameter("showtimeId");
                if (showtimeId == null || showtimeId.trim().isEmpty() || "st-shaun-0915".equals(showtimeId)) {
                    showtimeId = "ST_01"; // Map về suất chiếu chuẩn trong database
                }

                if (selectedSeatsStr != null && !selectedSeatsStr.trim().isEmpty()) {
                    List<String> seatList = new ArrayList<>();
                    for (String s : selectedSeatsStr.split(",")) {
                        String trimmed = s.trim();
                        if (!trimmed.isEmpty()) seatList.add(trimmed);
                    }
                    if (!seatList.isEmpty()) {
                        int userId = 2; // Khách hàng Lê Ngọc Hải mặc định
                        HttpSession session = request.getSession();
                        Object loginUser = session.getAttribute("loginUser");
                        if (loginUser == null) {
                            loginUser = session.getAttribute("user");
                        }
                        if (loginUser != null) {
                            try {
                                java.lang.reflect.Method getIdMethod = loginUser.getClass().getMethod("getUserID");
                                Object idVal = getIdMethod.invoke(loginUser);
                                if (idVal instanceof Integer) {
                                    userId = (Integer) idVal;
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        String bookingId = bookingService.holdSeats(userId, showtimeId, seatList);
                        System.out.println("[BookingFlowServlet] holdSeats for user " + userId + ", showtime " + showtimeId + ", seats " + seatList + " -> bookingId: " + bookingId);
                        if (bookingId != null) {
                            session.setAttribute("currentBookingId", bookingId);
                            session.setAttribute("currentShowtimeId", showtimeId);
                            session.setAttribute("heldSeats", selectedSeatsStr);
                            request.setAttribute("bookingId", bookingId);
                        }
                    }
                }
                request.getRequestDispatcher("/WEB-INF/views/booking/combo-selection.jsp").forward(request, response);
                break;
            case "/booking/checkout":
                request.getRequestDispatcher("/WEB-INF/views/booking/checkout.jsp").forward(request, response);
                break;
            case "/booking/complete":
                // Xử lý xác nhận thanh toán theo phương thức đã chọn (VNPAY hoặc MOMO)
                String paymentMethod = request.getParameter("paymentMethod");
                String methodDisplay = "MOMO".equalsIgnoreCase(paymentMethod) ? "Ví MoMo" : "Cổng VNPAY";
                request.setAttribute("paymentMethod", paymentMethod);

                // Cập nhật trạng thái booking thành PAID trong Database
                HttpSession curSession = request.getSession(false);
                if (curSession != null) {
                    String curBookingId = (String) curSession.getAttribute("currentBookingId");
                    if (curBookingId != null) {
                        bookingService.confirmPayment(curBookingId, "PAY_" + (paymentMethod != null ? paymentMethod.toUpperCase() : "ONLINE"));
                    }
                }

                request.setAttribute("successMessage", "Thanh toán thành công qua " + methodDisplay + "! Vé điện tử và mã QR đã được gửi về email của bạn.");
                request.getRequestDispatcher("/WEB-INF/views/booking/checkout.jsp").forward(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/movies");
                break;
        }
    }
}
