package com.cinema.dao.catalog;

import com.cinema.entity.Room;
import com.cinema.entity.Seat;
import com.cinema.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SeatDAO {

    public List<Seat> findByRoomId(String roomId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT s FROM Seat s WHERE s.room.roomID = :roomId ORDER BY s.seatRow ASC, s.seatCol ASC", Seat.class)
                     .setParameter("roomId", roomId)
                     .getResultList();
        } catch (Exception e) {
            System.err.println("[SeatDAO] Lỗi findByRoomId: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public Seat findById(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Seat.class, id);
        } catch (Exception e) {
            System.err.println("[SeatDAO] Lỗi findById: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public void insert(Seat s) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.persist(s);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[SeatDAO] Lỗi insert: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    public void update(Seat s) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.merge(s);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[SeatDAO] Lỗi update: " + e.getMessage());
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
            Seat s = em.find(Seat.class, id);
            if (s != null) {
                em.remove(s);
            }
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[SeatDAO] Lỗi delete: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    public void deleteByRoomId(String roomId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.createQuery("DELETE FROM Seat s WHERE s.room.roomID = :roomId")
              .setParameter("roomId", roomId)
              .executeUpdate();
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[SeatDAO] Lỗi deleteByRoomId: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    /**
     * Tạo tự động sơ đồ ghế ma trận cho phòng:
     * Ví dụ: rowsCount = 8 (Hàng A đến H), colsCount = 10 (Số 1 đến 10)
     * vipRows: danh sách hàng VIP (VD: E, F, G)
     * coupleRows: danh sách hàng ghế đôi (VD: H)
     */
    public int generateRoomSeats(String roomId, int rowsCount, int colsCount, List<String> vipRows, List<String> coupleRows) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();

            // 1. Tìm phòng
            Room room = em.find(Room.class, roomId);
            if (room == null) {
                throw new IllegalArgumentException("Không tìm thấy phòng với ID: " + roomId);
            }

            // 2. Xóa các ghế cũ của phòng
            em.createQuery("DELETE FROM Seat s WHERE s.room.roomID = :roomId")
              .setParameter("roomId", roomId)
              .executeUpdate();

            // 3. Chuẩn bị câu lệnh Multi-value INSERT nhanh gấp 100 lần (chỉ 1 round-trip)
            StringBuilder sql = new StringBuilder("INSERT INTO seats (seatid, seatcode, seatcol, seatrow, seattype, status, room_id) VALUES ");
            List<Object> params = new ArrayList<>();
            int totalCreated = 0;
            char startRowChar = 'A';

            for (int r = 0; r < rowsCount; r++) {
                String rowLetter = String.valueOf((char) (startRowChar + r));
                boolean isCouple = coupleRows != null && coupleRows.contains(rowLetter);
                boolean isVip = vipRows != null && vipRows.contains(rowLetter);
                String seatType = isCouple ? "COUPLE" : (isVip ? "VIP" : "STANDARD");

                for (int c = 1; c <= colsCount; c++) {
                    String seatCode = rowLetter + (c < 10 ? "0" + c : c);
                    String seatId = UUID.randomUUID().toString();

                    if (totalCreated > 0) sql.append(", ");
                    sql.append("(?, ?, ?, ?, ?, ?, ?)");

                    params.add(seatId);
                    params.add(seatCode);
                    params.add(c);
                    params.add(rowLetter);
                    params.add(seatType);
                    params.add("ACTIVE");
                    params.add(roomId);

                    totalCreated++;
                }
            }

            if (totalCreated > 0) {
                var query = em.createNativeQuery(sql.toString());
                for (int i = 0; i < params.size(); i++) {
                    query.setParameter(i + 1, params.get(i));
                }
                query.executeUpdate();
            }

            // 4. Cập nhật sức chứa (capacity) của phòng tương ứng
            room.setCapacity(totalCreated);
            em.merge(room);

            trans.commit();
            return totalCreated;
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            System.err.println("[SeatDAO] Lỗi generateRoomSeats: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    public long countTotalSeats() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(s) FROM Seat s", Long.class).getSingleResult();
        } catch (Exception e) {
            return 0;
        } finally {
            em.close();
        }
    }

    public long countActiveSeats() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(s) FROM Seat s WHERE s.status = 'ACTIVE'", Long.class).getSingleResult();
        } catch (Exception e) {
            return 0;
        } finally {
            em.close();
        }
    }
}
