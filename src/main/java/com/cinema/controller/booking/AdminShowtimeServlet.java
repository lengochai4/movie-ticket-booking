package com.cinema.controller.booking;

import com.cinema.dao.admin.AdminMovieDAO;
import com.cinema.dao.admin.AdminShowtimeDAO;
import com.cinema.dao.admin.StatisticsDAO;
import com.cinema.dto.DatePillDTO;
import com.cinema.dto.MovieDTO;
import com.cinema.dto.RoomDTO;
import com.cinema.dto.SeatFillDTO;
import com.cinema.dto.ShowtimeDTO;
import com.cinema.dto.TimelineBlockDTO;
import com.cinema.dto.TimelineRowDTO;
import com.cinema.enums.BookingStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@WebServlet(urlPatterns = {"/admin/showtimes", "/admin/showtimes/save", "/admin/showtimes/delete"})
public class AdminShowtimeServlet extends HttpServlet {

    private final AdminShowtimeDAO showtimeDAO = new AdminShowtimeDAO();
    private final AdminMovieDAO movieDAO = new AdminMovieDAO();
    private final StatisticsDAO statisticsDAO = new StatisticsDAO();

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String CANCELLED = BookingStatus.CANCELLED.name();
    private static final String PAID = BookingStatus.PAID.name();

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

        List<ShowtimeDTO> showtimesForDate = new ArrayList<>();
        List<ShowtimeDTO> todayShowtimes = new ArrayList<>();
        List<RoomDTO> allRooms = new ArrayList<>();
        List<MovieDTO> movies = new ArrayList<>();
        try {
            // 1. Lấy danh sách suất chiếu, phòng, phim
            showtimesForDate = showtimeDAO.findByDate(selectedDate);
            todayShowtimes = selectedDate.equals(today) ? showtimesForDate : showtimeDAO.findByDate(today);
            allRooms = showtimeDAO.findAllRooms();
            movies = movieDAO.findAll();
        } catch (RuntimeException e) {
            System.err.println("[ADMIN SHOWTIME] Lỗi khi tải dữ liệu: " + e.getMessage());
            request.setAttribute("errorMessage", "Không tải được dữ liệu suất chiếu. Vui lòng thử lại.");
        }

        // 2. Tính toán 4 chỉ số thống kê (KPIs) dựa hoàn toàn trên backend thực tế
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nowPlus90 = now.plusMinutes(90);
        int upcoming90MinCount = 0;
        Set<String> activeRoomIds = new HashSet<>();
        for (ShowtimeDTO s : todayShowtimes) {
            activeRoomIds.add(s.getRoom().getRoomID());
            if (!s.getStartTime().isBefore(now) && s.getStartTime().isBefore(nowPlus90)) {
                upcoming90MinCount++;
            }
        }
        int totalRoomsCount = allRooms.isEmpty() ? Math.max(activeRoomIds.size(), 1) : allRooms.size();

