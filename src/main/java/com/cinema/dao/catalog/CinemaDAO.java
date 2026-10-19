package com.cinema.dao.catalog;

import com.cinema.entity.Cinema;
import com.cinema.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.List;

public class CinemaDAO {

    public List<Cinema> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Cinema c ORDER BY c.name", Cinema.class).getResultList();
        } catch (Exception e) {
            System.err.println("[CinemaDAO] Lỗi findAll: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public Cinema findById(String cinemaId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Cinema.class, cinemaId);
        } catch (Exception e) {
            System.err.println("[CinemaDAO] Lỗi findById: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public void insert(Cinema c) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.persist(c);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[CinemaDAO] Lỗi insert: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    public void update(Cinema c) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.merge(c);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[CinemaDAO] Lỗi update: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    // Trùng tên rạp (không phân biệt hoa thường). excludeId: bỏ qua chính rạp đang sửa
    public boolean existsByName(String name, String excludeId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(c) FROM Cinema c WHERE LOWER(c.name) = LOWER(:name) AND c.cinemaID <> :excludeId", Long.class)
                     .setParameter("name", name)
                     .setParameter("excludeId", excludeId != null ? excludeId : "")
                     .getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    /**
     * Xóa rạp cùng các phòng và ghế của rạp.
     * Từ chối (IllegalStateException) nếu rạp còn suất chiếu hoặc đã có vé bán ra,
     * vì xóa sẽ làm mất đơn đặt vé và số liệu doanh thu.
     */
    public void delete(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            Cinema c = em.find(Cinema.class, id);
            if (c == null) {
                throw new IllegalArgumentException("Cụm rạp cần xóa không còn tồn tại.");
            }
            long showtimes = em.createQuery("SELECT COUNT(s) FROM Showtime s WHERE s.room.cinema.cinemaID = :id", Long.class)
                               .setParameter("id", id).getSingleResult();
            if (showtimes > 0) {
                throw new IllegalStateException("Không thể xóa rạp \"" + c.getName() + "\": các phòng của rạp còn "
                        + showtimes + " suất chiếu. Hãy xóa hoặc chuyển các suất chiếu trước.");
            }
            long tickets = em.createQuery("SELECT COUNT(t) FROM Ticket t WHERE t.seat.room.cinema.cinemaID = :id", Long.class)
                             .setParameter("id", id).getSingleResult();
            if (tickets > 0) {
                throw new IllegalStateException("Không thể xóa rạp \"" + c.getName() + "\": đã có " + tickets
                        + " vé được bán tại rạp này.");
            }
            em.remove(c);
            trans.commit();
        } catch (IllegalStateException | IllegalArgumentException e) {
            if (trans.isActive()) trans.rollback();
            throw e;
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[CinemaDAO] Lỗi delete: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }
}
