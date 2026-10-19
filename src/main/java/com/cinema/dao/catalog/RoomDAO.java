package com.cinema.dao.catalog;

import com.cinema.entity.Room;
import com.cinema.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // Trùng tên phòng trong cùng một rạp. excludeRoomId: bỏ qua chính phòng đang sửa
    public boolean existsByNameInCinema(String cinemaId, String name, String excludeRoomId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(r) FROM Room r WHERE r.cinema.cinemaID = :cinemaId "
                            + "AND LOWER(r.name) = LOWER(:name) AND r.roomID <> :excludeId", Long.class)
                     .setParameter("cinemaId", cinemaId)
                     .setParameter("name", name)
                     .setParameter("excludeId", excludeRoomId != null ? excludeRoomId : "")
                     .getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    // Số phòng hiện có của từng rạp (cinemaID -> số phòng), đếm thẳng trong DB thay vì
    // cinema.getRooms() (danh sách cache của JPA không tự cập nhật khi thêm/xóa phòng)
    public Map<String, Long> countRoomsByCinema() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Map<String, Long> counts = new HashMap<>();
            List<Object[]> rows = em.createQuery("SELECT r.cinema.cinemaID, COUNT(r) FROM Room r "
                            + "GROUP BY r.cinema.cinemaID", Object[].class)
                    .getResultList();
            for (Object[] row : rows) {
                counts.put((String) row[0], ((Number) row[1]).longValue());
            }
            return counts;
        } catch (Exception e) {
            System.err.println("[RoomDAO] Lỗi countRoomsByCinema: " + e.getMessage());
            return new HashMap<>();
        } finally {
            em.close();
        }
    }

    // Số ghế thực tế của từng phòng trong rạp (roomID -> số ghế), dùng làm sức chứa hiển thị
    public Map<String, Long> countSeatsByCinema(String cinemaId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Map<String, Long> counts = new HashMap<>();
            List<Object[]> rows = em.createQuery("SELECT r.roomID, COUNT(s) FROM Room r LEFT JOIN r.seats s "
                            + "WHERE r.cinema.cinemaID = :cinemaId GROUP BY r.roomID", Object[].class)
                    .setParameter("cinemaId", cinemaId)
                    .getResultList();
            for (Object[] row : rows) {
                counts.put((String) row[0], ((Number) row[1]).longValue());
            }
            return counts;
        } catch (Exception e) {
            System.err.println("[RoomDAO] Lỗi countSeatsByCinema: " + e.getMessage());
            return new HashMap<>();
        } finally {
            em.close();
        }
    }

    /**
     * Xóa phòng cùng sơ đồ ghế.
     * Từ chối (IllegalStateException) nếu phòng còn suất chiếu hoặc ghế của phòng đã có vé.
     */
    public void delete(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            Room r = em.find(Room.class, id);
            if (r == null) {
                throw new IllegalArgumentException("Phòng chiếu cần xóa không còn tồn tại.");
            }
            long showtimes = em.createQuery("SELECT COUNT(s) FROM Showtime s WHERE s.room.roomID = :id", Long.class)
                               .setParameter("id", id).getSingleResult();
            if (showtimes > 0) {
                throw new IllegalStateException("Không thể xóa \"" + r.getName() + "\": phòng còn " + showtimes
                        + " suất chiếu. Hãy xóa hoặc chuyển các suất chiếu trước.");
            }
            long tickets = em.createQuery("SELECT COUNT(t) FROM Ticket t WHERE t.seat.room.roomID = :id", Long.class)
                             .setParameter("id", id).getSingleResult();
            if (tickets > 0) {
                throw new IllegalStateException("Không thể xóa \"" + r.getName() + "\": ghế của phòng đã có "
                        + tickets + " vé được bán.");
            }
            em.remove(r);
            trans.commit();
        } catch (IllegalStateException | IllegalArgumentException e) {
            if (trans.isActive()) trans.rollback();
            throw e;
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[RoomDAO] Lỗi delete: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }
}
