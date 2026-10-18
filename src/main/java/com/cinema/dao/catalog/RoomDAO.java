package com.cinema.dao.catalog;

import com.cinema.entity.Room;
import com.cinema.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public List<Room> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT r FROM Room r LEFT JOIN FETCH r.cinema ORDER BY r.name", Room.class).getResultList();
        } catch (Exception e) {
            System.err.println("[RoomDAO] Lỗi findAll: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public List<Room> findByCinemaId(String cinemaId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT r FROM Room r LEFT JOIN FETCH r.cinema WHERE r.cinema.cinemaID = :cinemaId ORDER BY r.name", Room.class)
                     .setParameter("cinemaId", cinemaId)
                     .getResultList();
        } catch (Exception e) {
            System.err.println("[RoomDAO] Lỗi findByCinemaId: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public Room findById(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Room.class, id);
        } catch (Exception e) {
            System.err.println("[RoomDAO] Lỗi findById: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public void insert(Room r) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.persist(r);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[RoomDAO] Lỗi insert: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    public void update(Room r) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.merge(r);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[RoomDAO] Lỗi update: " + e.getMessage());
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
            Room r = em.find(Room.class, id);
            if (r != null) {
                em.remove(r);
            }
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[RoomDAO] Lỗi delete: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }
}
