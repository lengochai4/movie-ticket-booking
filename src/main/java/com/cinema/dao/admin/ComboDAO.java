package com.cinema.dao.admin;

import com.cinema.entity.Combo;
import com.cinema.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.List;

public class ComboDAO {

    public List<Combo> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Combo c ORDER BY c.category, c.price", Combo.class).getResultList();
        } catch (Exception e) {
            System.err.println("[ComboDAO] Lỗi findAll: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    // Danh sách combo đang mở bán, dùng cho bước chọn combo khi đặt vé (module của Hải)
    public List<Combo> findActive() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Combo c WHERE c.status = :status ORDER BY c.category, c.price", Combo.class)
                     .setParameter("status", Combo.STATUS_ACTIVE)
                     .getResultList();
        } catch (Exception e) {
            System.err.println("[ComboDAO] Lỗi findActive: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public Combo findById(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Combo.class, id);
        } catch (Exception e) {
            System.err.println("[ComboDAO] Lỗi findById: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public void insert(Combo c) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.persist(c);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[ComboDAO] Lỗi insert: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    public void update(Combo c) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.merge(c);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[ComboDAO] Lỗi update: " + e.getMessage());
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
            Combo c = em.find(Combo.class, id);
            if (c != null) {
                em.remove(c);
            }
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[ComboDAO] Lỗi delete: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }
}
