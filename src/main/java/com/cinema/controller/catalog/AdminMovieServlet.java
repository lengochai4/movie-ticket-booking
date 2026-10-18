package com.cinema.controller.catalog;

import com.cinema.dao.catalog.MovieDAO;
import com.cinema.dao.catalog.ShowtimeDAO;
import com.cinema.entity.Movie;
import com.cinema.entity.Showtime;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/admin/movies", "/admin/movies/save", "/admin/movies/delete"})
public class AdminMovieServlet extends HttpServlet {

    private final MovieDAO movieDAO = new MovieDAO();
    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        List<Movie> allMovies = new ArrayList<>();
        try {
            allMovies = movieDAO.findAll();
            if (allMovies == null) {
                allMovies = new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("[ADMIN MOVIE] Lỗi khi tải danh sách phim từ database: " + e.getMessage());
            e.printStackTrace();
        }

        // 1. Tính toán các chỉ số thống kê dựa hoàn toàn trên dữ liệu backend thực tế
        int totalMovies = allMovies.size();
        LocalDate today = LocalDate.now();
        int nowShowingCount = 0;
        int upcomingCount = 0;
        int totalDuration = 0;

        for (Movie m : allMovies) {
            if (m.getReleaseDate() != null) {
                if (m.getReleaseDate().isAfter(today)) {
                    upcomingCount++;
                } else {
                    nowShowingCount++;
                }
            } else {
                nowShowingCount++;
            }
            totalDuration += m.getDuration();
        }

        int avgDuration = totalMovies > 0 ? Math.round((float) totalDuration / totalMovies) : 0;

        // 2. Lọc phim theo tiêu chí tìm kiếm/lọc nếu người dùng gửi param
        String keyword = request.getParameter("keyword");
        String genre = request.getParameter("genre");
        String ageRating = request.getParameter("ageRating");

        List<Movie> filteredMovies = allMovies;

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            filteredMovies = filteredMovies.stream().filter(m ->
                (m.getTitle() != null && m.getTitle().toLowerCase().contains(kw)) ||
                (m.getDirector() != null && m.getDirector().toLowerCase().contains(kw)) ||
                (m.getGenre() != null && m.getGenre().toLowerCase().contains(kw))
            ).collect(Collectors.toList());
        }

        if (genre != null && !genre.trim().isEmpty()) {
            String g = genre.trim().toLowerCase();
            filteredMovies = filteredMovies.stream().filter(m ->
                m.getGenre() != null && m.getGenre().toLowerCase().contains(g)
            ).collect(Collectors.toList());
        }

        if (ageRating != null && !ageRating.trim().isEmpty()) {
            String ar = ageRating.trim();
            filteredMovies = filteredMovies.stream().filter(m ->
                m.getAgeRating() != null && m.getAgeRating().equalsIgnoreCase(ar)
            ).collect(Collectors.toList());
        }

        // 3. Đưa dữ liệu động vào request
        request.setAttribute("movieList", filteredMovies);
        request.setAttribute("totalMovies", totalMovies);
        request.setAttribute("nowShowingCount", nowShowingCount);
        request.setAttribute("upcomingCount", upcomingCount);
        request.setAttribute("avgDuration", avgDuration);

        // Chuyển tiếp tới giao diện quản lý phim
        request.getRequestDispatcher("/WEB-INF/views/admin/movies.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        if ("/admin/movies/save".equals(path)) {
            handleSave(request);
            response.sendRedirect(request.getContextPath() + "/admin/movies");
            return;
        } else if ("/admin/movies/delete".equals(path)) {
            handleDelete(request);
            response.sendRedirect(request.getContextPath() + "/admin/movies");
            return;
        }

