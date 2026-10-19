package com.cinema.dao.admin;

import com.cinema.dto.MovieDTO;
import com.cinema.dto.ShowtimeDTO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * CRUD phim cho trang Admin Phim của Vinh (JDBC: ConnectionPool + PreparedStatement).
 */
public class AdminMovieDAO {

    private static final String SELECT_MOVIE =
            "SELECT movieid, title, description, posterurl, trailerurl, director, duration, releasedate, genre, agerating "
            + "FROM movies ";

    public List<MovieDTO> findAll() {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(SELECT_MOVIE + "ORDER BY title");
            rs = ps.executeQuery();
            List<MovieDTO> movies = new ArrayList<>();
            while (rs.next()) {
                movies.add(readMovie(rs));
            }
            return movies;
        } catch (SQLException e) {
            System.err.println("[AdminMovieDAO] Lỗi findAll: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public MovieDTO findById(String movieId) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(SELECT_MOVIE + "WHERE movieid = ?");
            ps.setString(1, movieId);
            rs = ps.executeQuery();
            return rs.next() ? readMovie(rs) : null;
        } catch (SQLException e) {
            System.err.println("[AdminMovieDAO] Lỗi findById: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public void insert(MovieDTO movie) {
        if (movie.getMovieID() == null) {
            movie.setMovieID(UUID.randomUUID().toString());
        }
        String sql = "INSERT INTO movies (movieid, title, description, posterurl, trailerurl, director, "
                + "duration, releasedate, genre, agerating) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setString(1, movie.getMovieID());
            setMovieColumns(ps, movie, 2);
            ps.executeUpdate();
            JpaCacheSync.evictMoviesAndShowtimes();
        } catch (SQLException e) {
            System.err.println("[AdminMovieDAO] Lỗi insert: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public void update(MovieDTO movie) {
        updateWithShowtimeEnds(movie, new ArrayList<>());
    }

    /**
     * Cập nhật phim và giờ kết thúc của các suất chiếu (khi đổi thời lượng) trong cùng một giao dịch.
     */
    public void updateWithShowtimeEnds(MovieDTO movie, List<ShowtimeDTO> showtimes) {
        String updateMovie = "UPDATE movies SET title = ?, description = ?, posterurl = ?, trailerurl = ?, director = ?, "
                + "duration = ?, releasedate = ?, genre = ?, agerating = ? WHERE movieid = ?";
        String updateEnd = "UPDATE showtimes SET endtime = ? WHERE showtimeid = ?";
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement psMovie = null;
        PreparedStatement psEnd = null;
        try {
            connection = pool.getConnection();
            connection.setAutoCommit(false);

            psMovie = connection.prepareStatement(updateMovie);
            setMovieColumns(psMovie, movie, 1);
            psMovie.setString(10, movie.getMovieID());
            psMovie.executeUpdate();

            if (!showtimes.isEmpty()) {
                psEnd = connection.prepareStatement(updateEnd);
                for (ShowtimeDTO st : showtimes) {
                    psEnd.setTimestamp(1, Timestamp.valueOf(st.getEndTime()));
                    psEnd.setString(2, st.getShowtimeID());
                    psEnd.addBatch();
                }
                psEnd.executeBatch();
            }

            connection.commit();
            JpaCacheSync.evictMoviesAndShowtimes();
        } catch (SQLException e) {
            DBUtil.rollback(connection);
            System.err.println("[AdminMovieDAO] Lỗi update: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            DBUtil.closePreparedStatement(psEnd);
            DBUtil.closePreparedStatement(psMovie);
            DBUtil.resetAutoCommit(connection);
            pool.freeConnection(connection);
        }
    }

    /**
     * Xóa phim cùng các suất chiếu của phim trong một giao dịch.
     * Nếu suất chiếu đã có đơn đặt vé, khóa ngoại bookings.showtimeid sẽ chặn lại (SQLState 23503).
     * Trả về false nếu không có phim nào bị xóa (mã không tồn tại).
     */
    public boolean delete(String movieId) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement psShowtimes = null;
        PreparedStatement psMovie = null;
        try {
            connection = pool.getConnection();
            connection.setAutoCommit(false);

            psShowtimes = connection.prepareStatement("DELETE FROM showtimes WHERE movie_id = ?");
            psShowtimes.setString(1, movieId);
            psShowtimes.executeUpdate();

            psMovie = connection.prepareStatement("DELETE FROM movies WHERE movieid = ?");
            psMovie.setString(1, movieId);
            int deleted = psMovie.executeUpdate();

            connection.commit();
            JpaCacheSync.evictMoviesAndShowtimes();
            return deleted > 0;
        } catch (SQLException e) {
            DBUtil.rollback(connection);
            System.err.println("[AdminMovieDAO] Lỗi delete: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            DBUtil.closePreparedStatement(psMovie);
            DBUtil.closePreparedStatement(psShowtimes);
            DBUtil.resetAutoCommit(connection);
            pool.freeConnection(connection);
        }
    }

    // Gán 9 cột dữ liệu phim (title ... agerating) bắt đầu từ vị trí start
    private void setMovieColumns(PreparedStatement ps, MovieDTO m, int start) throws SQLException {
        ps.setString(start, m.getTitle());
        ps.setString(start + 1, m.getDescription());
        ps.setString(start + 2, m.getPosterURL());
        ps.setString(start + 3, m.getTrailerURL());
        ps.setString(start + 4, m.getDirector());
        ps.setInt(start + 5, m.getDuration());
        if (m.getReleaseDate() != null) {
            ps.setDate(start + 6, Date.valueOf(m.getReleaseDate()));
        } else {
            ps.setNull(start + 6, Types.DATE);
        }
        ps.setString(start + 7, m.getGenre());
        ps.setString(start + 8, m.getAgeRating());
    }

    static MovieDTO readMovie(ResultSet rs) throws SQLException {
        MovieDTO m = new MovieDTO();
        m.setMovieID(rs.getString("movieid"));
        m.setTitle(rs.getString("title"));
        m.setDescription(rs.getString("description"));
        m.setPosterURL(rs.getString("posterurl"));
        m.setTrailerURL(rs.getString("trailerurl"));
        m.setDirector(rs.getString("director"));
        m.setDuration(rs.getInt("duration"));
        Date releaseDate = rs.getDate("releasedate");
        m.setReleaseDate(releaseDate != null ? releaseDate.toLocalDate() : null);
        m.setGenre(rs.getString("genre"));
        m.setAgeRating(rs.getString("agerating"));
        return m;
    }
}
