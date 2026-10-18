package com.cinema.dao.catalog;

import com.cinema.entity.Showtime;
import com.cinema.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ShowtimeDAO {

    public List<Showtime> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT s FROM Showtime s LEFT JOIN FETCH s.movie LEFT JOIN FETCH s.room r LEFT JOIN FETCH r.cinema ORDER BY s.startTime DESC";
            return em.createQuery(jpql, Showtime.class).getResultList();
        } catch (Exception e) {
            System.err.println("[ShowtimeDAO] Lỗi findAll: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public List<Showtime> findByDate(LocalDate date) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            String jpql = "SELECT s FROM Showtime s LEFT JOIN FETCH s.movie LEFT JOIN FETCH s.room r LEFT JOIN FETCH r.cinema " +
                          "WHERE s.startTime >= :start AND s.startTime <= :end ORDER BY s.startTime ASC";
            return em.createQuery(jpql, Showtime.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("[ShowtimeDAO] Lỗi findByDate: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public Showtime findById(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT s FROM Showtime s LEFT JOIN FETCH s.movie LEFT JOIN FETCH s.room r LEFT JOIN FETCH r.cinema WHERE s.showtimeID = :id";
            TypedQuery<Showtime> q = em.createQuery(jpql, Showtime.class).setParameter("id", id);
            List<Showtime> list = q.getResultList();
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            System.err.println("[ShowtimeDAO] Lỗi findById: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    // Tìm các suất chiếu cùng phòng bị chồng giờ với khoảng [start, end).
    // excludeId: bỏ qua chính suất chiếu đang sửa (null khi thêm mới)
    public List<Showtime> findOverlapping(String roomId, LocalDateTime start, LocalDateTime end, String excludeId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            boolean hasExclude = excludeId != null && !excludeId.trim().isEmpty();
            String jpql = "SELECT s FROM Showtime s LEFT JOIN FETCH s.movie " +
                          "WHERE s.room.roomID = :roomId AND s.startTime < :end AND s.endTime > :start " +
                          (hasExclude ? "AND s.showtimeID <> :excludeId " : "") +
                          "ORDER BY s.startTime ASC";
            TypedQuery<Showtime> q = em.createQuery(jpql, Showtime.class)
                    .setParameter("roomId", roomId)
                    .setParameter("start", start)
                    .setParameter("end", end);
            if (hasExclude) {
                q.setParameter("excludeId", excludeId.trim());
            }
            return q.getResultList();
        } catch (Exception e) {
            // Không nuốt lỗi: nếu không kiểm tra được thì không cho lưu
            System.err.println("[ShowtimeDAO] Lỗi findOverlapping: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    public void insert(Showtime s) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.persist(s);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[ShowtimeDAO] Lỗi insert: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    public void update(Showtime s) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.merge(s);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[ShowtimeDAO] Lỗi update: " + e.getMessage());
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
            Showtime s = em.find(Showtime.class, id);
            if (s != null) {
                em.remove(s);
            }
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[ShowtimeDAO] Lỗi delete: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }
}
