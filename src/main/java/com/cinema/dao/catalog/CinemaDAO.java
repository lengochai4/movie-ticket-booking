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

    public void delete(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            Cinema c = em.find(Cinema.class, id);
            if (c != null) {
                em.remove(c);
            }
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[CinemaDAO] Lỗi delete: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }
}
