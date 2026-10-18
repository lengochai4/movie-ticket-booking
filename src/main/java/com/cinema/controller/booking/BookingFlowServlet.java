package com.cinema.controller.booking;

import com.cinema.entity.Cinema;
import com.cinema.entity.Movie;
import com.cinema.entity.Room;
import com.cinema.entity.Showtime;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@WebServlet(urlPatterns = {"/booking/seats", "/booking/combo", "/booking/checkout", "/booking/complete"})
public class BookingFlowServlet extends HttpServlet {

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
                request.setAttribute("successMessage", "Thanh toán thành công qua " + methodDisplay + "! Vé điện tử và mã QR đã được gửi về email của bạn.");
                request.getRequestDispatcher("/WEB-INF/views/booking/checkout.jsp").forward(request, response);
                break;
            default:
                response.sendRedirect(request.getContextPath() + "/movies");
                break;
        }
    }
}
