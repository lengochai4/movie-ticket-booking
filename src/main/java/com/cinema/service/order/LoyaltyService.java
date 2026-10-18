package com.cinema.service.order;

import com.cinema.dao.order.LoyaltyAccountDAO;
import com.cinema.entity.LoyaltyAccount;
import com.cinema.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

/**
 * Hợp đồng điểm thưởng cho Hải gọi lúc thanh toán.
 * 1 điểm = 10.000đ.
 */
public class LoyaltyService {

    public static final int POINT_VALUE_VND = 10000;

    private final LoyaltyAccountDAO loyaltyAccountDAO = new LoyaltyAccountDAO();

    public int getPoints(String userId) {
        if (userId == null || userId.isBlank()) {
            return 0;
        }
        EntityManager em = open();
        try {
            LoyaltyAccount account = loyaltyAccountDAO.find(em, userId.trim());
            if (account == null || account.getPoints() == null) {
                return 0;
            }
            return account.getPoints();
        } finally {
            em.close();
        }
    }

    public int addPoints(String userId, int points) {
        requireUser(userId);
        if (points < 0) {
            throw new IllegalArgumentException("Số điểm cộng phải lớn hơn hoặc bằng 0.");
        }
        if (points == 0) {
            return getPoints(userId);
        }
        EntityManager em = open();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            Integer balance = loyaltyAccountDAO.adjust(em, userId.trim(), points);
            trans.commit();
            return balance == null ? 0 : balance;
        } catch (RuntimeException e) {
            rollback(trans);
            throw e;
        } finally {
            em.close();
        }
    }

    public int usePoints(String userId, int points) {
        requireUser(userId);
        if (points < 0) {
            throw new IllegalArgumentException("Số điểm sử dụng phải lớn hơn hoặc bằng 0.");
        }
        if (points == 0) {
            return getPoints(userId);
        }
        EntityManager em = open();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            int balance = usePoints(em, userId.trim(), points);
            trans.commit();
            return balance;
        } catch (RuntimeException e) {
            rollback(trans);
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Trừ điểm trong giao dịch đang mở (dùng khi lưu đơn vé).
     */
    public int usePoints(EntityManager em, String userId, int points) {
        requireUser(userId);
        if (points < 0) {
            throw new IllegalArgumentException("Số điểm sử dụng phải lớn hơn hoặc bằng 0.");
        }
        if (points == 0) {
            LoyaltyAccount account = loyaltyAccountDAO.find(em, userId.trim());
            if (account == null || account.getPoints() == null) {
                return 0;
            }
            return account.getPoints();
        }
        int balance = loyaltyAccountDAO.ensure(em, userId.trim(), 0);
        if (points > balance) {
            throw new IllegalStateException("Không đủ điểm thưởng. Hiện có " + balance + " điểm.");
        }
        Integer next = loyaltyAccountDAO.adjust(em, userId.trim(), -points);
        return next == null ? 0 : next;
    }

    private void requireUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã thành viên.");
        }
    }

    private EntityManager open() {
        if (JPAUtil.getEntityManagerFactory() == null) {
            throw new IllegalStateException("Không kết nối được cơ sở dữ liệu.");
        }
        return JPAUtil.getEntityManagerFactory().createEntityManager();
    }

    private void rollback(EntityTransaction trans) {
        if (trans != null && trans.isActive()) {
            trans.rollback();
        }
    }
}
