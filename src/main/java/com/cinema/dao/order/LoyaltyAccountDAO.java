package com.cinema.dao.order;

import com.cinema.entity.LoyaltyAccount;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;

public class LoyaltyAccountDAO {

    public LoyaltyAccount find(EntityManager em, String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        return em.find(LoyaltyAccount.class, userId);
    }

    public int ensure(EntityManager em, String userId, Integer initialPoints) {
        LoyaltyAccount account = find(em, userId);
        if (account == null) {
            account = new LoyaltyAccount();
            account.setUserID(userId);
            int seed = initialPoints == null ? 0 : Math.max(0, initialPoints);
            account.setPoints(seed);
            account.setUpdatedAt(LocalDateTime.now());
            em.persist(account);
            return seed;
        }
        if (account.getPoints() == null) {
            account.setPoints(0);
        }
        return account.getPoints();
    }

    /**
     * Cộng hoặc trừ điểm. Không để số dư âm.
     * @return số điểm sau khi cập nhật, hoặc null nếu không có userId
     */
    public Integer adjust(EntityManager em, String userId, int delta) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        LoyaltyAccount account = find(em, userId);
        if (account == null) {
            account = new LoyaltyAccount();
            account.setUserID(userId);
            account.setPoints(0);
            em.persist(account);
        }
        int current = account.getPoints() == null ? 0 : account.getPoints();
        int next = current + delta;
        if (next < 0) {
            next = 0;
        }
        account.setPoints(next);
        account.setUpdatedAt(LocalDateTime.now());
        return next;
    }
}
