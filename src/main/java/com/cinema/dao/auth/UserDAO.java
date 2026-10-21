package com.cinema.dao.auth;

import com.cinema.entity.User;
import com.cinema.util.JPAUtil;
import com.cinema.util.PasswordUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDAO {

    public UserDAO() {
        initDefaultUsers();
    }

    public User findById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(User.class, id.trim());
        } catch (Exception e) {
            System.err.println("[UserDAO] Lỗi findById: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public User findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)", User.class);
            query.setParameter("email", email.trim());
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            System.err.println("[UserDAO] Lỗi findByEmail: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public User findByEmailOrPhone(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return null;
        }
        String idStr = identifier.trim();
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:id) OR u.phone = :id", User.class);
            query.setParameter("id", idStr);
            List<User> list = query.getResultList();
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            System.err.println("[UserDAO] Lỗi findByEmailOrPhone: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public User findByVerificationToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.verificationToken = :token", User.class);
            query.setParameter("token", token.trim());
            List<User> list = query.getResultList();
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            System.err.println("[UserDAO] Lỗi findByVerificationToken: " + e.getMessage());
            return null;
        } finally {
            em.close();
        }
    }

    public List<User> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u ORDER BY u.createdAt DESC", User.class);
            List<User> list = query.getResultList();
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("[UserDAO] Lỗi findAll: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public List<User> filterUsers(String keyword, String role, String status) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT u FROM User u WHERE 1=1 ");

            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append("AND (LOWER(u.fullName) LIKE LOWER(:kw) ")
                    .append("OR LOWER(u.email) LIKE LOWER(:kw) ")
                    .append("OR u.phone LIKE :kw) ");
            }

            if (role != null && !role.trim().isEmpty()) {
                if ("USER".equalsIgnoreCase(role) || "CUSTOMER".equalsIgnoreCase(role)) {
                    jpql.append("AND (UPPER(u.role) = 'USER' OR UPPER(u.role) = 'CUSTOMER') ");
                } else {
                    jpql.append("AND UPPER(u.role) = UPPER(:role) ");
                }
            }

            if (status != null && !status.trim().isEmpty()) {
                jpql.append("AND UPPER(u.status) = UPPER(:status) ");
            }

            jpql.append("ORDER BY u.createdAt DESC");

            TypedQuery<User> query = em.createQuery(jpql.toString(), User.class);

            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("kw", "%" + keyword.trim() + "%");
            }
            if (role != null && !role.trim().isEmpty() && !"USER".equalsIgnoreCase(role) && !"CUSTOMER".equalsIgnoreCase(role)) {
                query.setParameter("role", role.trim());
            }
            if (status != null && !status.trim().isEmpty()) {
                query.setParameter("status", status.trim());
            }

            List<User> list = query.getResultList();
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("[UserDAO] Lỗi filterUsers: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public int countAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Long count = em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            return 0;
        } finally {
            em.close();
        }
    }

    public int countByRole(String role) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql;
            if ("USER".equalsIgnoreCase(role) || "CUSTOMER".equalsIgnoreCase(role)) {
                jpql = "SELECT COUNT(u) FROM User u WHERE UPPER(u.role) = 'USER' OR UPPER(u.role) = 'CUSTOMER'";
                Long count = em.createQuery(jpql, Long.class).getSingleResult();
                return count != null ? count.intValue() : 0;
            } else {
                jpql = "SELECT COUNT(u) FROM User u WHERE UPPER(u.role) = UPPER(:role)";
                Long count = em.createQuery(jpql, Long.class).setParameter("role", role).getSingleResult();
                return count != null ? count.intValue() : 0;
            }
        } catch (Exception e) {
            return 0;
        } finally {
            em.close();
        }
    }

    public int countByStatus(String status) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE UPPER(u.status) = UPPER(:status)", Long.class)
                    .setParameter("status", status)
                    .getSingleResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            return 0;
        } finally {
            em.close();
        }
    }

    public int countActiveAdmins() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE UPPER(u.role) = 'ADMIN' AND UPPER(u.status) = 'ACTIVE'", Long.class)
                    .getSingleResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            System.err.println("[UserDAO] Lỗi countActiveAdmins: " + e.getMessage());
            return 0;
        } finally {
            em.close();
        }
    }

    public boolean insert(User user) {
        if (user == null) {
            return false;
        }
        if (user.getId() == null || user.getId().isBlank()) {
            user.setId(UUID.randomUUID().toString());
        }
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(user);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("[UserDAO] Lỗi insert: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    public boolean update(User user) {
        if (user == null) {
            return false;
        }
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(user);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("[UserDAO] Lỗi update: " + e.getMessage());
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
            User user = em.find(User.class, id.trim());
            if (user != null) {
                em.remove(user);
            }
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("[UserDAO] Lỗi delete: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Tự động khởi tạo dữ liệu người dùng mẫu nếu bảng users đang trống.
     */
    private void initDefaultUsers() {
        try {
            if (countAll() > 0) {
                return;
            }
            System.out.println("👤 Khởi tạo dữ liệu người dùng ban đầu...");

            String defaultPass = PasswordUtil.hash("Password@123");

            insert(new User("usr-admin-01", "Nguyễn Thị Lan Phương", "admin@cinetik.com", "0988123456", defaultPass, "ADMIN", "ACTIVE"));
            insert(new User("usr-admin-02", "Nguyễn Thị Lan Phương", "thilanphuong24@gmail.com", "0988123457", defaultPass, "ADMIN", "ACTIVE"));
            insert(new User("usr-staff-01", "Trần Hải Đăng", "haidang.cinetik@gmail.com", "0912345678", defaultPass, "STAFF", "ACTIVE"));
            insert(new User("usr-user-01", "Lê Gia Ân", "giaan.film@gmail.com", "0909876543", defaultPass, "USER", "ACTIVE"));
            insert(new User("usr-user-02", "Phạm Hoàng Nam", "hoangnam99@yahoo.com", "0934567890", defaultPass, "USER", "ACTIVE"));
            insert(new User("usr-user-03", "Vũ Thị Mai Chi", "maichi.vu@gmail.com", "0977889900", defaultPass, "USER", "LOCKED"));
            insert(new User("usr-user-04", "Đặng Quốc Huy", "quochuy.cinema@outlook.com", "0965432109", defaultPass, "USER", "ACTIVE"));

            System.out.println(" Đã khởi tạo người dùng mặc định thành công!");
        } catch (Exception e) {
            System.err.println("[UserDAO] Lỗi initDefaultUsers: " + e.getMessage());
        }
    }
}
