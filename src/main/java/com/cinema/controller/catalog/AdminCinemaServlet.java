package com.cinema.controller.catalog;

import com.cinema.dao.catalog.CinemaDAO;
import com.cinema.dao.catalog.RoomDAO;
import com.cinema.dao.catalog.SeatDAO;
import com.cinema.entity.Cinema;
import com.cinema.entity.Room;
import com.cinema.entity.Seat;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@WebServlet(urlPatterns = {
    "/admin/cinemas",
    "/admin/cinemas/cinema/save",
    "/admin/cinemas/cinema/delete",
    "/admin/cinemas/room/save",
    "/admin/cinemas/room/delete",
    "/admin/cinemas/seats/generate",
    "/admin/cinemas/seats/toggle"
})
public class AdminCinemaServlet extends HttpServlet {

    // Giới hạn khớp với ô nhập trên giao diện (cinemas.jsp) và độ dài cột trong entity
    private static final int MIN_ROWS = 2, MAX_ROWS = 20;
    private static final int MIN_COLS = 4, MAX_COLS = 30;
    private static final int MAX_CINEMA_NAME = 255, MAX_ADDRESS = 500, MAX_ROOM_NAME = 100;

    private static final String FLASH_SUCCESS = "cinemaAdminSuccess";
    private static final String FLASH_ERROR = "cinemaAdminError";

    // Mã form dùng một lần: chống bấm nút nhiều lần tạo trùng rạp/phòng
    private static final String TOKENS_ISSUED = "cinemaAdminTokens";
    private static final String TOKENS_USED = "cinemaAdminUsedTokens";
    private static final String TOKEN_MUTEX = "cinemaAdminTokenMutex";
    private static final int MAX_TOKENS = 20; // đủ cho vài tab quản trị mở cùng lúc
    private static final Object MUTEX_INIT = new Object();

    // Kiểm tra trùng tên rồi thêm mới phải đi liền nhau, kể cả khi hai admin cùng thao tác
    private static final Object CATALOG_WRITE_LOCK = new Object();

    private enum TokenCheck { VALID, DUPLICATE, UNKNOWN }

    private final CinemaDAO cinemaDAO = new CinemaDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final SeatDAO seatDAO = new SeatDAO();

    // Rạp/phòng sẽ mở lại sau khi xử lý xong một thao tác
    private record Target(String cinemaId, String roomId) {}

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 1. Lấy toàn bộ danh sách cụm rạp
        List<Cinema> cinemaList = cinemaDAO.findAll();

        // 2. Xác định rạp đang được chọn
        String cinemaIdParam = trimToNull(request.getParameter("cinemaId"));
        Cinema selectedCinema = null;
        if (cinemaIdParam != null) {
            selectedCinema = cinemaDAO.findById(cinemaIdParam);
        }
        if (selectedCinema == null && !cinemaList.isEmpty()) {
            selectedCinema = cinemaList.get(0);
        }

        // 3. Lấy danh sách phòng của rạp được chọn; phòng được chọn phải thuộc rạp này
        List<Room> roomList = new ArrayList<>();
        Map<String, Long> seatCounts = Map.of();
        Room selectedRoom = null;
        if (selectedCinema != null) {
            roomList = roomDAO.findByCinemaId(selectedCinema.getCinemaID());
            seatCounts = roomDAO.countSeatsByCinema(selectedCinema.getCinemaID());

            String roomIdParam = trimToNull(request.getParameter("roomId"));
            if (roomIdParam != null) {
                for (Room r : roomList) {
                    if (r.getRoomID().equals(roomIdParam)) {
                        selectedRoom = r;
                        break;
                    }
                }
            }
            if (selectedRoom == null && !roomList.isEmpty()) {
                selectedRoom = roomList.get(0);
            }
        }

        // 4. Lấy danh sách ghế của phòng được chọn
        List<Seat> seatList = new ArrayList<>();
        if (selectedRoom != null) {
            seatList = seatDAO.findByRoomId(selectedRoom.getRoomID());
        }

        // 5. Thống kê nhanh KPIs
        int totalCinemas = cinemaList.size();
        int totalRooms = roomDAO.findAll().size();
        long totalSeats = seatDAO.countTotalSeats();
        long activeSeats = seatDAO.countActiveSeats();

        // Đẩy toàn bộ dữ liệu sang JSP
        request.setAttribute("cinemaList", cinemaList);
        request.setAttribute("selectedCinema", selectedCinema);
        request.setAttribute("selectedCinemaId", selectedCinema != null ? selectedCinema.getCinemaID() : "");

        request.setAttribute("roomList", roomList);
        request.setAttribute("seatCounts", seatCounts);
        request.setAttribute("selectedRoom", selectedRoom);
        request.setAttribute("selectedRoomId", selectedRoom != null ? selectedRoom.getRoomID() : "");