        doGet(request, response);
    }

    private void handleSave(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String id = trimToNull(request.getParameter("movieID"));
        String title = trimToNull(request.getParameter("title"));
        String director = trimToNull(request.getParameter("director"));
        String genre = trimToNull(request.getParameter("genre"));
        String durationStr = trimToNull(request.getParameter("duration"));
        String releaseDateStr = trimToNull(request.getParameter("releaseDate"));
        String ageRating = trimToNull(request.getParameter("ageRating"));
        String posterURL = trimToNull(request.getParameter("posterURL"));
        String trailerURL = trimToNull(request.getParameter("trailerURL"));
        String description = trimToNull(request.getParameter("description"));

        // 1. Kiểm tra dữ liệu đầu vào
        if (title == null) {
            session.setAttribute("errorMessage", "Tên phim không được để trống.");
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationStr);
        } catch (Exception e) {
            duration = 0;
        }
        if (duration <= 0) {
            session.setAttribute("errorMessage", "Thời lượng phim phải là số phút lớn hơn 0.");
            return;
        }

        if (releaseDateStr == null) {
            session.setAttribute("errorMessage", "Vui lòng nhập ngày khởi chiếu. Phim không có ngày khởi chiếu sẽ không hiển thị cho khách.");
            return;
        }
        LocalDate releaseDate;
        try {
            releaseDate = LocalDate.parse(releaseDateStr);
        } catch (Exception e) {
            session.setAttribute("errorMessage", "Ngày khởi chiếu không hợp lệ.");
            return;
        }

        // 2. Lưu vào database
        try {
            if (id != null) {
                // Cập nhật phim hiện có
                Movie movie = movieDAO.findById(id);
                if (movie == null) {
                    session.setAttribute("errorMessage", "Phim cần sửa không còn tồn tại.");
                    return;
                }
                // Đổi thời lượng thì giờ kết thúc của các suất sắp tới phải tính lại,
                // nếu không việc kiểm tra trùng giờ cùng phòng sẽ dùng giờ kết thúc cũ
                int recalculated = 0;
                if (duration != movie.getDuration() && movie.getShowtimes() != null) {
                    LocalDateTime now = LocalDateTime.now();
                    for (Showtime st : movie.getShowtimes()) {
                        if (st.getStartTime() == null || st.getStartTime().isBefore(now)) continue;
                        LocalDateTime newEnd = st.getStartTime().plusMinutes(duration + 15);
                        List<Showtime> overlaps = showtimeDAO.findOverlapping(
                                st.getRoom().getRoomID(), st.getStartTime(), newEnd, st.getShowtimeID());
                        if (!overlaps.isEmpty()) {
                            Showtime conflict = overlaps.get(0);
                            session.setAttribute("errorMessage", "Không thể đổi thời lượng thành " + duration
                                    + " phút: suất chiếu lúc " + st.getFormattedStartTime() + " ngày " + st.getFormattedDate()
                                    + " ở " + st.getRoom().getName() + " sẽ kết thúc lúc " + newEnd.format(TIME_FORMAT)
                                    + ", trùng với suất \"" + (conflict.getMovie() != null ? conflict.getMovie().getTitle() : "")
                                    + "\" lúc " + conflict.getTimeRange() + ". Hãy dời suất chiếu trước.");
                            return;
                        }
                        st.setEndTime(newEnd);
                        recalculated++;
                    }
                }

                movie.setTitle(title);
                movie.setDirector(director);
                movie.setGenre(genre);
                movie.setDuration(duration);
                movie.setReleaseDate(releaseDate);
                movie.setAgeRating(ageRating);
                movie.setPosterURL(posterURL);
                movie.setTrailerURL(trailerURL);
                movie.setDescription(description);
                movieDAO.update(movie);
                System.out.println("[ADMIN MOVIE] Đã cập nhật phim: " + title + " (ID: " + id + ")");
                session.setAttribute("successMessage", "Đã cập nhật phim \"" + title + "\"."
                        + (recalculated > 0 ? " Đã tính lại giờ kết thúc của " + recalculated + " suất chiếu sắp tới." : ""));
            } else {
                // Thêm phim mới
                Movie newMovie = new Movie(title, description, posterURL, director, duration, releaseDate);
                newMovie.setGenre(genre);
                newMovie.setAgeRating(ageRating);
                newMovie.setTrailerURL(trailerURL);
                movieDAO.insert(newMovie);
                System.out.println("[ADMIN MOVIE] Đã thêm phim mới: " + title);
                session.setAttribute("successMessage", "Đã thêm phim \"" + title + "\".");
            }
        } catch (RuntimeException e) {
            System.err.println("[ADMIN MOVIE] Lỗi khi lưu phim: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("errorMessage", "Không thể lưu phim do lỗi hệ thống. Vui lòng thử lại.");
        }
    }

    private void handleDelete(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String id = trimToNull(request.getParameter("movieID"));
        if (id == null) {
            session.setAttribute("errorMessage", "Thiếu mã phim cần xóa.");
            return;
        }
        try {
            movieDAO.delete(id);
            System.out.println("[ADMIN MOVIE] Đã xóa phim có ID: " + id);
            session.setAttribute("successMessage", "Đã xóa phim thành công.");
        } catch (RuntimeException e) {
            System.err.println("[ADMIN MOVIE] Lỗi khi xóa phim: " + e.getMessage());
            if (isForeignKeyViolation(e)) {
                session.setAttribute("errorMessage",
                        "Không thể xóa phim vì đã có vé được đặt cho suất chiếu của phim này.");
            } else {
                e.printStackTrace();
                session.setAttribute("errorMessage", "Không thể xóa phim do lỗi hệ thống. Vui lòng thử lại.");
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

    private String trimToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
