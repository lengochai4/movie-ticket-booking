package com.cinema.controller.catalog;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/review/add")
public class ReviewServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String movieID = request.getParameter("movieID");
        String rating = request.getParameter("rating");
        String comment = request.getParameter("comment");

        // Giả lập nhận đánh giá thành công khi chưa có DB
        System.out.println(">>> [MOCK REVIEW] Nhận đánh giá phim: " + movieID 
                + " | Số sao: " + rating 
                + " | Nội dung: " + comment);

        // Quay lại trang chi tiết phim
        response.sendRedirect(request.getContextPath() + "/movie-detail?id=" + (movieID != null ? movieID : "demo"));
    }
}
