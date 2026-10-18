package com.cinema.controller.catalog;

import com.cinema.dao.catalog.CinemaDAO;
import com.cinema.entity.Cinema;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/cinemas")
public class CinemaListServlet extends HttpServlet {
    private CinemaDAO cinemaDAO = new CinemaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Cinema> cinemas = cinemaDAO.findAll();
        request.setAttribute("cinemas", cinemas);
        request.getRequestDispatcher("/WEB-INF/views/catalog/cinemas.jsp").forward(request, response);
    }
}
