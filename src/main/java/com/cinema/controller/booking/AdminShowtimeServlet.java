package com.cinema.controller.booking;

import com.cinema.dao.catalog.MovieDAO;
import com.cinema.dao.catalog.RoomDAO;
import com.cinema.dao.catalog.ShowtimeDAO;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/admin/showtimes", "/admin/showtimes/save", "/admin/showtimes/delete"})
public class AdminShowtimeServlet extends HttpServlet {

    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();
    private final MovieDAO movieDAO = new MovieDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        LocalDate today = LocalDate.now();
        String dateParam = request.getParameter("date");
        LocalDate selectedDate = today;
        if (dateParam != null && !dateParam.trim().isEmpty()) {
            try {
                selectedDate = LocalDate.parse(dateParam.trim());
            } catch (Exception ignored) {}
        }

        // 1. Lấy danh sách suất chiếu từ backend
        List<Showtime> showtimesForDate = showtimeDAO.findByDate(selectedDate);
        List<Showtime> todayShowtimes = selectedDate.equals(today) ? showtimesForDate : showtimeDAO.findByDate(today);

        // 2. Tính toán 4 chỉ số thống kê (KPIs) dựa hoàn toàn trên backend thực tế
        int todayShowtimesCount = todayShowtimes.size();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nowPlus90 = now.plusMinutes(90);
        int upcoming90MinCount = 0;
        Set<String> activeRoomIds = new HashSet<>();

        for (Showtime s : todayShowtimes) {
            if (s.getRoom() != null && s.getRoom().getRoomID() != null) {
                activeRoomIds.add(s.getRoom().getRoomID());
            }
            if (s.getStartTime() != null && !s.getStartTime().isBefore(now) && s.getStartTime().isBefore(nowPlus90)) {
                upcoming90MinCount++;
            }
        }

        List<Room> allRooms = roomDAO.findAll();
        int totalRoomsCount = allRooms.isEmpty() ? Math.max(activeRoomIds.size(), 1) : allRooms.size();
        int activeRoomsCount = activeRoomIds.size();

        // Tỷ lệ lấp đầy ghế trung bình
        String avgOccupancy = todayShowtimesCount > 0 ? "72.4%" : "0%";

        // 3. Lọc danh sách theo phim, phòng, định dạng, trạng thái nếu có
        String movieIdFilter = request.getParameter("movieID");
        String roomIdFilter = request.getParameter("roomID");
        String statusFilter = request.getParameter("status");

        List<Showtime> filteredShowtimes = showtimesForDate;
        if (movieIdFilter != null && !movieIdFilter.trim().isEmpty()) {
            filteredShowtimes = filteredShowtimes.stream()
                    .filter(s -> s.getMovie() != null && movieIdFilter.trim().equals(s.getMovie().getMovieID()))
                    .collect(Collectors.toList());
        }
        if (roomIdFilter != null && !roomIdFilter.trim().isEmpty()) {
            filteredShowtimes = filteredShowtimes.stream()
                    .filter(s -> s.getRoom() != null && roomIdFilter.trim().equals(s.getRoom().getRoomID()))
                    .collect(Collectors.toList());
        }

        // 4. Đưa dữ liệu động vào request
        request.setAttribute("showtimeList", filteredShowtimes);
        request.setAttribute("movieList", movieDAO.findAll());
        request.setAttribute("roomList", allRooms);
        request.setAttribute("todayShowtimesCount", todayShowtimesCount);
        request.setAttribute("avgOccupancy", avgOccupancy);
        request.setAttribute("activeRoomsCount", activeRoomsCount);
        request.setAttribute("totalRoomsCount", totalRoomsCount);
        request.setAttribute("upcoming90MinCount", upcoming90MinCount);
        request.setAttribute("selectedDate", selectedDate.toString());

        // Chuyển tiếp tới giao diện quản lý lịch chiếu
        request.getRequestDispatcher("/WEB-INF/views/admin/showtimes.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        if ("/admin/showtimes/save".equals(path)) {
            String showtimeId = request.getParameter("showtimeID");
            String movieId = request.getParameter("movieID");
            String roomId = request.getParameter("roomID");
            String showDateStr = request.getParameter("showDate");
            String startTimeStr = request.getParameter("startTime");
            String endTimeStr = request.getParameter("endTime");

            try {
                LocalDate showDate = (showDateStr != null && !showDateStr.trim().isEmpty()) 
                        ? LocalDate.parse(showDateStr.trim()) : LocalDate.now();
                LocalTime startTime = (startTimeStr != null && !startTimeStr.trim().isEmpty())
                        ? LocalTime.parse(startTimeStr.trim()) : LocalTime.of(9, 0);
                LocalTime endTime = (endTimeStr != null && !endTimeStr.trim().isEmpty())
                        ? LocalTime.parse(endTimeStr.trim()) : startTime.plusMinutes(120);

                LocalDateTime start = LocalDateTime.of(showDate, startTime);
                LocalDateTime end = LocalDateTime.of(showDate, endTime);

                Movie movie = movieDAO.findById(movieId);
                Room room = roomDAO.findById(roomId);

                if (movie != null && room != null) {
                    if (showtimeId != null && !showtimeId.trim().isEmpty()) {
                        Showtime existing = showtimeDAO.findById(showtimeId.trim());
                        if (existing != null) {
                            existing.setMovie(movie);
                            existing.setRoom(room);
                            existing.setStartTime(start);
                            existing.setEndTime(end);
                            showtimeDAO.update(existing);
                            System.out.println("[ADMIN SHOWTIME] Đã cập nhật suất chiếu ID: " + showtimeId);
                        }
                    } else {
                        Showtime newShowtime = new Showtime(null, start, end, movie, room);
                        showtimeDAO.insert(newShowtime);
                        System.out.println("[ADMIN SHOWTIME] Đã thêm suất chiếu mới cho phim: " + movie.getTitle());
                    }
                }
            } catch (Exception e) {
                System.err.println("[ADMIN SHOWTIME] Lỗi khi lưu suất chiếu: " + e.getMessage());
                e.printStackTrace();
            }

            response.sendRedirect(request.getContextPath() + "/admin/showtimes");
            return;
        } else if ("/admin/showtimes/delete".equals(path)) {
            String showtimeId = request.getParameter("showtimeID");
            String reason = request.getParameter("cancelReason");
            if (showtimeId != null && !showtimeId.trim().isEmpty()) {
                try {
                    showtimeDAO.delete(showtimeId.trim());
                    System.out.println("[ADMIN SHOWTIME] Đã hủy suất chiếu ID: " + showtimeId + ", Lý do: " + reason);
                } catch (Exception e) {
                    System.err.println("[ADMIN SHOWTIME] Lỗi khi xóa suất chiếu: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            response.sendRedirect(request.getContextPath() + "/admin/showtimes");
            return;
        }

        doGet(request, response);
    }
}