        // 3. Số vé đã bán / tỷ lệ lấp đầy (đọc bookings, chỉ tính đơn PAID)
        String avgOccupancy = "0%";
        Map<String, Long> soldById = new HashMap<>();
        try {
            SeatFillDTO fill = statisticsDAO.getSeatFill(PAID, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
            avgOccupancy = fill.getFillRateText();
            soldById = statisticsDAO.getSoldTicketsByShowtime(PAID, selectedDate.atStartOfDay(), selectedDate.plusDays(1).atStartOfDay());
        } catch (RuntimeException e) {
            System.err.println("[ADMIN SHOWTIME] Lỗi khi đọc số vé đã bán: " + e.getMessage());
            avgOccupancy = "—";
        }

        // 4. Trạng thái và % lấp đầy từng suất của ngày đang xem
        //    ENDED: đã bắt đầu/đã chiếu; SOON: bắt đầu trong 90 phút tới; OPEN: đang mở bán
        for (ShowtimeDTO s : showtimesForDate) {
            s.setStatus(!s.getStartTime().isAfter(now) ? "ENDED"
                    : (s.getStartTime().isBefore(nowPlus90) ? "SOON" : "OPEN"));
            long sold = soldById.getOrDefault(s.getShowtimeID(), 0L);
            int capacity = s.getRoom().getCapacity();
            s.setSoldTickets(sold);
            s.setFillPercent(capacity > 0 ? (int) Math.min(100, Math.round(sold * 100.0 / capacity)) : 0);
        }

        // 5. Đưa dữ liệu động vào request
        request.setAttribute("showtimeList", showtimesForDate);
        request.setAttribute("timelineRows", buildTimelineRows(showtimesForDate));
        request.setAttribute("movieList", movies);
        request.setAttribute("roomList", allRooms);
        request.setAttribute("todayShowtimesCount", todayShowtimes.size());
        request.setAttribute("avgOccupancy", avgOccupancy);
        request.setAttribute("activeRoomsCount", activeRoomIds.size());
        request.setAttribute("totalRoomsCount", totalRoomsCount);
        request.setAttribute("upcoming90MinCount", upcoming90MinCount);
        request.setAttribute("selectedDate", selectedDate.toString());
        request.setAttribute("datePills", buildDatePills(today));

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
            String backDate = null;
            try {
                backDate = LocalDate.parse(trimToNull(request.getParameter("date"))).toString();
            } catch (Exception ignored) {}
            response.sendRedirect(request.getContextPath() + "/admin/showtimes" + (backDate != null ? "?date=" + backDate : ""));
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
            MovieDTO movie = movieId != null ? movieDAO.findById(movieId) : null;
            RoomDTO room = roomId != null ? showtimeDAO.findRoomById(roomId) : null;
            if (movie == null || room == null) {
                session.setAttribute("errorMessage", "Không tìm thấy phim hoặc phòng chiếu đã chọn.");
                return showDate.toString();
            }
            // Phòng chưa có sơ đồ ghế thì khách không chọn được ghế nào
            if (room.getCapacity() <= 0) {
                session.setAttribute("errorMessage", room.getName() + " (" + room.getCinemaName()
                        + ") chưa có sơ đồ ghế. Hãy tạo sơ đồ ghế ở trang Quản lý rạp trước khi xếp suất chiếu.");
                return showDate.toString();
            }

            // 2. Giờ kết thúc = giờ bắt đầu + thời lượng phim + 15 phút dọn rạp (tự qua ngày nếu chiếu khuya)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = LocalDateTime.of(showDate, startTime);
            LocalDateTime end = start.plusMinutes(movie.getDuration() > 0 ? movie.getDuration() + 15 : 120);

            // 3. Với suất đang sửa: không sửa suất đã bắt đầu; suất đã có đơn đặt vé thì chỉ được đổi giá
            ShowtimeDTO existing = null;
            if (showtimeId != null) {
                existing = showtimeDAO.findById(showtimeId);
                if (existing == null) {
                    session.setAttribute("errorMessage", "Suất chiếu cần sửa không còn tồn tại.");
                    return showDate.toString();
                }
                if (existing.getStartTime().isBefore(now)) {
                    session.setAttribute("errorMessage", "Suất chiếu lúc " + existing.getTimeRange() + " ngày "
                            + existing.getFormattedDate() + " đã bắt đầu hoặc đã chiếu xong, không thể sửa.");
                    return showDate.toString();
                }
                boolean scheduleChanged = !existing.getMovie().getMovieID().equals(movie.getMovieID())
                        || !existing.getRoom().getRoomID().equals(room.getRoomID())
                        || !existing.getStartTime().equals(start);
                if (scheduleChanged) {
                    long openBookings = statisticsDAO.countOpenBookings(showtimeId, CANCELLED);
                    if (openBookings > 0) {
                        session.setAttribute("errorMessage", "Suất chiếu này đã có " + openBookings
                                + " đơn đặt vé nên chỉ được đổi giá vé. Muốn đổi phim, phòng hoặc giờ chiếu, hãy hủy các đơn đó trước.");
                        return showDate.toString();
                    }
                }
            }

            // 4. Không xếp suất chiếu vào thời điểm đã qua
            if (start.isBefore(now)) {
                session.setAttribute("errorMessage", "Không thể xếp suất chiếu vào thời điểm đã qua ("
                        + start.format(TIME_FORMAT) + " ngày " + showDate.format(DATE_FORMAT) + ").");
                return showDate.toString();
            }

            // 5. Kiểm tra trùng giờ với suất chiếu khác trong cùng phòng
            List<ShowtimeDTO> overlaps = showtimeDAO.findOverlapping(room.getRoomID(), start, end, showtimeId);
            if (!overlaps.isEmpty()) {
                ShowtimeDTO conflict = overlaps.get(0);
                session.setAttribute("errorMessage", "Phòng " + room.getName() + " đã có suất chiếu \""
                        + conflict.getMovie().getTitle()
                        + "\" lúc " + conflict.getTimeRange() + " ngày " + conflict.getFormattedDate()
                        + ", trùng với khung giờ " + start.format(TIME_FORMAT) + " - " + end.format(TIME_FORMAT) + ".");
                return showDate.toString();
            }

            // 6. Lưu vào database qua DTO
            ShowtimeDTO showtime = existing != null ? existing : new ShowtimeDTO();
            showtime.setMovie(movie);
            showtime.setRoom(room);
            showtime.setStartTime(start);
            showtime.setEndTime(end);
            showtime.setPrice(price);
            if (existing != null) {
                showtimeDAO.update(showtime);
                session.setAttribute("successMessage", "Đã cập nhật suất chiếu \"" + movie.getTitle() + "\" lúc "
                        + showtime.getTimeRange() + " ngày " + showtime.getFormattedDate() + ".");
            } else {
                showtimeDAO.insert(showtime);
                session.setAttribute("successMessage", "Đã thêm suất chiếu \"" + movie.getTitle() + "\" lúc "
                        + showtime.getTimeRange() + " ngày " + showtime.getFormattedDate() + ".");
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
            // Không xóa suất còn đơn đặt vé chưa hủy (vé của khách sẽ mất suất chiếu)
            long openBookings = statisticsDAO.countOpenBookings(showtimeId, CANCELLED);
            if (openBookings > 0) {
                session.setAttribute("errorMessage", "Suất chiếu này còn " + openBookings
                        + " đơn đặt vé chưa hủy, không thể xóa. Hãy hủy các đơn đó ở trang Quản lý đặt vé trước.");
                return;
            }
            if (showtimeDAO.delete(showtimeId)) {
                System.out.println("[ADMIN SHOWTIME] Đã hủy suất chiếu ID: " + showtimeId + ", Lý do: " + reason);
                session.setAttribute("successMessage", "Đã hủy suất chiếu thành công.");
            } else {
                session.setAttribute("errorMessage", "Suất chiếu cần hủy không còn tồn tại.");
            }
        } catch (RuntimeException e) {
            System.err.println("[ADMIN SHOWTIME] Lỗi khi xóa suất chiếu: " + e.getMessage());
            if (isForeignKeyViolation(e)) {
                // Đơn đã hủy vẫn giữ tham chiếu tới suất chiếu để lưu lịch sử
                session.setAttribute("errorMessage", "Suất chiếu này còn lịch sử đơn đặt vé (đã hủy) nên không thể xóa.");
            } else {
                e.printStackTrace();
                session.setAttribute("errorMessage", "Không thể hủy suất chiếu do lỗi hệ thống. Vui lòng thử lại.");
            }
        }
    }

