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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private final CinemaDAO cinemaDAO = new CinemaDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final SeatDAO seatDAO = new SeatDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 1. Lấy toàn bộ danh sách cụm rạp
        List<Cinema> cinemaList = cinemaDAO.findAll();

        // 2. Xác định rạp đang được chọn
        String cinemaIdParam = request.getParameter("cinemaId");
        Cinema selectedCinema = null;
        if (cinemaIdParam != null && !cinemaIdParam.trim().isEmpty()) {
            selectedCinema = cinemaDAO.findById(cinemaIdParam.trim());
        }
        if (selectedCinema == null && !cinemaList.isEmpty()) {
            selectedCinema = cinemaList.get(0);
        }

        // 3. Lấy danh sách phòng của rạp được chọn
        List<Room> roomList = new ArrayList<>();
        Room selectedRoom = null;
        if (selectedCinema != null) {
            roomList = roomDAO.findByCinemaId(selectedCinema.getCinemaID());

            // 4. Xác định phòng đang được chọn
            String roomIdParam = request.getParameter("roomId");
            if (roomIdParam != null && !roomIdParam.trim().isEmpty()) {
                selectedRoom = roomDAO.findById(roomIdParam.trim());
            }
            if (selectedRoom == null && !roomList.isEmpty()) {
                selectedRoom = roomList.get(0);
            }
        }

        // 5. Lấy danh sách ghế của phòng được chọn
        List<Seat> seatList = new ArrayList<>();
        if (selectedRoom != null) {
            seatList = seatDAO.findByRoomId(selectedRoom.getRoomID());
        }

        // 6. Thống kê nhanh KPIs
        int totalCinemas = cinemaList.size();
        int totalRooms = roomDAO.findAll().size();
        long totalSeats = seatDAO.countTotalSeats();
        long activeSeats = seatDAO.countActiveSeats();

        // Đẩy toàn bộ dữ liệu sang JSP
        request.setAttribute("cinemaList", cinemaList);
        request.setAttribute("selectedCinema", selectedCinema);
        request.setAttribute("selectedCinemaId", selectedCinema != null ? selectedCinema.getCinemaID() : "");

        request.setAttribute("roomList", roomList);
        request.setAttribute("selectedRoom", selectedRoom);
        request.setAttribute("selectedRoomId", selectedRoom != null ? selectedRoom.getRoomID() : "");

        request.setAttribute("seatList", seatList);
        request.setAttribute("totalCinemas", totalCinemas);
        request.setAttribute("totalRooms", totalRooms);
        request.setAttribute("totalSeats", totalSeats);
        request.setAttribute("activeSeats", activeSeats);

        // Thông báo nếu có
        String msg = request.getParameter("msg");
        if ("cinema_saved".equals(msg)) request.setAttribute("successMessage", "Đã lưu thông tin rạp chiếu thành công!");
        if ("cinema_deleted".equals(msg)) request.setAttribute("successMessage", "Đã xóa cụm rạp thành công!");
        if ("room_saved".equals(msg)) request.setAttribute("successMessage", "Đã lưu thông tin phòng chiếu thành công!");
        if ("room_deleted".equals(msg)) request.setAttribute("successMessage", "Đã xóa phòng chiếu thành công!");
        if ("seats_generated".equals(msg)) request.setAttribute("successMessage", "Đã tạo tự động ma trận sơ đồ ghế thành công!");

        request.getRequestDispatcher("/WEB-INF/views/admin/cinemas.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        try {
            if ("/admin/cinemas/cinema/save".equals(path)) {
                handleSaveCinema(request, response);
                return;
            } else if ("/admin/cinemas/cinema/delete".equals(path)) {
                handleDeleteCinema(request, response);
                return;
            } else if ("/admin/cinemas/room/save".equals(path)) {
                handleSaveRoom(request, response);
                return;
            } else if ("/admin/cinemas/room/delete".equals(path)) {
                handleDeleteRoom(request, response);
                return;
            } else if ("/admin/cinemas/seats/generate".equals(path)) {
                handleGenerateSeats(request, response);
                return;
            } else if ("/admin/cinemas/seats/toggle".equals(path)) {
                handleToggleSeat(request, response);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/admin/cinemas");
    }

    private void handleSaveCinema(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String cinemaId = request.getParameter("cinemaId");
        String name = request.getParameter("name");
        String address = request.getParameter("address");

        if (cinemaId == null || cinemaId.trim().isEmpty()) {
            Cinema cinema = new Cinema();
            cinema.setName(name);
            cinema.setAddress(address);
            cinemaDAO.insert(cinema);
            response.sendRedirect(request.getContextPath() + "/admin/cinemas?cinemaId=" + cinema.getCinemaID() + "&msg=cinema_saved");
        } else {
            Cinema cinema = cinemaDAO.findById(cinemaId.trim());
            if (cinema != null) {
                cinema.setName(name);
                cinema.setAddress(address);
                cinemaDAO.update(cinema);
            }
            response.sendRedirect(request.getContextPath() + "/admin/cinemas?cinemaId=" + cinemaId + "&msg=cinema_saved");
        }
    }

    private void handleDeleteCinema(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String cinemaId = request.getParameter("cinemaId");
        if (cinemaId != null && !cinemaId.trim().isEmpty()) {
            cinemaDAO.delete(cinemaId.trim());
        }
        response.sendRedirect(request.getContextPath() + "/admin/cinemas?msg=cinema_deleted");
    }

    private void handleSaveRoom(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String roomId = request.getParameter("roomId");
        String cinemaId = request.getParameter("cinemaId");
        String name = request.getParameter("name");
        int capacity = 80;
        try {
            capacity = Integer.parseInt(request.getParameter("capacity"));
        } catch (Exception ignored) {}

        Cinema cinema = cinemaDAO.findById(cinemaId);
        if (cinema == null) {
            response.sendRedirect(request.getContextPath() + "/admin/cinemas");
            return;
        }

        if (roomId == null || roomId.trim().isEmpty()) {
            Room room = new Room();
            room.setName(name);
            room.setCapacity(capacity);
            room.setCinema(cinema);
            roomDAO.insert(room);
            response.sendRedirect(request.getContextPath() + "/admin/cinemas?cinemaId=" + cinemaId + "&roomId=" + room.getRoomID() + "&msg=room_saved");
        } else {
            Room room = roomDAO.findById(roomId.trim());
            if (room != null) {
                room.setName(name);
                room.setCapacity(capacity);
                room.setCinema(cinema);
                roomDAO.update(room);
            }
            response.sendRedirect(request.getContextPath() + "/admin/cinemas?cinemaId=" + cinemaId + "&roomId=" + roomId + "&msg=room_saved");
        }
    }

    private void handleDeleteRoom(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String roomId = request.getParameter("roomId");
        String cinemaId = request.getParameter("cinemaId");
        if (roomId != null && !roomId.trim().isEmpty()) {
            roomDAO.delete(roomId.trim());
        }
        response.sendRedirect(request.getContextPath() + "/admin/cinemas?cinemaId=" + (cinemaId != null ? cinemaId : "") + "&msg=room_deleted");
    }

    private void handleGenerateSeats(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String roomId = request.getParameter("roomId");
        String cinemaId = request.getParameter("cinemaId");
        int rows = 8;
        int cols = 10;
        try {
            rows = Integer.parseInt(request.getParameter("rows"));
            cols = Integer.parseInt(request.getParameter("cols"));
        } catch (Exception ignored) {}

        String vipRowsStr = request.getParameter("vipRows"); // VD: "E,F,G"
        String coupleRowsStr = request.getParameter("coupleRows"); // VD: "H"

        List<String> vipList = new ArrayList<>();
        if (vipRowsStr != null && !vipRowsStr.trim().isEmpty()) {
            vipList = Arrays.stream(vipRowsStr.split(","))
                            .map(String::trim)
                            .map(String::toUpperCase)
                            .toList();
        }

        List<String> coupleList = new ArrayList<>();
        if (coupleRowsStr != null && !coupleRowsStr.trim().isEmpty()) {
            coupleList = Arrays.stream(coupleRowsStr.split(","))
                              .map(String::trim)
                              .map(String::toUpperCase)
                              .toList();
        }

        seatDAO.generateRoomSeats(roomId, rows, cols, vipList, coupleList);

        response.sendRedirect(request.getContextPath() + "/admin/cinemas?cinemaId=" + cinemaId + "&roomId=" + roomId + "&msg=seats_generated");
    }

    private void handleToggleSeat(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String seatId = request.getParameter("seatId");
        String cinemaId = request.getParameter("cinemaId");
        String roomId = request.getParameter("roomId");

        if (seatId != null) {
            Seat seat = seatDAO.findById(seatId);
            if (seat != null) {
                seat.setStatus("ACTIVE".equals(seat.getStatus()) ? "MAINTENANCE" : "ACTIVE");
                seatDAO.update(seat);
            }
        }

        response.sendRedirect(request.getContextPath() + "/admin/cinemas?cinemaId=" + (cinemaId != null ? cinemaId : "") + "&roomId=" + (roomId != null ? roomId : ""));
    }
}
