package com.cinema.dao.admin;

import com.cinema.entity.Combo;
import com.cinema.entity.Movie;
import com.cinema.entity.Showtime;
import com.cinema.util.JPAUtil;
import jakarta.persistence.EntityManagerFactory;

/**
 * Phần của Vinh ghi DB bằng JDBC, trong khi trang khách (Phương) và module khác vẫn đọc bằng JPA.
 * Sau mỗi lần ghi phải xóa cache dùng chung của EclipseLink, nếu không JPA trả về dữ liệu cũ
 * (ví dụ tên phim, giờ chiếu, giá vé trước khi admin sửa).
 */
public class JpaCacheSync {

    public static void evictMoviesAndShowtimes() {
        evict(Movie.class, Showtime.class);
    }

    public static void evictCombos() {
        evict(Combo.class);
    }

    private static void evict(Class<?>... types) {
        try {
            EntityManagerFactory emf = JPAUtil.getEntityManagerFactory();
            if (emf == null) return;
            for (Class<?> type : types) {
                emf.getCache().evict(type);
            }
        } catch (RuntimeException e) {
            System.err.println("[JpaCacheSync] Không xóa được cache JPA: " + e.getMessage());
        }
    }
}
