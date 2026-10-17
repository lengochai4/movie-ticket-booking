package com.cinema.controller.catalog;

import com.cinema.dao.catalog.MovieDAO;
import com.cinema.entity.Movie;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/movie-detail")
public class MovieDetailServlet extends HttpServlet {
    private MovieDAO movieDAO = new MovieDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String id = request.getParameter("id");
        Movie movie = null;

        // Thử tìm trong CSDL nếu có
        try {
            if (id != null && !id.trim().isEmpty()) {
                movie = movieDAO.findById(id);
            }
        } catch (Exception e) {
            // Khi chưa có CSDL hoặc kết nối lỗi, bỏ qua để dùng Mock Data
        }

        // NẾU DATABASE CHƯA CÓ PHIM: Tự động tạo 1 Phim mẫu (Mock Data) để test giao diện
        if (movie == null) {
            movie = new Movie(
                "Dune: Hành Tinh Cát - Phần 2",
                "Hành trình thần thoại tiếp theo của Paul Atreides khi anh hợp nhất cùng Chani và tộc người Fremen trong lúc tìm kiếm sự trả thù cho gia tộc của mình. Đứng trước sự lựa chọn giữa tình yêu của đời mình và số phận của vũ trụ, Paul nỗ lực ngăn chặn một tương lai đen tối mà chỉ anh có thể nhìn thấy trước.",
                "https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
                "Denis Villeneuve",
                166,
                java.time.LocalDate.of(2024, 3, 1)
            );
            movie.setMovieID("demo-dune-2");
            movie.setGenre("Hành Động, Khoa Học Viễn Tưởng, Phiêu Lưu");
            movie.setAgeRating("T16");
        }

        request.setAttribute("movie", movie);
        request.getRequestDispatcher("/WEB-INF/views/catalog/movie-detail.jsp").forward(request, response);
    }
}
