package com.cinema.controller.catalog;

import com.cinema.dao.catalog.MovieDAO;
import com.cinema.entity.Movie;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/admin/movies", "/admin/movies/save", "/admin/movies/delete"})
public class AdminMovieServlet extends HttpServlet {

    private final MovieDAO movieDAO = new MovieDAO();

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
            String id = request.getParameter("movieID");
            String title = request.getParameter("title");
            String director = request.getParameter("director");
            String genre = request.getParameter("genre");
            String durationStr = request.getParameter("duration");
            String releaseDateStr = request.getParameter("releaseDate");
            String ageRating = request.getParameter("ageRating");
            String posterURL = request.getParameter("posterURL");
            String description = request.getParameter("description");

            int duration = 0;
            if (durationStr != null && !durationStr.trim().isEmpty()) {
                try {
                    duration = Integer.parseInt(durationStr.trim());
                } catch (NumberFormatException ignored) {}
            }

            LocalDate releaseDate = null;
            if (releaseDateStr != null && !releaseDateStr.trim().isEmpty()) {
                try {
                    releaseDate = LocalDate.parse(releaseDateStr.trim());
                } catch (Exception ignored) {}
            }

            try {
                if (id != null && !id.trim().isEmpty()) {
                    // Cập nhật phim hiện có
                    Movie movie = movieDAO.findById(id.trim());
                    if (movie != null) {
                        movie.setTitle(title);
                        movie.setDirector(director);
                        movie.setGenre(genre);
                        movie.setDuration(duration);
                        movie.setReleaseDate(releaseDate);
                        movie.setAgeRating(ageRating);
                        movie.setPosterURL(posterURL);
                        movie.setDescription(description);
                        movieDAO.update(movie);
                        System.out.println("[ADMIN MOVIE] Đã cập nhật phim: " + title + " (ID: " + id + ")");
                    }
                } else {
                    // Thêm phim mới
                    Movie newMovie = new Movie(title, description, posterURL, director, duration, releaseDate);
                    newMovie.setGenre(genre);
                    newMovie.setAgeRating(ageRating);
                    movieDAO.insert(newMovie);
                    System.out.println("[ADMIN MOVIE] Đã thêm phim mới: " + title);
                }
            } catch (Exception e) {
                System.err.println("[ADMIN MOVIE] Lỗi khi lưu phim: " + e.getMessage());
                e.printStackTrace();
            }

            response.sendRedirect(request.getContextPath() + "/admin/movies");
            return;
        } else if ("/admin/movies/delete".equals(path)) {
            String id = request.getParameter("movieID");
            if (id != null && !id.trim().isEmpty()) {
                try {
                    movieDAO.delete(id.trim());
                    System.out.println("[ADMIN MOVIE] Đã xóa phim có ID: " + id);
                } catch (Exception e) {
                    System.err.println("[ADMIN MOVIE] Lỗi khi xóa phim: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            response.sendRedirect(request.getContextPath() + "/admin/movies");
            return;
        }

        doGet(request, response);
    }
}