        request.setAttribute("seatList", seatList);
        request.setAttribute("totalCinemas", totalCinemas);
        request.setAttribute("totalRooms", totalRooms);
        request.setAttribute("totalSeats", totalSeats);
        request.setAttribute("activeSeats", activeSeats);

        // Thông báo từ thao tác trước (lưu trong session để còn sau khi redirect)
        HttpSession session = request.getSession();
        moveFlash(session, request, FLASH_SUCCESS, "successMessage");
        moveFlash(session, request, FLASH_ERROR, "errorMessage");
        request.setAttribute("formToken", issueToken(session));

        request.getRequestDispatcher("/WEB-INF/views/admin/cinemas.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();
        HttpSession session = request.getSession();

        // Nếu thao tác lỗi thì quay lại đúng rạp/phòng đang xem
        Target target = new Target(trimToNull(request.getParameter("cinemaId")), trimToNull(request.getParameter("roomId")));

        switch (consumeToken(session, request.getParameter("formToken"))) {
            case DUPLICATE -> {
                // Lần bấm lặp của một form đã xử lý: bỏ qua, giữ nguyên thông báo của lần đầu
                redirect(request, response, target);
                return;
            }
            case UNKNOWN -> {
                session.setAttribute(FLASH_ERROR, "Trang đã cũ hoặc phiên làm việc đã hết hạn. Vui lòng thực hiện lại thao tác.");
                redirect(request, response, target);
                return;
            }
            case VALID -> { }
        }

        try {
            switch (path) {
                case "/admin/cinemas/cinema/save" -> target = handleSaveCinema(request, session);
                case "/admin/cinemas/cinema/delete" -> target = handleDeleteCinema(request, session);
                case "/admin/cinemas/room/save" -> target = handleSaveRoom(request, session);
                case "/admin/cinemas/room/delete" -> target = handleDeleteRoom(request, session);
                case "/admin/cinemas/seats/generate" -> target = handleGenerateSeats(request, session);
                case "/admin/cinemas/seats/toggle" -> target = handleToggleSeat(request, session);
                default -> { }
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Lỗi dữ liệu nhập hoặc thao tác bị chặn: báo nguyên văn cho admin
            session.setAttribute(FLASH_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            session.setAttribute(FLASH_ERROR, "Không lưu được thay đổi do lỗi hệ thống. Vui lòng thử lại.");
        }

        redirect(request, response, target);
    }

    private Target handleSaveCinema(HttpServletRequest request, HttpSession session) {
        String cinemaId = trimToNull(request.getParameter("cinemaId"));
        String name = required(request.getParameter("name"), "Tên cụm rạp", MAX_CINEMA_NAME);
        String address = required(request.getParameter("address"), "Địa chỉ", MAX_ADDRESS);

        synchronized (CATALOG_WRITE_LOCK) {
            if (cinemaDAO.existsByName(name, cinemaId)) {
                throw new IllegalArgumentException("Đã có cụm rạp tên \"" + name + "\".");
            }
            return cinemaId == null
                    ? insertCinema(name, address, session)
                    : updateCinema(cinemaId, name, address, session);
        }
    }

    private Target insertCinema(String name, String address, HttpSession session) {
        Cinema cinema = new Cinema();
        cinema.setName(name);
        cinema.setAddress(address);
        cinemaDAO.insert(cinema);
        session.setAttribute(FLASH_SUCCESS, "Đã thêm cụm rạp \"" + name + "\".");
        return new Target(cinema.getCinemaID(), null);
    }

    private Target updateCinema(String cinemaId, String name, String address, HttpSession session) {
        Cinema cinema = cinemaDAO.findById(cinemaId);
        if (cinema == null) {
            throw new IllegalArgumentException("Cụm rạp cần sửa không còn tồn tại.");
        }
        cinema.setName(name);
        cinema.setAddress(address);
        cinemaDAO.update(cinema);
        session.setAttribute(FLASH_SUCCESS, "Đã lưu thông tin cụm rạp \"" + name + "\".");
        return new Target(cinemaId, null);
    }

    private Target handleDeleteCinema(HttpServletRequest request, HttpSession session) {
        String cinemaId = trimToNull(request.getParameter("cinemaId"));
        if (cinemaId == null) {
            throw new IllegalArgumentException("Thiếu mã cụm rạp cần xóa.");
        }
        cinemaDAO.delete(cinemaId);
        session.setAttribute(FLASH_SUCCESS, "Đã xóa cụm rạp cùng các phòng và sơ đồ ghế.");
        return new Target(null, null);
    }

    private Target handleSaveRoom(HttpServletRequest request, HttpSession session) {
        String roomId = trimToNull(request.getParameter("roomId"));
        String cinemaId = trimToNull(request.getParameter("cinemaId"));
        String name = required(request.getParameter("name"), "Tên phòng chiếu", MAX_ROOM_NAME);

        Cinema cinema = cinemaId != null ? cinemaDAO.findById(cinemaId) : null;
        if (cinema == null) {
            throw new IllegalArgumentException("Không tìm thấy cụm rạp của phòng chiếu.");
        }

        synchronized (CATALOG_WRITE_LOCK) {
            if (roomDAO.existsByNameInCinema(cinemaId, name, roomId)) {
                throw new IllegalArgumentException("Rạp \"" + cinema.getName() + "\" đã có phòng tên \"" + name + "\".");
            }
            return roomId == null
                    ? insertRoom(cinema, name, session)
                    : renameRoom(cinemaId, roomId, name, session);
        }
    }

    private Target insertRoom(Cinema cinema, String name, HttpSession session) {
        // Sức chứa = số ghế thật, được tính khi tạo sơ đồ ghế; phòng mới chưa có ghế nên bằng 0
        Room room = new Room();
        room.setName(name);
        room.setCapacity(0);
        room.setCinema(cinema);
        roomDAO.insert(room);
        session.setAttribute(FLASH_SUCCESS, "Đã thêm \"" + name + "\". Hãy tạo sơ đồ ghế trước khi xếp suất chiếu cho phòng.");
        return new Target(cinema.getCinemaID(), room.getRoomID());
    }

    // Sửa phòng: chỉ đổi tên, không đổi rạp và không ghi đè sức chứa
    private Target renameRoom(String cinemaId, String roomId, String name, HttpSession session) {
        Room room = roomDAO.findById(roomId);
        if (room == null || room.getCinema() == null || !cinemaId.equals(room.getCinema().getCinemaID())) {
            throw new IllegalArgumentException("Phòng chiếu cần sửa không thuộc cụm rạp đang chọn.");
        }
        room.setName(name);
        roomDAO.update(room);
        session.setAttribute(FLASH_SUCCESS, "Đã lưu thông tin phòng \"" + name + "\".");
        return new Target(cinemaId, roomId);
    }

    private Target handleDeleteRoom(HttpServletRequest request, HttpSession session) {
        String roomId = trimToNull(request.getParameter("roomId"));
        String cinemaId = trimToNull(request.getParameter("cinemaId"));
        if (roomId == null) {
            throw new IllegalArgumentException("Thiếu mã phòng chiếu cần xóa.");
        }
        roomDAO.delete(roomId);
        session.setAttribute(FLASH_SUCCESS, "Đã xóa phòng chiếu cùng sơ đồ ghế.");
        return new Target(cinemaId, null);
    }

    private Target handleGenerateSeats(HttpServletRequest request, HttpSession session) {
        String roomId = trimToNull(request.getParameter("roomId"));
        String cinemaId = trimToNull(request.getParameter("cinemaId"));
        if (roomId == null) {
            throw new IllegalArgumentException("Thiếu mã phòng chiếu cần tạo sơ đồ ghế.");
        }

        int rows = parseIntInRange(request.getParameter("rows"), MIN_ROWS, MAX_ROWS, "Số hàng ghế");
        int cols = parseIntInRange(request.getParameter("cols"), MIN_COLS, MAX_COLS, "Số ghế mỗi hàng");
        List<String> vipList = parseRowLetters(request.getParameter("vipRows"), rows, "VIP");
        List<String> coupleList = parseRowLetters(request.getParameter("coupleRows"), rows, "ghế đôi");
        for (String row : vipList) {
            if (coupleList.contains(row)) {
                throw new IllegalArgumentException("Hàng " + row + " không thể vừa là hàng VIP vừa là hàng ghế đôi.");
            }
        }

        int created = seatDAO.generateRoomSeats(roomId, rows, cols, vipList, coupleList);
        session.setAttribute(FLASH_SUCCESS, "Đã tạo sơ đồ " + created + " ghế (" + rows + " hàng × " + cols + " ghế).");
        return new Target(cinemaId, roomId);
    }

    private Target handleToggleSeat(HttpServletRequest request, HttpSession session) {
        String seatId = trimToNull(request.getParameter("seatId"));
        String cinemaId = trimToNull(request.getParameter("cinemaId"));
        String roomId = trimToNull(request.getParameter("roomId"));
        if (seatId == null) {
            throw new IllegalArgumentException("Thiếu mã ghế cần đổi trạng thái.");
        }
        Seat seat = seatDAO.toggleMaintenance(seatId, roomId);
        session.setAttribute(FLASH_SUCCESS, "Ghế " + seat.getSeatCode() + " đã chuyển sang "
                + ("MAINTENANCE".equals(seat.getStatus()) ? "bảo trì." : "hoạt động."));
        return new Target(cinemaId, roomId);
    }

    // ---------- tiện ích ----------

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String required(String raw, String label, int maxLength) {
        String value = trimToNull(raw);
        if (value == null) {
            throw new IllegalArgumentException(label + " không được để trống.");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(label + " tối đa " + maxLength + " ký tự.");
        }
        return value;
    }

    private static int parseIntInRange(String raw, int min, int max, String label) {
        int value;
        try {
            value = Integer.parseInt(raw != null ? raw.trim() : "");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " phải là số nguyên từ " + min + " đến " + max + ".");
        }
        if (value < min || value > max) {
            throw new IllegalArgumentException(label + " phải từ " + min + " đến " + max + ".");
        }
        return value;
    }

    // "E, f,G" -> [E, F, G]; mỗi hàng phải là một chữ cái nằm trong các hàng sẽ được tạo
    private static List<String> parseRowLetters(String raw, int rows, String label) {
        List<String> result = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return result;
        }
        char lastRow = (char) ('A' + rows - 1);
        for (String part : raw.split(",")) {
            String row = part.trim().toUpperCase();
            if (row.isEmpty()) continue;
            if (row.length() != 1 || row.charAt(0) < 'A' || row.charAt(0) > lastRow) {
                throw new IllegalArgumentException("Hàng " + label + " \"" + part.trim() + "\" không hợp lệ: phòng chỉ có các hàng A–" + lastRow + ".");
            }
            if (!result.contains(row)) {
                result.add(row);
            }
        }
        return result;
    }