    // PostgreSQL trả SQLState 23503 khi vi phạm khóa ngoại (bản ghi đang được bảng khác tham chiếu)
    private boolean isForeignKeyViolation(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SQLException && "23503".equals(((SQLException) t).getSQLState())) {
                return true;
            }
        }
        return false;
    }

    // Nhận giá dạng "90.000", "90,000", "90000" hoặc "90.000 đ" -> 90000.0
    private Double parsePrice(String priceStr) {
        if (priceStr == null) return null;
        String digits = priceStr.replaceAll("[^0-9]", "");
        if (digits.isEmpty() || digits.length() > 9) return null;
        return Double.valueOf(digits);
    }

    // Dữ liệu Timeline: mỗi phòng có suất trong ngày là 1 hàng, mỗi suất là 1 khối đặt theo % trên trục 08:00 - 24:00
    private List<TimelineRowDTO> buildTimelineRows(List<ShowtimeDTO> showtimes) {
        final int axisStart = 8 * 60;   // 08:00
        final int axisLength = 16 * 60; // 08:00 -> 24:00
        Map<String, TimelineRowDTO> rowsByRoom = new LinkedHashMap<>();
        for (ShowtimeDTO s : showtimes) {
            TimelineRowDTO row = rowsByRoom.computeIfAbsent(s.getRoom().getRoomID(), k -> {
                TimelineRowDTO r = new TimelineRowDTO();
                r.setRoom(s.getRoom());
                return r;
            });
            int startMin = s.getStartTime().getHour() * 60 + s.getStartTime().getMinute();
            long durationMin = Duration.between(s.getStartTime(), s.getEndTime()).toMinutes();
            double left = Math.max(0, startMin - axisStart) * 100.0 / axisLength;
            double right = Math.min(axisLength, startMin - axisStart + durationMin) * 100.0 / axisLength;
            if (right <= 0 || left >= 100) continue; // nằm ngoài khung 08:00 - 24:00
            TimelineBlockDTO block = new TimelineBlockDTO();
            block.setShowtime(s);
            block.setLeft(String.format(Locale.US, "%.2f", left));
            block.setWidth(String.format(Locale.US, "%.2f", Math.max(1.5, right - left)));
            row.getBlocks().add(block);
        }
        // Sắp xếp hàng theo tên rạp rồi tên phòng
        List<TimelineRowDTO> rows = new ArrayList<>(rowsByRoom.values());
        rows.sort(Comparator.comparing((TimelineRowDTO r) -> r.getRoom().getCinemaName() + " " + r.getRoom().getName()));
        return rows;
    }

    // 7 ngày kể từ hôm nay cho thanh chọn ngày
    private List<DatePillDTO> buildDatePills(LocalDate today) {
        String[] dayNames = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
        DateTimeFormatter dayNum = DateTimeFormatter.ofPattern("dd/MM");
        List<DatePillDTO> pills = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = today.plusDays(i);
            pills.add(new DatePillDTO(d.toString(),
                    i == 0 ? "Hôm nay" : dayNames[d.getDayOfWeek().getValue() - 1],
                    d.format(dayNum)));
        }
        return pills;
    }

    private String trimToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
