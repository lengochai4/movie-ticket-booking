package com.cinema.controller.catalog;

import com.cinema.dao.admin.AdminMovieDAO;
import com.cinema.dao.admin.AdminShowtimeDAO;
import com.cinema.dao.admin.StatisticsDAO;
import com.cinema.dto.MovieDTO;
import com.cinema.dto.ShowtimeDTO;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/admin/movies", "/admin/movies/save", "/admin/movies/delete"})
public class AdminMovieServlet extends HttpServlet {

    private final AdminMovieDAO movieDAO = new AdminMovieDAO();
    private final AdminShowtimeDAO showtimeDAO = new AdminShowtimeDAO();
    private final StatisticsDAO statisticsDAO = new StatisticsDAO();

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        List<MovieDTO> allMovies = new ArrayList<>();
        try {
            allMovies = movieDAO.findAll();
        } catch (RuntimeException e) {
            System.err.println("[ADMIN MOVIE] Lỗi khi tải danh sách phim từ database: " + e.getMessage());
            request.setAttribute("errorMessage", "Không tải được danh sách phim. Vui lòng thử lại.");
        }

        // 1. Tính toán các chỉ số thống kê dựa hoàn toàn trên dữ liệu backend thực tế
        int totalMovies = allMovies.size();
        LocalDate today = LocalDate.now();
        int nowShowingCount = 0;
        int upcomingCount = 0;
        int totalDuration = 0;

        for (MovieDTO m : allMovies) {
            if (m.getReleaseDate() != null && m.getReleaseDate().isAfter(today)) {
                upcomingCount++;
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

        List<MovieDTO> filteredMovies = allMovies;

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

        // 1. Đọc dữ liệu form vào DTO
        MovieDTO movie = new MovieDTO();
        movie.setMovieID(trimToNull(request.getParameter("movieID")));
        movie.setTitle(trimToNull(request.getParameter("title")));
        movie.setDirector(trimToNull(request.getParameter("director")));
        movie.setGenre(trimToNull(request.getParameter("genre")));
        movie.setAgeRating(trimToNull(request.getParameter("ageRating")));
        movie.setPosterURL(trimToNull(request.getParameter("posterURL")));
        movie.setTrailerURL(trimToNull(request.getParameter("trailerURL")));
        movie.setDescription(trimToNull(request.getParameter("description")));
        String durationStr = trimToNull(request.getParameter("duration"));
        String releaseDateStr = trimToNull(request.getParameter("releaseDate"));

        // 2. Kiểm tra dữ liệu đầu vào
        if (movie.getTitle() == null) {
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
        movie.setDuration(duration);

        if (releaseDateStr == null) {
            session.setAttribute("errorMessage", "Vui lòng nhập ngày khởi chiếu. Phim không có ngày khởi chiếu sẽ không hiển thị cho khách.");
            return;
        }
        try {
            movie.setReleaseDate(LocalDate.parse(releaseDateStr));
        } catch (Exception e) {
            session.setAttribute("errorMessage", "Ngày khởi chiếu không hợp lệ.");
            return;
        }

        // 3. Lưu vào database
        try {
            if (movie.getMovieID() != null) {
                MovieDTO existing = movieDAO.findById(movie.getMovieID());
                if (existing == null) {
                    session.setAttribute("errorMessage", "Phim cần sửa không còn tồn tại.");
                    return;
                }
                // Đổi thời lượng thì giờ kết thúc của các suất sắp tới phải tính lại,
                // nếu không việc kiểm tra trùng giờ cùng phòng sẽ dùng giờ kết thúc cũ
                List<ShowtimeDTO> recalculated = new ArrayList<>();
                if (duration != existing.getDuration()) {
                    for (ShowtimeDTO st : showtimeDAO.findUpcomingByMovie(movie.getMovieID(), LocalDateTime.now())) {
                        LocalDateTime newEnd = st.getStartTime().plusMinutes(duration + 15);
                        List<ShowtimeDTO> overlaps = showtimeDAO.findOverlapping(
                                st.getRoom().getRoomID(), st.getStartTime(), newEnd, st.getShowtimeID());
                        if (!overlaps.isEmpty()) {
                            ShowtimeDTO conflict = overlaps.get(0);
                            session.setAttribute("errorMessage", "Không thể đổi thời lượng thành " + duration
                                    + " phút: suất chiếu lúc " + st.getFormattedStartTime() + " ngày " + st.getFormattedDate()
                                    + " ở " + st.getRoom().getName() + " sẽ kết thúc lúc " + newEnd.format(TIME_FORMAT)
                                    + ", trùng với suất \"" + conflict.getMovie().getTitle()
                                    + "\" lúc " + conflict.getTimeRange() + ". Hãy dời suất chiếu trước.");
                            return;
                        }
                        st.setEndTime(newEnd);
                        recalculated.add(st);
                    }
                }

                movieDAO.updateWithShowtimeEnds(movie, recalculated);
                System.out.println("[ADMIN MOVIE] Đã cập nhật phim: " + movie.getTitle() + " (ID: " + movie.getMovieID() + ")");
                session.setAttribute("successMessage", "Đã cập nhật phim \"" + movie.getTitle() + "\"."
                        + (recalculated.isEmpty() ? "" : " Đã tính lại giờ kết thúc của " + recalculated.size() + " suất chiếu sắp tới."));
            } else {
                movieDAO.insert(movie);
                System.out.println("[ADMIN MOVIE] Đã thêm phim mới: " + movie.getTitle());
                session.setAttribute("successMessage", "Đã thêm phim \"" + movie.getTitle() + "\".");
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
            // Không xóa phim còn đơn đặt vé chưa hủy (xóa phim sẽ xóa luôn suất chiếu của các đơn đó)
            long openBookings = statisticsDAO.countOpenBookingsByMovie(id, BookingStatus.CANCELLED.name());
            if (openBookings > 0) {
                session.setAttribute("errorMessage", "Phim này còn " + openBookings
                        + " đơn đặt vé chưa hủy, không thể xóa. Hãy hủy các đơn đó ở trang Quản lý đặt vé trước.");
                return;
            }
            if (movieDAO.delete(id)) {
                System.out.println("[ADMIN MOVIE] Đã xóa phim có ID: " + id);
                session.setAttribute("successMessage", "Đã xóa phim thành công.");
            } else {
                session.setAttribute("errorMessage", "Phim cần xóa không còn tồn tại.");
            }
        } catch (RuntimeException e) {
            System.err.println("[ADMIN MOVIE] Lỗi khi xóa phim: " + e.getMessage());
            if (isForeignKeyViolation(e)) {
                session.setAttribute("errorMessage",
                        "Không thể xóa phim vì suất chiếu của phim này đã có đơn đặt vé (kể cả đơn đã hủy được lưu làm lịch sử).");
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