    // Cấp một mã form mới cho lần hiển thị trang này
    private static String issueToken(HttpSession session) {
        String token = UUID.randomUUID().toString();
        synchronized (tokenMutex(session)) {
            Set<String> issued = tokenSet(session, TOKENS_ISSUED);
            issued.add(token);
            session.setAttribute(TOKENS_ISSUED, issued); // báo cho container là session đã đổi
        }
        return token;
    }

    // Mỗi mã chỉ dùng được một lần; mã đã dùng được ghi lại để nhận ra lần bấm lặp
    private static TokenCheck consumeToken(HttpSession session, String token) {
        if (token == null || token.isBlank()) {
            return TokenCheck.UNKNOWN;
        }
        synchronized (tokenMutex(session)) {
            Set<String> issued = tokenSet(session, TOKENS_ISSUED);
            Set<String> used = tokenSet(session, TOKENS_USED);
            TokenCheck result;
            if (issued.remove(token)) {
                used.add(token);
                result = TokenCheck.VALID;
            } else {
                result = used.contains(token) ? TokenCheck.DUPLICATE : TokenCheck.UNKNOWN;
            }
            session.setAttribute(TOKENS_ISSUED, issued);
            session.setAttribute(TOKENS_USED, used);
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    private static Set<String> tokenSet(HttpSession session, String key) {
        Object value = session.getAttribute(key);
        if (value instanceof Set<?>) {
            return (Set<String>) value;
        }
        // Giữ tối đa MAX_TOKENS mã gần nhất, mã cũ nhất bị bỏ trước
        Set<String> set = Collections.newSetFromMap(new LinkedHashMap<String, Boolean>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                return size() > MAX_TOKENS;
            }
        });
        session.setAttribute(key, set);
        return set;
    }

