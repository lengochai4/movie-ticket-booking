package com.cinema.dao.review;

import com.cinema.entity.Review;
import com.cinema.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ReviewDAO {

    public ReviewDAO() {
        initDefaultReviews();
    }

    public List<Review> findByMovieId(String movieId) {
        if (movieId == null || movieId.isBlank()) {
            return new ArrayList<>();
        }
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Review> query = em.createQuery(
                    "SELECT r FROM Review r WHERE r.movieID = :mId ORDER BY r.createdAt DESC", Review.class);
            query.setParameter("mId", movieId.trim());
            List<Review> list = query.getResultList();
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("[ReviewDAO] Lỗi findByMovieId: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public double getAverageRating(String movieId) {
        if (movieId == null || movieId.isBlank()) {
            return 5.0;
        }
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Double avg = em.createQuery(
                    "SELECT AVG(r.rating) FROM Review r WHERE r.movieID = :mId", Double.class)
                    .setParameter("mId", movieId.trim())
                    .getSingleResult();
            if (avg == null) {
                return 5.0;
            }
            return Double.parseDouble(String.format(Locale.US, "%.1f", avg));
        } catch (Exception e) {
            return 5.0;
        } finally {
            em.close();
        }
    }

    public int countByMovieId(String movieId) {
        if (movieId == null || movieId.isBlank()) {
            return 0;
        }
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(r) FROM Review r WHERE r.movieID = :mId", Long.class)
                    .setParameter("mId", movieId.trim())
                    .getSingleResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            return 0;
        } finally {
            em.close();
        }
    }

    public boolean insert(Review review) {
        if (review == null) {
            return false;
        }
        if (review.getId() == null || review.getId().isBlank()) {
            review.setId(UUID.randomUUID().toString());
        }
        if (review.getCreatedAtDateTime() == null) {
            review.setCreatedAt(LocalDateTime.now());
        }
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(review);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("[ReviewDAO] Lỗi insert: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    public boolean delete(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Review r = em.find(Review.class, id.trim());
            if (r != null) {
                em.remove(r);
            }
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("[ReviewDAO] Lỗi delete: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    private void initDefaultReviews() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Long total = em.createQuery("SELECT COUNT(r) FROM Review r", Long.class).getSingleResult();
            if (total != null && total > 0) {
                return;
            }
            System.out.println(" Khởi tạo các đánh giá phim mẫu...");

            Review r1 = new Review(
                    "rev-001",
                    "demo-dune-2",
                    "usr-staff-01",
                    "Trần Hải Đăng",
                    5,
                    "Kỹ xảo và âm thanh tại phòng chiếu IMAX của Cinetik thực sự bùng nổ! Đoạn cao trào cuối phim làm mình nổi cả da gà. Rất đáng tiền vé!"
            );
            r1.setCreatedAt(LocalDateTime.now().minusDays(1));
            r1.setHelpfulCount(18);
            insert(r1);

            Review r2 = new Review(
                    "rev-002",
                    "demo-dune-2",
                    "usr-user-01",
                    "Lê Gia Ân",
                    4,
                    "Diễn xuất của dàn cast chính xuất sắc, cốt truyện có chiều sâu và nhiều cú twist bất ngờ. Điểm trừ nhẹ là nhịp phim đầu hơi chậm một chút."
            );
            r2.setCreatedAt(LocalDateTime.now().minusDays(2));
            r2.setHelpfulCount(9);
            insert(r2);

            Review r3 = new Review(
                    "rev-003",
                    "demo-dune-2",
                    "usr-user-02",
                    "Phạm Hoàng Nam",
                    5,
                    "Phim quá hay, nhạc phim đọng lại nhiều cảm xúc. Cinetik phục vụ bắp nước rất nhanh và ghế ngồi êm ái!"
            );
            r3.setCreatedAt(LocalDateTime.now().minusDays(4));
            r3.setHelpfulCount(5);
            insert(r3);

            System.out.println(" Đã khởi tạo đánh giá phim mẫu thành công!");
        } catch (Exception e) {
            System.err.println("[ReviewDAO] Lỗi initDefaultReviews: " + e.getMessage());
        } finally {
            em.close();
        }
    }
}
