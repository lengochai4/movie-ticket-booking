package com.cinema.dao.admin;

import com.cinema.dto.MovieDTO;
import com.cinema.dto.RoomDTO;
import com.cinema.dto.ShowtimeDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Suất chiếu và phòng chiếu cho trang Admin Suất chiếu của Vinh (JDBC: ConnectionPool + PreparedStatement).
 */
public class AdminShowtimeDAO {

    // Suất chiếu kèm phim, phòng, rạp
    private static final String SELECT_SHOWTIME =
            "SELECT s.showtimeid, s.starttime, s.endtime, s.price, "
            + "m.movieid, m.title, m.duration, m.agerating, m.posterurl, "
            + "r.roomid, r.name AS room_name, r.capacity, c.cinemaid, c.name AS cinema_name "
            + "FROM showtimes s "
            + "JOIN movies m ON m.movieid = s.movie_id "
            + "JOIN rooms r ON r.roomid = s.room_id "
            + "JOIN cinema c ON c.cinemaid = r.cinema_id ";

    private static final String SELECT_ROOM =
            "SELECT r.roomid, r.name AS room_name, r.capacity, c.cinemaid, c.name AS cinema_name "
            + "FROM rooms r JOIN cinema c ON c.cinemaid = r.cinema_id ";

    // Suất chiếu bắt đầu trong ngày
    public List<ShowtimeDTO> findByDate(LocalDate date) {
        return queryShowtimes(SELECT_SHOWTIME + "WHERE s.starttime >= ? AND s.starttime < ? ORDER BY s.starttime",
                Timestamp.valueOf(date.atStartOfDay()), Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
    }

    public ShowtimeDTO findById(String showtimeId) {
        List<ShowtimeDTO> list = queryShowtimes(SELECT_SHOWTIME + "WHERE s.showtimeid = ?", showtimeId);
        return list.isEmpty() ? null : list.get(0);
    }

    // Các suất chiếu sắp tới của một phim (dùng khi đổi thời lượng phim)
    public List<ShowtimeDTO> findUpcomingByMovie(String movieId, LocalDateTime from) {
        return queryShowtimes(SELECT_SHOWTIME + "WHERE s.movie_id = ? AND s.starttime >= ? ORDER BY s.starttime",
                movieId, Timestamp.valueOf(from));
    }

    /**
     * Các suất chiếu cùng phòng chồng giờ với khoảng [start, end).
     * excludeId: bỏ qua chính suất chiếu đang sửa (null khi thêm mới).
     * Suất nối tiếp (kết thúc đúng lúc suất kia bắt đầu) không bị tính là trùng.
     */
    public List<ShowtimeDTO> findOverlapping(String roomId, LocalDateTime start, LocalDateTime end, String excludeId) {
        String sql = SELECT_SHOWTIME + "WHERE s.room_id = ? AND s.starttime < ? AND s.endtime > ? "
                + "AND s.showtimeid <> ? ORDER BY s.starttime";
        return queryShowtimes(sql, roomId, Timestamp.valueOf(end), Timestamp.valueOf(start),
                excludeId != null ? excludeId : "");
    }

    public void insert(ShowtimeDTO st) {
        if (st.getShowtimeID() == null) {
            st.setShowtimeID(UUID.randomUUID().toString());
        }
        String sql = "INSERT INTO showtimes (starttime, endtime, price, movie_id, room_id, showtimeid) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        executeWrite(sql, st, "insert");
    }

    public void update(ShowtimeDTO st) {
        String sql = "UPDATE showtimes SET starttime = ?, endtime = ?, price = ?, movie_id = ?, room_id = ? "
                + "WHERE showtimeid = ?";
        executeWrite(sql, st, "update");
    }

    // Trả về false nếu không có bản ghi nào bị xóa (mã không tồn tại)
    public boolean delete(String showtimeId) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement("DELETE FROM showtimes WHERE showtimeid = ?");
            ps.setString(1, showtimeId);
            int deleted = ps.executeUpdate();
            JpaCacheSync.evictMoviesAndShowtimes();
            return deleted > 0;
        } catch (SQLException e) {
            System.err.println("[AdminShowtimeDAO] Lỗi delete: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public List<RoomDTO> findAllRooms() {
        return queryRooms(SELECT_ROOM + "ORDER BY c.name, r.name");
    }

    public RoomDTO findRoomById(String roomId) {
        List<RoomDTO> list = queryRooms(SELECT_ROOM + "WHERE r.roomid = ?", roomId);
        return list.isEmpty() ? null : list.get(0);
    }

    // ---------- hàm dùng chung ----------

    // Thêm/sửa: thứ tự tham số giống nhau cho cả INSERT và UPDATE ở trên
    private void executeWrite(String sql, ShowtimeDTO st, String action) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setTimestamp(1, Timestamp.valueOf(st.getStartTime()));
            ps.setTimestamp(2, Timestamp.valueOf(st.getEndTime()));
            if (st.getPrice() != null) {
                ps.setDouble(3, st.getPrice());
            } else {
                ps.setNull(3, Types.DOUBLE);
            }
            ps.setString(4, st.getMovie().getMovieID());
            ps.setString(5, st.getRoom().getRoomID());
            ps.setString(6, st.getShowtimeID());
            ps.executeUpdate();
            JpaCacheSync.evictMoviesAndShowtimes();
        } catch (SQLException e) {
            System.err.println("[AdminShowtimeDAO] Lỗi " + action + ": " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    private List<ShowtimeDTO> queryShowtimes(String sql, Object... params) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(sql);
            bind(ps, params);
            rs = ps.executeQuery();
            List<ShowtimeDTO> list = new ArrayList<>();
            while (rs.next()) {
                list.add(readShowtime(rs));
            }
            return list;
        } catch (SQLException e) {
            System.err.println("[AdminShowtimeDAO] Lỗi truy vấn suất chiếu: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    private List<RoomDTO> queryRooms(String sql, Object... params) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(sql);
            bind(ps, params);
            rs = ps.executeQuery();
            List<RoomDTO> list = new ArrayList<>();
            while (rs.next()) {
                list.add(readRoom(rs));
            }
            return list;
        } catch (SQLException e) {
            System.err.println("[AdminShowtimeDAO] Lỗi truy vấn phòng: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    private void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    private ShowtimeDTO readShowtime(ResultSet rs) throws SQLException {
        ShowtimeDTO st = new ShowtimeDTO();
        st.setShowtimeID(rs.getString("showtimeid"));
        st.setStartTime(rs.getTimestamp("starttime").toLocalDateTime());
        st.setEndTime(rs.getTimestamp("endtime").toLocalDateTime());
        double price = rs.getDouble("price");
        st.setPrice(rs.wasNull() ? null : price);

        MovieDTO movie = new MovieDTO();
        movie.setMovieID(rs.getString("movieid"));
        movie.setTitle(rs.getString("title"));
        movie.setDuration(rs.getInt("duration"));
        movie.setAgeRating(rs.getString("agerating"));
        movie.setPosterURL(rs.getString("posterurl"));
        st.setMovie(movie);

        st.setRoom(readRoom(rs));
        return st;
    }

    private RoomDTO readRoom(ResultSet rs) throws SQLException {
        RoomDTO room = new RoomDTO();
        room.setRoomID(rs.getString("roomid"));
        room.setName(rs.getString("room_name"));
        room.setCapacity(rs.getInt("capacity"));
        room.setCinemaID(rs.getString("cinemaid"));
        room.setCinemaName(rs.getString("cinema_name"));
        return room;
    }
}