    private static Object tokenMutex(HttpSession session) {
        synchronized (MUTEX_INIT) {
            Object mutex = session.getAttribute(TOKEN_MUTEX);
            if (mutex == null) {
                mutex = new Object[0]; // Object[] là Serializable, giữ được khi session được lưu lại
                session.setAttribute(TOKEN_MUTEX, mutex);
            }
            return mutex;
        }
    }

    private static void moveFlash(HttpSession session, HttpServletRequest request, String sessionKey, String requestKey) {
        Object message = session.getAttribute(sessionKey);
        if (message != null) {
            request.setAttribute(requestKey, message);
            session.removeAttribute(sessionKey);
        }
    }

    private static void redirect(HttpServletRequest request, HttpServletResponse response, Target target) throws IOException {
        StringBuilder url = new StringBuilder(request.getContextPath()).append("/admin/cinemas");
        String sep = "?";
        if (target.cinemaId() != null) {
            url.append(sep).append("cinemaId=").append(URLEncoder.encode(target.cinemaId(), StandardCharsets.UTF_8));
            sep = "&";
        }
        if (target.roomId() != null) {
            url.append(sep).append("roomId=").append(URLEncoder.encode(target.roomId(), StandardCharsets.UTF_8));
        }
        response.sendRedirect(url.toString());
    }
}
