package com.cinema.controller.catalog;

import com.cinema.dao.catalog.CinemaDAO;
import com.cinema.dao.catalog.MovieDAO;
import com.cinema.entity.Cinema;
import com.cinema.entity.Movie;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet(name = "MovieListServlet", urlPatterns = {"/movies"})
public class MovieListServlet extends HttpServlet {

    private final MovieDAO movieDAO = new MovieDAO();
    private final CinemaDAO cinemaDAO = new CinemaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 1. Nhận các tham số lọc từ URL / Form
        String tab = request.getParameter("tab");
        if (tab == null || tab.trim().isEmpty()) {
            tab = "now_showing"; // Mặc định là phim đang chiếu
        }

        String genre = request.getParameter("genre");
        String cinemaId = request.getParameter("cinemaId");
        String dateParam = request.getParameter("date");
        String keyword = request.getParameter("keyword");

        LocalDate filterDate = null;
        if (dateParam != null && !dateParam.trim().isEmpty()) {
            try {
                filterDate = LocalDate.parse(dateParam.trim());
            } catch (Exception ignored) {}
        }

        // Với phim sắp chiếu, do chưa có suất chiếu tại rạp nên không áp dụng lọc rạp và ngày
        if ("coming_soon".equalsIgnoreCase(tab)) {
            cinemaId = null;
            filterDate = null;
        }

        // 2. Gọi DAO thực hiện lọc dữ liệu
        List<Movie> moviesList = movieDAO.filterMovies(tab, genre, cinemaId, filterDate, keyword);

        // 3. Lấy dữ liệu rạp, thể loại và danh sách phim nổi bật (cho sân khấu nghệ thuật)
        List<Cinema> cinemaList = cinemaDAO.findAll();
        List<String> genreList = movieDAO.getPopularGenres();
        List<Movie> featuredMovies = movieDAO.findFeaturedMovies();

        // 4. Đẩy toàn bộ dữ liệu sang giao diện movies.jsp
        request.setAttribute("movieList", moviesList);
        request.setAttribute("cinemaList", cinemaList);
        request.setAttribute("genreList", genreList);
        request.setAttribute("featuredMovies", featuredMovies);

        // Giữ lại trạng thái lọc của người dùng
        request.setAttribute("selectedTab", tab);
        request.setAttribute("selectedGenre", genre != null ? genre : "");
        request.setAttribute("selectedCinemaId", cinemaId != null ? cinemaId : "");
        request.setAttribute("selectedDate", dateParam != null ? dateParam : "");
        request.setAttribute("keyword", keyword != null ? keyword : "");

        request.getRequestDispatcher("/WEB-INF/views/catalog/movies.jsp").forward(request, response);
    }
}
