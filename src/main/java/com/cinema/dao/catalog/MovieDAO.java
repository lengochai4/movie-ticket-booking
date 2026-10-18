package com.cinema.dao.catalog;

import com.cinema.entity.Movie;
import com.cinema.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class MovieDAO {

    public List<Movie> findAll() {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Movie> query = em.createQuery("SELECT m FROM Movie m ORDER BY m.title", Movie.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // Lấy danh sách phim đang chiếu
    public List<Movie> findNowShowing() {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            String jpql = "SELECT m FROM Movie m WHERE m.releaseDate <= :today ORDER BY m.releaseDate DESC";
            TypedQuery<Movie> query = em.createQuery(jpql, Movie.class);
            query.setParameter("today", LocalDate.now());
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // Lấy danh sách phim sắp chiếu
    public List<Movie> findComingSoon() {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            String jpql = "SELECT m FROM Movie m WHERE m.releaseDate > :today ORDER BY m.releaseDate ASC";
            TypedQuery<Movie> query = em.createQuery(jpql, Movie.class);
            query.setParameter("today", LocalDate.now());
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // Lấy top 3 phim nổi bật cho khu vực sân khấu nghệ thuật
    public List<Movie> findFeaturedMovies() {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            // Ưu tiên các phim đang chiếu có ngày chiếu gần nhất
            String jpql = "SELECT m FROM Movie m WHERE m.releaseDate <= :today ORDER BY m.releaseDate DESC";
            TypedQuery<Movie> query = em.createQuery(jpql, Movie.class);
            query.setParameter("today", LocalDate.now());
            query.setMaxResults(3);
            List<Movie> list = query.getResultList();
            if (list == null || list.size() < 3) {
                TypedQuery<Movie> allQuery = em.createQuery("SELECT m FROM Movie m ORDER BY m.title", Movie.class);
                allQuery.setMaxResults(3);
                list = allQuery.getResultList();
            }
            return list;
        } catch (Exception e) {
            System.err.println("[MovieDAO] Lỗi findFeaturedMovies: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    // Bộ lọc thông minh theo Thể loại, Rạp, Ngày và Từ khóa
    public List<Movie> filterMovies(String tab, String genre, String cinemaId, LocalDate date, String keyword) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder();
            boolean hasCinemaOrDate = (cinemaId != null && !cinemaId.trim().isEmpty()) || date != null;

            if (hasCinemaOrDate) {
                jpql.append("SELECT DISTINCT m FROM Movie m JOIN m.showtimes s JOIN s.room r WHERE 1=1 ");
                if (cinemaId != null && !cinemaId.trim().isEmpty()) {
                    jpql.append("AND r.cinema.cinemaID = :cinemaId ");
                }
                if (date != null) {
                    jpql.append("AND s.startTime >= :dayStart AND s.startTime <= :dayEnd ");
                }
            } else {
                jpql.append("SELECT m FROM Movie m WHERE 1=1 ");
            }

            // Lọc theo Tab: đang chiếu hay sắp chiếu
            if ("coming_soon".equalsIgnoreCase(tab)) {
                jpql.append("AND m.releaseDate > :today ");
            } else {
                jpql.append("AND m.releaseDate <= :today ");
            }

            // Lọc theo thể loại
            if (genre != null && !genre.trim().isEmpty()) {
                jpql.append("AND LOWER(m.genre) LIKE LOWER(:genre) ");
            }

            // Lọc theo từ khóa tìm kiếm
            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append("AND (LOWER(m.title) LIKE LOWER(:kw) OR LOWER(m.genre) LIKE LOWER(:kw)) ");
            }

            if ("coming_soon".equalsIgnoreCase(tab)) {
                jpql.append("ORDER BY m.releaseDate ASC");
            } else {
                jpql.append("ORDER BY m.releaseDate DESC");
            }

            TypedQuery<Movie> query = em.createQuery(jpql.toString(), Movie.class);
            query.setParameter("today", LocalDate.now());

            if (hasCinemaOrDate) {
                if (cinemaId != null && !cinemaId.trim().isEmpty()) {
                    query.setParameter("cinemaId", cinemaId.trim());
                }
                if (date != null) {
                    query.setParameter("dayStart", date.atStartOfDay());
                    query.setParameter("dayEnd", date.atTime(LocalTime.MAX));
                }
            }

            if (genre != null && !genre.trim().isEmpty()) {
                query.setParameter("genre", "%" + genre.trim() + "%");
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("kw", "%" + keyword.trim() + "%");
            }

            return query.getResultList();
        } catch (Exception e) {
            System.err.println("[MovieDAO] Lỗi filterMovies: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public List<String> getPopularGenres() {
        return Arrays.asList(
            "Hành Động",
            "Phiêu Lưu",
            "Hoạt Hình",
            "Hài",
            "Kinh Dị",
            "Khoa Học Viễn Tưởng",
            "Lãng Mạn",
            "Chính Kịch",
            "Gia Đình",
            "Gây Cấn",
            "Tội Phạm"
        );
    }

    public void insert(Movie movie) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.persist(movie);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[MovieDAO] Lỗi insert: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    public void update(Movie movie) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.merge(movie);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[MovieDAO] Lỗi update: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    public void delete(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            Movie movie = em.find(Movie.class, id);
            if (movie != null) {
                em.remove(movie);
            }
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[MovieDAO] Lỗi delete: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    // Tìm bộ phim theo ID kèm theo toàn bộ suất chiếu, phòng chiếu và cụm rạp
    public Movie findById(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT m FROM Movie m LEFT JOIN FETCH m.showtimes s LEFT JOIN FETCH s.room r LEFT JOIN FETCH r.cinema WHERE m.movieID = :id";
            return em.createQuery(jpql, Movie.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            // Thử query đơn nếu phim chưa có suất chiếu
            try {
                return em.find(Movie.class, id);
            } catch (Exception ex) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    public List<Movie> search(String keyword) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(:kw) OR LOWER(m.genre) LIKE LOWER(:kw)";
            return em.createQuery(jpql, Movie.class).setParameter("kw", "%" + keyword + "%").getResultList();
        } finally {
            em.close();
        }
    }
}
