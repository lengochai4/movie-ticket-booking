package com.cinema.controller.booking;

import com.cinema.dao.admin.StatisticsDAO;
import com.cinema.dao.catalog.MovieDAO;
import com.cinema.dao.catalog.RoomDAO;
import com.cinema.dao.catalog.ShowtimeDAO;
import com.cinema.entity.Movie;
import com.cinema.entity.Room;
import com.cinema.entity.Showtime;
import com.cinema.enums.BookingStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
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

    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();
    private final MovieDAO movieDAO = new MovieDAO();
    private final RoomDAO roomDAO = new RoomDAO();
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

        // 3. Số vé đã bán / tỷ lệ lấp đầy (đọc bookings, chỉ tính đơn PAID)
        String avgOccupancy = "0%";
        Map<String, Long> soldById = new HashMap<>();
        try {
            double[] fill = statisticsDAO.getSeatFill(PAID, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
            if (fill[1] > 0) {
                avgOccupancy = String.format(Locale.US, "%.1f%%", fill[0] * 100.0 / fill[1]);
            }
            soldById = statisticsDAO.getSoldTicketsByShowtime(PAID, selectedDate.atStartOfDay(), selectedDate.plusDays(1).atStartOfDay());
        } catch (RuntimeException e) {
            System.err.println("[ADMIN SHOWTIME] Lỗi khi đọc số vé đã bán: " + e.getMessage());
            avgOccupancy = "—";
        }

        // 4. Trạng thái và % lấp đầy từng suất của ngày đang xem
        //    ENDED: đã bắt đầu/đã chiếu; SOON: bắt đầu trong 90 phút tới; OPEN: đang mở bán
        Map<String, String> statusById = new HashMap<>();
        Map<String, Integer> fillById = new HashMap<>();
        for (Showtime s : showtimesForDate) {
            String status = !s.getStartTime().isAfter(now) ? "ENDED"
                    : (s.getStartTime().isBefore(nowPlus90) ? "SOON" : "OPEN");
            statusById.put(s.getShowtimeID(), status);
            int capacity = s.getRoom() != null ? s.getRoom().getCapacity() : 0;
            long sold = soldById.getOrDefault(s.getShowtimeID(), 0L);
            fillById.put(s.getShowtimeID(), capacity > 0 ? (int) Math.min(100, Math.round(sold * 100.0 / capacity)) : 0);
        }

        // 5. Đưa dữ liệu động vào request
        request.setAttribute("showtimeList", showtimesForDate);
        request.setAttribute("statusById", statusById);
        request.setAttribute("soldById", soldById);
        request.setAttribute("fillById", fillById);
        request.setAttribute("timelineRows", buildTimelineRows(showtimesForDate));
        request.setAttribute("movieList", movieDAO.findAll());
        request.setAttribute("roomList", allRooms);
        request.setAttribute("todayShowtimesCount", todayShowtimesCount);
        request.setAttribute("avgOccupancy", avgOccupancy);
        request.setAttribute("activeRoomsCount", activeRoomsCount);
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
            Movie movie = movieId != null ? movieDAO.findById(movieId) : null;
            Room room = roomId != null ? roomDAO.findById(roomId) : null;
            if (movie == null || room == null) {
                session.setAttribute("errorMessage", "Không tìm thấy phim hoặc phòng chiếu đã chọn.");
                return showDate.toString();
            }

            // 2. Giờ kết thúc = giờ bắt đầu + thời lượng phim + 15 phút dọn rạp (tự qua ngày nếu chiếu khuya)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = LocalDateTime.of(showDate, startTime);
            LocalDateTime end = start.plusMinutes(movie.getDuration() > 0 ? movie.getDuration() + 15 : 120);

            // 3. Với suất đang sửa: không sửa suất đã bắt đầu; suất đã có đơn đặt vé thì chỉ được đổi giá
            Showtime existing = null;
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
            List<Showtime> overlaps = showtimeDAO.findOverlapping(room.getRoomID(), start, end, showtimeId);
            if (!overlaps.isEmpty()) {
                Showtime conflict = overlaps.get(0);
                session.setAttribute("errorMessage", "Phòng " + room.getName() + " đã có suất chiếu \""
                        + (conflict.getMovie() != null ? conflict.getMovie().getTitle() : "")
                        + "\" lúc " + conflict.getTimeRange() + " ngày " + conflict.getFormattedDate()
                        + ", trùng với khung giờ " + start.format(TIME_FORMAT) + " - " + end.format(TIME_FORMAT) + ".");
                return showDate.toString();
            }

            // 6. Lưu vào database
            if (existing != null) {
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
            // Không xóa suất còn đơn đặt vé chưa hủy (vé của khách sẽ mất suất chiếu)
            long openBookings = statisticsDAO.countOpenBookings(showtimeId, CANCELLED);
            if (openBookings > 0) {
                session.setAttribute("errorMessage", "Suất chiếu này còn " + openBookings
                        + " đơn đặt vé chưa hủy, không thể xóa. Hãy hủy các đơn đó ở trang Quản lý đặt vé trước.");
                return;
            }
            showtimeDAO.delete(showtimeId);
            System.out.println("[ADMIN SHOWTIME] Đã hủy suất chiếu ID: " + showtimeId + ", Lý do: " + reason);
            session.setAttribute("successMessage", "Đã hủy suất chiếu thành công.");
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
    private List<Map<String, Object>> buildTimelineRows(List<Showtime> showtimes) {
        final int axisStart = 8 * 60;   // 08:00
        final int axisLength = 16 * 60; // 08:00 -> 24:00
        Map<String, Map<String, Object>> rowsByRoom = new LinkedHashMap<>();
        for (Showtime s : showtimes) {
            if (s.getRoom() == null || s.getStartTime() == null || s.getEndTime() == null) continue;
            Map<String, Object> row = rowsByRoom.computeIfAbsent(s.getRoom().getRoomID(), k -> {
                Map<String, Object> r = new HashMap<>();
                r.put("room", s.getRoom());
                r.put("blocks", new ArrayList<Map<String, Object>>());
                return r;
            });
            int startMin = s.getStartTime().getHour() * 60 + s.getStartTime().getMinute();
            long durationMin = java.time.Duration.between(s.getStartTime(), s.getEndTime()).toMinutes();
            double left = Math.max(0, startMin - axisStart) * 100.0 / axisLength;
            double right = Math.min(axisLength, startMin - axisStart + durationMin) * 100.0 / axisLength;
            if (right <= 0 || left >= 100) continue; // nằm ngoài khung 08:00 - 24:00
            Map<String, Object> block = new HashMap<>();
            block.put("st", s);
            block.put("left", String.format(Locale.US, "%.2f", left));
            block.put("width", String.format(Locale.US, "%.2f", Math.max(1.5, right - left)));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> blocks = (List<Map<String, Object>>) row.get("blocks");
            blocks.add(block);
        }
        // Sắp xếp hàng theo tên rạp rồi tên phòng
        List<Map<String, Object>> rows = new ArrayList<>(rowsByRoom.values());
        rows.sort(Comparator.comparing((Map<String, Object> r) -> {
            Room room = (Room) r.get("room");
            return (room.getCinema() != null ? room.getCinema().getName() : "") + " " + room.getName();
        }));
        return rows;
    }

    // 7 ngày kể từ hôm nay cho thanh chọn ngày: {iso: yyyy-MM-dd, dayName: Hôm nay / Thứ 2..., dayNum: dd/MM}
    private List<Map<String, String>> buildDatePills(LocalDate today) {
        String[] dayNames = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
        List<Map<String, String>> pills = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = today.plusDays(i);
            Map<String, String> pill = new HashMap<>();
            pill.put("iso", d.toString());
            pill.put("dayName", i == 0 ? "Hôm nay" : dayNames[d.getDayOfWeek().getValue() - 1]);
            pill.put("dayNum", d.format(DateTimeFormatter.ofPattern("dd/MM")));
            pills.add(pill);
        }
        return pills;
    }

    private String trimToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
