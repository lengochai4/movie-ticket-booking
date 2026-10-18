<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%---- 2. FILM REEL TICKET BANNER (BĂNG CUỘN VÉ PHIM CỔ ĐIỂN) ----%>
<section class="movie-ticket-banner">
    <img src="${movie.posterURL}" 
         alt="${movie.title}" 
         class="banner-poster">
    <div class="banner-content">
        <span class="banner-tag">${not empty movieFormat ? movieFormat : (not empty format ? format : '2D Phụ Đề Tiếng Việt')} · ${movie.ageRating}</span>
        <h1 class="banner-title">${movie.title}</h1>
        <div class="banner-meta">
            <span class="banner-meta-item">🕒 <strong>${showtime.startTime}</strong></span>
            <span class="banner-meta-item">📍 ${cinema.name}</span>
            <span class="banner-meta-item">🚪 ${room.name}</span>
        </div>
    </div>
</section>
