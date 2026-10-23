package com.cinema.dao.booking;

import com.cinema.dao.admin.ConnectionPool;
import com.cinema.entity.Combo;
import com.cinema.entity.Payment;
import com.cinema.entity.Seat;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class BookingDAO {

    /**
     * Lấy kết nối database dùng chung ConnectionPool (đọc cấu hình từ META-INF/persistence.xml).
     */
    protected Connection getConnection() throws SQLException {
        return ConnectionPool.getInstance().getConnection();
    }

    /**
     * Tự động giải phóng các lượt giữ ghế (PENDING) đã quá 10 phút.
     */
    public void releaseExpiredBookings() {
        String updateBookingsSql =
                "UPDATE bookings SET bookingstatus = 'CANCELLED', status = 'CANCELLED', cancelreason = 'Quá hạn giữ chỗ 10 phút' " +
                "WHERE bookingstatus = 'PENDING' " +
                "  AND (COALESCE(createdat, created_at) + INTERVAL '10 minute') < CURRENT_TIMESTAMP";

        String updateTicketsSql =
                "UPDATE tickets SET ticketstatus = 'CANCELLED', status = 'CANCELLED' " +
                "FROM bookings b " +
                "WHERE (tickets.bookingid = b.bookingid OR tickets.booking_id = b.booking_id) " +
                "  AND b.bookingstatus = 'CANCELLED' " +
                "  AND (tickets.ticketstatus = 'HOLD' OR tickets.ticketstatus = 'AVAILABLE')";

        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            stmt.executeUpdate(updateBookingsSql);
            stmt.executeUpdate(updateTicketsSql);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            System.err.println("[BookingDAO] Lỗi releaseExpiredBookings: " + e.getMessage());
        } finally {
            close(stmt, conn);
        }
    }

    /**
     * Lấy toàn bộ sơ đồ ghế theo Suất chiếu và đánh dấu trạng thái khả dụng isAvailable.
     * Ghế không khả dụng nếu đã có vé thuộc đơn PAID hoặc PENDING còn trong 10 phút.
     */
    public List<Seat> getSeatsByShowtime(String showTimeID) {
        releaseExpiredBookings();

        List<Seat> seats = new ArrayList<>();
        Set<String> occupiedSeatIDs = new HashSet<>();

        String occupiedSql =
                "SELECT t.seatid FROM tickets t " +
                "JOIN bookings b ON (t.bookingid = b.bookingid OR t.booking_id = b.booking_id) " +
                "WHERE (b.showtimeid = ? OR b.showtime_id = ?) " +
                "  AND (t.ticketstatus != 'CANCELLED' AND t.status != 'CANCELLED') " +
                "  AND ((b.bookingstatus = 'PAID' OR b.status = 'PAID') " +
                "       OR ((b.bookingstatus = 'PENDING' OR b.status = 'PENDING') " +
                "           AND (COALESCE(b.createdat, b.created_at) + INTERVAL '10 minute') > CURRENT_TIMESTAMP))";

        String seatsSql =
                "SELECT s.seatid, s.room_id, s.seatrow, s.seatcol, s.seattype, s.seatcode, s.status, " +
                "       COALESCE(st.price, 80000) AS base_price " +
                "FROM seats s " +
                "JOIN showtimes st ON s.room_id = st.room_id " +
                "WHERE st.showtimeid = ? " +
                "ORDER BY s.seatrow, s.seatcol, s.seatid";

        Connection conn = null;
        PreparedStatement psOccupied = null;
        PreparedStatement psSeats = null;
        ResultSet rsOccupied = null;
        ResultSet rsSeats = null;

        try {
            conn = getConnection();

            // 1. Lấy danh sách ID ghế đã bị chiếm chỗ
            psOccupied = conn.prepareStatement(occupiedSql);
            psOccupied.setString(1, showTimeID);
            psOccupied.setString(2, showTimeID);
            rsOccupied = psOccupied.executeQuery();
            while (rsOccupied.next()) {
                String sid = rsOccupied.getString("seatid");
                if (sid != null) {
                    occupiedSeatIDs.add(sid);
                }
            }

            // 2. Lấy toàn bộ danh sách ghế trong phòng chiếu
            psSeats = conn.prepareStatement(seatsSql);
            psSeats.setString(1, showTimeID);
            rsSeats = psSeats.executeQuery();
            while (rsSeats.next()) {
                Seat seat = new Seat();
                seat.setSeatID(rsSeats.getString("seatid"));
                seat.setRoomID(rsSeats.getString("room_id"));
                seat.setRow(rsSeats.getString("seatrow"));
                seat.setColumn(String.valueOf(rsSeats.getInt("seatcol")));
                String sType = rsSeats.getString("seattype");
                seat.setType(sType != null ? sType : "STANDARD");

                // Phụ thu loại ghế
                BigDecimal extra = BigDecimal.ZERO;
                if ("VIP".equalsIgnoreCase(sType)) {
                    extra = BigDecimal.valueOf(20000);
                } else if ("COUPLE".equalsIgnoreCase(sType) || "SWEETBOX".equalsIgnoreCase(sType)) {
                    extra = BigDecimal.valueOf(50000);
                }
                seat.setExtraPrice(extra);

                // Khả dụng khi chưa bị chiếm chỗ và trạng thái ghế ACTIVE
                String sStatus = rsSeats.getString("status");
                boolean isActive = sStatus == null || "ACTIVE".equalsIgnoreCase(sStatus);
                seat.setAvailable(isActive && !occupiedSeatIDs.contains(seat.getSeatID()));
                seats.add(seat);
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] Lỗi getSeatsByShowtime: " + e.getMessage());
        } finally {
            close(rsOccupied, psOccupied, rsSeats, psSeats, conn);
        }

        return seats;
    }

    /**
     * Giữ ghế trong Transaction: kiểm tra trùng lặp với SELECT FOR UPDATE chống race condition,
     * tạo đơn Booking PENDING và vé Ticket AVAILABLE.
     */
    public boolean holdSeats(String bookingID, String userID, String showTimeID, List<String> seatIDs, int holdMinutes,
                             String customerName, String email, String phone) {
        if (seatIDs == null || seatIDs.isEmpty()) {
            return false;
        }

        releaseExpiredBookings();

        String lockShowtimeSql =
                "SELECT showtimeid FROM showtimes WHERE showtimeid = ? FOR UPDATE";

        String checkConflictSql =
                "SELECT t.seatid FROM tickets t " +
                "JOIN bookings b ON (t.bookingid = b.bookingid OR t.booking_id = b.booking_id) " +
                "WHERE (b.showtimeid = ? OR b.showtime_id = ?) " +
                "  AND (t.ticketstatus != 'CANCELLED' AND t.status != 'CANCELLED') " +
                "  AND ((b.bookingstatus = 'PAID' OR b.status = 'PAID') " +
                "       OR ((b.bookingstatus = 'PENDING' OR b.status = 'PENDING') " +
                "           AND (COALESCE(b.createdat, b.created_at) + INTERVAL '10 minute') > CURRENT_TIMESTAMP))";

        String getShowtimeInfoSql =
                "SELECT st.showtimeid, st.starttime, COALESCE(st.price, 80000) AS price, " +
                "       m.title AS movietitle, m.posterurl, " +
                "       r.name AS roomname, c.name AS cinemaname, c.address AS cinemaaddress " +
                "FROM showtimes st " +
                "LEFT JOIN movies m ON st.movie_id = m.movieid " +
                "LEFT JOIN rooms r ON st.room_id = r.roomid " +
                "LEFT JOIN cinema c ON r.cinema_id = c.cinemaid " +
                "WHERE st.showtimeid = ?";

        String getSeatInfoSql =
                "SELECT s.seatid, s.seatrow, s.seatcol, s.seattype " +
                "FROM seats s " +
                "JOIN showtimes st ON s.room_id = st.room_id " +
                "WHERE s.seatid = ? AND st.showtimeid = ? " +
                "  AND (s.status IS NULL OR s.status = 'ACTIVE' OR s.status = 'AVAILABLE')";

        String insertBookingSql =
                "INSERT INTO bookings (" +
                "    bookingid, booking_id, ordercode, order_code, userid, user_id, showtimeid, showtime_id, " +
                "    bookingstatus, status, createdat, created_at, " +
                "    movietitle, posterurl, cinemaname, cinemaaddress, roomname, movieformat, showstart, " +
                "    seats, ticketprice, comboprice, combos, totalprice, pointsused, pointsearned, pointsreversed, points_reversed, " +
                "    customername, customer_name, email, phone) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " +
                "    ?, ?, ?, ?, ?, '2D Digital', ?, ?, ?, 0, '', ?, 0, 0, false, false, ?, ?, ?, ?)";

        String insertTicketSql =
                "INSERT INTO tickets (" +
                "    ticketid, ticket_id, bookingid, booking_id, seatid, seat_id, seatlabel, seat_label, price, ticketstatus, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'HOLD', 'HOLD')";

        Connection conn = null;
        PreparedStatement psLock = null;
        PreparedStatement psCheck = null;
        PreparedStatement psInfo = null;
        PreparedStatement psSeat = null;
        PreparedStatement psBooking = null;
        PreparedStatement psTicket = null;
        ResultSet rsLock = null;
        ResultSet rsCheck = null;
        ResultSet rsInfo = null;
        ResultSet rsSeat = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // 0. Khóa hàng showtime chống race condition giữ ghế đồng thời
            psLock = conn.prepareStatement(lockShowtimeSql);
            psLock.setString(1, showTimeID);
            rsLock = psLock.executeQuery();
            if (!rsLock.next()) {
                conn.rollback();
                return false; // Suất chiếu không tồn tại
            }

            // 1. Kiểm tra ghế đã bị giữ/đặt chưa
            psCheck = conn.prepareStatement(checkConflictSql);
            psCheck.setString(1, showTimeID);
            psCheck.setString(2, showTimeID);
            rsCheck = psCheck.executeQuery();
            Set<String> takenSeats = new HashSet<>();
            while (rsCheck.next()) {
                String sid = rsCheck.getString("seatid");
                if (sid != null) {
                    takenSeats.add(sid);
                }
            }

            for (String seatId : seatIDs) {
                if (takenSeats.contains(seatId)) {
                    conn.rollback();
                    return false; // Ghế đã bị giữ, hủy transaction
                }
            }

            // 2. Lấy thông tin Suất chiếu, Phim, Rạp
            psInfo = conn.prepareStatement(getShowtimeInfoSql);
            psInfo.setString(1, showTimeID);
            rsInfo = psInfo.executeQuery();
            String movieTitle = "Phim Chiếu Rạp";
            String posterUrl = "";
            String roomName = "Phòng 1";
            String cinemaName = "Cinetik Cinema";
            String cinemaAddress = "";
            double basePrice = 80000.0;
            Timestamp showStart = new Timestamp(System.currentTimeMillis() + 86400000L);

            if (rsInfo.next()) {
                if (rsInfo.getString("movietitle") != null) movieTitle = rsInfo.getString("movietitle");
                if (rsInfo.getString("posterurl") != null) posterUrl = rsInfo.getString("posterurl");
                if (rsInfo.getString("roomname") != null) roomName = rsInfo.getString("roomname");
                if (rsInfo.getString("cinemaname") != null) cinemaName = rsInfo.getString("cinemaname");
                if (rsInfo.getString("cinemaaddress") != null) cinemaAddress = rsInfo.getString("cinemaaddress");
                basePrice = rsInfo.getDouble("price");
                if (basePrice <= 0) basePrice = 80000.0;
                Timestamp stTime = rsInfo.getTimestamp("starttime");
                if (stTime != null) showStart = stTime;
            }

            // 3. Tính tiền ghế và gom nhãn ghế (xác thực ghế thuộc phòng của suất chiếu và đang ACTIVE)
            double totalSeatPrice = 0.0;
            List<String> seatLabels = new ArrayList<>();
            Map<String, String> seatIdToLabel = new HashMap<>();
            Map<String, Double> seatIdToPrice = new HashMap<>();

            psSeat = conn.prepareStatement(getSeatInfoSql);
            for (String sid : seatIDs) {
                psSeat.setString(1, sid);
                psSeat.setString(2, showTimeID);
                rsSeat = psSeat.executeQuery();
                if (!rsSeat.next()) {
                    conn.rollback();
                    return false; // Ghế không hợp lệ, không thuộc phòng này hoặc đang bảo trì
                }
                String row = rsSeat.getString("seatrow");
                int col = rsSeat.getInt("seatcol");
                String sType = rsSeat.getString("seattype");
                String label = (row != null ? row : "") + col;
                double seatPrice = basePrice;
                if ("VIP".equalsIgnoreCase(sType)) {
                    seatPrice += 20000.0;
                } else if ("COUPLE".equalsIgnoreCase(sType) || "SWEETBOX".equalsIgnoreCase(sType)) {
                    seatPrice += 50000.0;
                }
                rsSeat.close();
                seatLabels.add(label);
                seatIdToLabel.put(sid, label);
                seatIdToPrice.put(sid, seatPrice);
                totalSeatPrice += seatPrice;
            }

            // 4. Tạo mã đơn hàng orderCode
            String orderCode = "CNK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            String seatsStr = String.join(", ", seatLabels);

            // Tự động tìm thông tin người dùng nếu chưa có
            String finalCustomerName = customerName;
            String finalEmail = email;
            String finalPhone = phone;
            if (userID != null && !userID.isBlank() && (finalCustomerName == null || finalEmail == null || finalPhone == null)) {
                try (PreparedStatement psUser = conn.prepareStatement("SELECT full_name, email, phone FROM users WHERE user_id = ?")) {
                    psUser.setString(1, userID);
                    try (ResultSet rsUser = psUser.executeQuery()) {
                        if (rsUser.next()) {
                            if (finalCustomerName == null) finalCustomerName = rsUser.getString("full_name");
                            if (finalEmail == null) finalEmail = rsUser.getString("email");
                            if (finalPhone == null) finalPhone = rsUser.getString("phone");
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            if (finalCustomerName == null || finalCustomerName.isBlank()) finalCustomerName = "Khách Hàng";
            if (finalEmail == null) finalEmail = "";
            if (finalPhone == null) finalPhone = "";

            // 5. Thêm bản ghi vào bảng bookings
            psBooking = conn.prepareStatement(insertBookingSql);
            psBooking.setString(1, bookingID);
            psBooking.setString(2, bookingID);
            psBooking.setString(3, orderCode);
            psBooking.setString(4, orderCode);
            psBooking.setString(5, userID);
            psBooking.setString(6, userID);
            psBooking.setString(7, showTimeID);
            psBooking.setString(8, showTimeID);
            psBooking.setString(9, movieTitle);
            psBooking.setString(10, posterUrl);
            psBooking.setString(11, cinemaName);
            psBooking.setString(12, cinemaAddress);
            psBooking.setString(13, roomName);
            psBooking.setTimestamp(14, showStart);
            psBooking.setString(15, seatsStr);
            psBooking.setDouble(16, totalSeatPrice);
            psBooking.setDouble(17, totalSeatPrice);
            psBooking.setString(18, finalCustomerName);
            psBooking.setString(19, finalCustomerName);
            psBooking.setString(20, finalEmail);
            psBooking.setString(21, finalPhone);
            psBooking.executeUpdate();

            // 6. Thêm bản ghi vào bảng tickets
            psTicket = conn.prepareStatement(insertTicketSql);
            for (String sid : seatIDs) {
                String ticketID = "TK_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                String label = seatIdToLabel.getOrDefault(sid, sid);
                double price = seatIdToPrice.getOrDefault(sid, basePrice);

                psTicket.setString(1, ticketID);
                psTicket.setString(2, ticketID);
                psTicket.setString(3, bookingID);
                psTicket.setString(4, bookingID);
                psTicket.setString(5, sid);
                psTicket.setString(6, sid);
                psTicket.setString(7, label);
                psTicket.setString(8, label);
                psTicket.setDouble(9, price);
                psTicket.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            System.err.println("[BookingDAO] Lỗi Transaction holdSeats: " + e.getMessage());
            return false;
        } finally {
            close(rsLock, rsCheck, psCheck, rsInfo, psInfo, rsSeat, psSeat, psLock, psBooking, psTicket, conn);
        }
    }

    /**
     * Overload holdSeats với String userID không có thông tin liên hệ
     */
    public boolean holdSeats(String bookingID, String userID, String showTimeID, List<String> seatIDs, int holdMinutes) {
        return holdSeats(bookingID, userID, showTimeID, seatIDs, holdMinutes, null, null, null);
    }

    /**
     * Fallback overload cho userID dạng int
     */
    public boolean holdSeats(String bookingID, int userID, String showTimeID, List<String> seatIDs, int holdMinutes) {
        return holdSeats(bookingID, String.valueOf(userID), showTimeID, seatIDs, holdMinutes, null, null, null);
    }

    /**
     * Lấy danh sách tất cả các Combo đang mở bán (status = 'ACTIVE') từ bảng combos.
     */
    public List<Combo> getAllCombos() {
        List<Combo> combos = new ArrayList<>();
        String sql = "SELECT comboid, name, description, price, category, imageurl, status " +
                     "FROM combos WHERE status = 'ACTIVE' ORDER BY category, price";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Combo c = new Combo();
                c.setComboID(rs.getString("comboid"));
                c.setName(rs.getString("name"));
                c.setDescription(rs.getString("description"));
                c.setPrice(rs.getBigDecimal("price"));
                c.setCategory(rs.getString("category"));
                c.setImageURL(rs.getString("imageurl"));
                c.setStatus(rs.getString("status"));
                combos.add(c);
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] Lỗi getAllCombos: " + e.getMessage());
        } finally {
            close(rs, stmt, conn);
        }
        return combos;
    }

    /**
     * Lưu danh sách bắp nước khách chọn vào đơn đặt vé (cập nhật comboprice, combos, totalprice).
     */
    public boolean saveBookingCombos(String bookingID, Map<String, Integer> comboQuantities) {
        if (bookingID == null || comboQuantities == null) {
            return false;
        }

        Connection conn = null;
        PreparedStatement psCheck = null;
        PreparedStatement psCombo = null;
        PreparedStatement psUpdate = null;
        ResultSet rsCheck = null;
        ResultSet rs = null;

        try {
            conn = getConnection();

            // 1. Kiểm tra đơn hàng phải còn PENDING (không cho sửa đơn đã thanh toán hoặc đã hủy)
            String checkBookingSql = "SELECT bookingstatus FROM bookings WHERE (bookingid = ? OR booking_id = ?)";
            psCheck = conn.prepareStatement(checkBookingSql);
            psCheck.setString(1, bookingID.trim());
            psCheck.setString(2, bookingID.trim());
            rsCheck = psCheck.executeQuery();
            if (!rsCheck.next() || !"PENDING".equalsIgnoreCase(rsCheck.getString("bookingstatus"))) {
                return false;
            }

            double comboTotal = 0.0;
            List<String> comboDescList = new ArrayList<>();

            if (!comboQuantities.isEmpty()) {
                // 2. Chỉ chấp nhận các combo đang mở bán (ACTIVE)
                String queryComboSql = "SELECT comboid, name, price FROM combos WHERE comboid = ? AND (status = 'ACTIVE' OR status IS NULL)";
                psCombo = conn.prepareStatement(queryComboSql);

                for (Map.Entry<String, Integer> entry : comboQuantities.entrySet()) {
                    String comboId = entry.getKey();
                    int qty = entry.getValue();
                    if (qty <= 0) continue;

                    psCombo.setString(1, comboId);
                    rs = psCombo.executeQuery();
                    if (rs.next()) {
                        String name = rs.getString("name");
                        double price = rs.getDouble("price");
                        comboTotal += price * qty;
                        comboDescList.add(name + " × " + qty);
                    }
                    rs.close();
                }
            }

            String comboStr = String.join(", ", comboDescList);

            String updateSql = "UPDATE bookings SET " +
                    "combos = ?, " +
                    "comboprice = ?, combo_price = ?, " +
                    "totalprice = (COALESCE(ticketprice, ticket_price, 0) + ?), " +
                    "total_price = (COALESCE(ticketprice, ticket_price, 0) + ?) " +
                    "WHERE (bookingid = ? OR booking_id = ?) AND bookingstatus = 'PENDING'";

            psUpdate = conn.prepareStatement(updateSql);
            psUpdate.setString(1, comboStr);
            psUpdate.setDouble(2, comboTotal);
            psUpdate.setDouble(3, comboTotal);
            psUpdate.setDouble(4, comboTotal);
            psUpdate.setDouble(5, comboTotal);
            psUpdate.setString(6, bookingID.trim());
            psUpdate.setString(7, bookingID.trim());
            int affected = psUpdate.executeUpdate();

            return affected > 0;
        } catch (SQLException e) {
            System.err.println("[BookingDAO] Lỗi saveBookingCombos: " + e.getMessage());
            return false;
        } finally {
            close(rsCheck, psCheck, rs, psCombo, psUpdate, conn);
        }
    }

    /**
     * Cập nhật thông tin giảm trừ điểm và tổng tiền cuối cùng cho đơn hàng đang chờ thanh toán (PENDING).
     */
    public boolean updatePendingBooking(String bookingID, int pointsUsed, BigDecimal finalAmount,
                                        String customerName, String email, String phone) {
        if (bookingID == null || bookingID.trim().isEmpty()) {
            return false;
        }

        String sql = "UPDATE bookings SET " +
                "pointsused = ?, points_used = ?, " +
                "totalprice = ?, total_price = ?, " +
                "customername = COALESCE(?, customername), customer_name = COALESCE(?, customer_name), " +
                "email = COALESCE(?, email), phone = COALESCE(?, phone) " +
                "WHERE (bookingid = ? OR booking_id = ?) AND (bookingstatus = 'PENDING' OR status = 'PENDING')";

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, pointsUsed);
            ps.setInt(2, pointsUsed);
            double amt = finalAmount != null ? finalAmount.doubleValue() : 0.0;
            ps.setDouble(3, amt);
            ps.setDouble(4, amt);
            ps.setString(5, customerName != null && !customerName.isBlank() ? customerName : null);
            ps.setString(6, customerName != null && !customerName.isBlank() ? customerName : null);
            ps.setString(7, email != null && !email.isBlank() ? email : null);
            ps.setString(8, phone != null && !phone.isBlank() ? phone : null);
            ps.setString(9, bookingID.trim());
            ps.setString(10, bookingID.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BookingDAO] Lỗi updatePendingBooking: " + e.getMessage());
            return false;
        } finally {
            close(ps, conn);
        }
    }

    /**
     * Lấy tổng tiền tạm tính gốc (tiền vé + tiền combo) từ đơn hàng.
     */
    public BigDecimal calculateRawTotal(String bookingID) {
        if (bookingID == null || bookingID.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Tính tổng gốc thực tế = tiền vé + tiền combo, không đọc totalprice để tránh trừ điểm 2 lần
        String sql = "SELECT (COALESCE(ticketprice, ticket_price, 0) + COALESCE(comboprice, combo_price, 0)) AS total " +
                     "FROM bookings WHERE bookingid = ? OR booking_id = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, bookingID.trim());
            ps.setString(2, bookingID.trim());
            rs = ps.executeQuery();
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total");
                return total != null ? total : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] Lỗi calculateRawTotal: " + e.getMessage());
        } finally {
            close(rs, ps, conn);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Xác nhận thanh toán thành công: chuyển Booking sang PAID, cập nhật điểm và tiền thanh toán thực tế.
     */
    public boolean completePayment(String bookingID, Payment payment, int pointsUsed, int pointsEarned, BigDecimal finalAmount,
                                   String customerName, String email, String phone) {
        if (bookingID == null) {
            return false;
        }

        String updateBookingSql =
                "UPDATE bookings SET " +
                "bookingstatus = 'PAID', status = 'PAID', " +
                "pointsused = ?, points_used = ?, " +
                "pointsearned = ?, points_earned = ?, " +
                "totalprice = ?, total_price = ?, " +
                "customername = COALESCE(?, customername), customer_name = COALESCE(?, customer_name), " +
                "email = COALESCE(?, email), phone = COALESCE(?, phone) " +
                "WHERE (bookingid = ? OR booking_id = ?) " +
                "  AND (UPPER(COALESCE(bookingstatus, status, '')) = 'PENDING') " +
                "  AND (COALESCE(createdat, created_at, CURRENT_TIMESTAMP) + INTERVAL '15 minute') >= CURRENT_TIMESTAMP";

        String updateTicketSql =
                "UPDATE tickets SET ticketstatus = 'BOOKED', status = 'BOOKED' " +
                "WHERE bookingid = ? OR booking_id = ?";

        Connection conn = null;
        PreparedStatement psBooking = null;
        PreparedStatement psTicket = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            psBooking = conn.prepareStatement(updateBookingSql);
            psBooking.setInt(1, pointsUsed);
            psBooking.setInt(2, pointsUsed);
            psBooking.setInt(3, pointsEarned);
            psBooking.setInt(4, pointsEarned);
            double amt = (finalAmount != null) ? finalAmount.doubleValue() : 0.0;
            psBooking.setDouble(5, amt);
            psBooking.setDouble(6, amt);
            psBooking.setString(7, customerName != null && !customerName.isBlank() ? customerName : null);
            psBooking.setString(8, customerName != null && !customerName.isBlank() ? customerName : null);
            psBooking.setString(9, email != null && !email.isBlank() ? email : null);
            psBooking.setString(10, phone != null && !phone.isBlank() ? phone : null);
            psBooking.setString(11, bookingID.trim());
            psBooking.setString(12, bookingID.trim());
            int updatedRows = psBooking.executeUpdate();

            if (updatedRows == 0) {
                conn.rollback();
                System.err.println("[BookingDAO] completePayment: Đơn hàng " + bookingID + " đã quá hạn hoặc không ở trạng thái PENDING.");
                return false;
            }

            psTicket = conn.prepareStatement(updateTicketSql);
            psTicket.setString(1, bookingID.trim());
            psTicket.setString(2, bookingID.trim());
            psTicket.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            System.err.println("[BookingDAO] Lỗi completePayment: " + e.getMessage());
            return false;
        } finally {
            close(psBooking, psTicket, conn);
        }
    }

    public boolean completePayment(String bookingID, Payment payment) {
        return completePayment(bookingID, payment, 0, 0, payment != null ? payment.getAmount() : BigDecimal.ZERO, null, null, null);
    }

    /**
     * Đọc thông tin chi tiết một đơn đặt vé để kiểm tra tính hợp lệ trước khi xác nhận thanh toán.
     */
    public Map<String, Object> getBookingDetails(String bookingID) {
        if (bookingID == null) return null;
        String sql = "SELECT bookingid, ordercode, userid, showtimeid, bookingstatus, status, " +
                     "       movietitle, posterurl, cinemaname, cinemaaddress, roomname, movieformat, showstart, " +
                     "       seats, combos, " +
                     "       COALESCE(ticketprice, ticket_price, 0) AS ticketprice, " +
                     "       COALESCE(comboprice, combo_price, 0) AS comboprice, " +
                     "       COALESCE(totalprice, total_price, 0) AS totalprice, " +
                     "       COALESCE(pointsused, points_used, 0) AS pointsused, " +
                     "       COALESCE(pointsearned, points_earned, 0) AS pointsearned, " +
                     "       COALESCE(createdat, created_at) AS createdat, " +
                     "       customername, email, phone " +
                     "FROM bookings WHERE bookingid = ? OR booking_id = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, bookingID.trim());
            ps.setString(2, bookingID.trim());
            rs = ps.executeQuery();
            if (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("bookingId", rs.getString("bookingid"));
                map.put("orderCode", rs.getString("ordercode"));
                map.put("userId", rs.getString("userid"));
                map.put("showtimeId", rs.getString("showtimeid"));
                map.put("status", rs.getString("bookingstatus"));
                map.put("bookingStatus", rs.getString("bookingstatus"));
                map.put("movieTitle", rs.getString("movietitle"));
                map.put("posterUrl", rs.getString("posterurl"));
                map.put("cinemaName", rs.getString("cinemaname"));
                map.put("cinemaAddress", rs.getString("cinemaaddress"));
                map.put("roomName", rs.getString("roomname"));
                map.put("movieFormat", rs.getString("movieformat"));
                map.put("showStart", rs.getTimestamp("showstart"));
                map.put("seats", rs.getString("seats"));
                map.put("combos", rs.getString("combos"));
                map.put("ticketPrice", rs.getDouble("ticketprice"));
                map.put("comboPrice", rs.getDouble("comboprice"));
                map.put("totalPrice", rs.getDouble("totalprice"));
                map.put("pointsUsed", rs.getInt("pointsused"));
                map.put("pointsEarned", rs.getInt("pointsearned"));
                map.put("createdAt", rs.getTimestamp("createdat"));
                map.put("customerName", rs.getString("customername"));
                map.put("email", rs.getString("email"));
                map.put("phone", rs.getString("phone"));
                return map;
            }
        } catch (SQLException e) {
            System.err.println("[BookingDAO] Lỗi getBookingDetails: " + e.getMessage());
        } finally {
            close(rs, ps, conn);
        }
        return null;
    }

    public boolean confirmPayment(String bookingID, String paymentID) {
        return completePayment(bookingID, new Payment(paymentID, "VNPAY", BigDecimal.ZERO, null));
    }

    private static void close(AutoCloseable... closeables) {
        if (closeables == null) return;
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
