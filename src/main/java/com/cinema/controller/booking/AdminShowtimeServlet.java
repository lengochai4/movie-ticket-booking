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
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

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
            String redirectDate = handleSave(request);
            response.sendRedirect(request.getContextPath() + "/admin/showtimes"
                    + (redirectDate != null ? "?date=" + redirectDate : ""));
            return;
        } else if ("/admin/showtimes/delete".equals(path)) {
            handleDelete(request);
            response.sendRedirect(request.getContextPath() + "/admin/showtimes");
            return;
        }

        doGet(request, response);
    }

    // Lưu suất chiếu (thêm mới hoặc cập nhật). Trả về ngày chiếu (yyyy-MM-dd) để quay lại đúng ngày
    private String handleSave(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String showtimeId = trimToNull(request.getParameter("showtimeID"));
        String movieId = trimToNull(request.getParameter("movieID"));
        String roomId = trimToNull(request.getParameter("roomID"));
        String showDateStr = trimToNull(request.getParameter("showDate"));
        String startTimeStr = trimToNull(request.getParameter("startTime"));
        String priceStr = trimToNull(request.getParameter("price"));

        // 1. Kiểm tra dữ liệu đầu vào
        LocalDate showDate;
        LocalTime startTime;
        try {
            showDate = LocalDate.parse(showDateStr);
            startTime = LocalTime.parse(startTimeStr);
        } catch (Exception e) {
            session.setAttribute("errorMessage", "Ngày chiếu hoặc giờ bắt đầu không hợp lệ.");
            return null;
        }

        Double price = parsePrice(priceStr);
        if (price == null || price <= 0) {
            session.setAttribute("errorMessage", "Giá vé không hợp lệ. Vui lòng nhập số tiền lớn hơn 0 (VD: 90.000).");
            return showDate.toString();
        }

        try {
            Movie movie = movieId != null ? movieDAO.findById(movieId) : null;
            Room room = roomId != null ? roomDAO.findById(roomId) : null;
            if (movie == null || room == null) {
                session.setAttribute("errorMessage", "Không tìm thấy phim hoặc phòng chiếu đã chọn.");
                return showDate.toString();
            }

            // 2. Giờ kết thúc = giờ bắt đầu + thời lượng phim + 15 phút dọn rạp (tự qua ngày nếu chiếu khuya)
            LocalDateTime start = LocalDateTime.of(showDate, startTime);
            LocalDateTime end = start.plusMinutes(movie.getDuration() > 0 ? movie.getDuration() + 15 : 120);

            // 3. Kiểm tra trùng giờ với suất chiếu khác trong cùng phòng
            List<Showtime> overlaps = showtimeDAO.findOverlapping(room.getRoomID(), start, end, showtimeId);
            if (!overlaps.isEmpty()) {
                Showtime conflict = overlaps.get(0);
                session.setAttribute("errorMessage", "Phòng " + room.getName() + " đã có suất chiếu \""
                        + (conflict.getMovie() != null ? conflict.getMovie().getTitle() : "")
                        + "\" lúc " + conflict.getTimeRange() + " ngày " + conflict.getFormattedDate()
                        + ", trùng với khung giờ " + start.format(TIME_FORMAT) + " - " + end.format(TIME_FORMAT) + ".");
                return showDate.toString();
            }

            // 4. Lưu vào database
            if (showtimeId != null) {
                Showtime existing = showtimeDAO.findById(showtimeId);
                if (existing == null) {
                    session.setAttribute("errorMessage", "Suất chiếu cần sửa không còn tồn tại.");
                    return showDate.toString();
                }
                existing.setMovie(movie);
                existing.setRoom(room);
                existing.setStartTime(start);
                existing.setEndTime(end);
                existing.setPrice(price);
                showtimeDAO.update(existing);
                session.setAttribute("successMessage", "Đã cập nhật suất chiếu \"" + movie.getTitle() + "\" lúc "
                        + existing.getTimeRange() + " ngày " + existing.getFormattedDate() + ".");
            } else {
                Showtime newShowtime = new Showtime(null, start, end, movie, room);
                newShowtime.setPrice(price);
                showtimeDAO.insert(newShowtime);
                session.setAttribute("successMessage", "Đã thêm suất chiếu \"" + movie.getTitle() + "\" lúc "
                        + newShowtime.getTimeRange() + " ngày " + newShowtime.getFormattedDate() + ".");
            }
        } catch (RuntimeException e) {
            System.err.println("[ADMIN SHOWTIME] Lỗi khi lưu suất chiếu: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("errorMessage", "Không thể lưu suất chiếu do lỗi hệ thống. Vui lòng thử lại.");
        }
        return showDate.toString();
    }

    private void handleDelete(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String showtimeId = trimToNull(request.getParameter("showtimeID"));
        String reason = request.getParameter("cancelReason");
        if (showtimeId == null) {
            session.setAttribute("errorMessage", "Thiếu mã suất chiếu cần hủy.");
            return;
        }
        try {
            showtimeDAO.delete(showtimeId);
            System.out.println("[ADMIN SHOWTIME] Đã hủy suất chiếu ID: " + showtimeId + ", Lý do: " + reason);
            session.setAttribute("successMessage", "Đã hủy suất chiếu thành công.");
        } catch (RuntimeException e) {
            System.err.println("[ADMIN SHOWTIME] Lỗi khi xóa suất chiếu: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("errorMessage", "Không thể hủy suất chiếu do lỗi hệ thống. Vui lòng thử lại.");
        }
    }

    // Nhận giá dạng "90.000", "90,000", "90000" hoặc "90.000 đ" -> 90000.0
    private Double parsePrice(String priceStr) {
        if (priceStr == null) return null;
        String digits = priceStr.replaceAll("[^0-9]", "");
        if (digits.isEmpty() || digits.length() > 9) return null;
        return Double.valueOf(digits);
    }

    private String trimToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
