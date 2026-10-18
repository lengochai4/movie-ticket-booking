package com.cinema.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {
    private static EntityManagerFactory emf;

    // Khởi tạo EntityManagerFactory 1 lần duy nhất
    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            try {
                // "CinemaPU" phải khớp với name trong persistence.xml
                emf = Persistence.createEntityManagerFactory("CinemaPU");
            } catch (Exception e) {
                System.err.println("Lỗi khởi tạo EntityManagerFactory: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return emf;
    }

    public static jakarta.persistence.EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    // HÀM MAIN ĐỂ TEST NHANH KẾT NỐI (Không cần bật Tomcat)
    public static void main(String[] args) {
        System.out.println("Đang kết nối Database...");
        EntityManagerFactory factory = JPAUtil.getEntityManagerFactory();
        if (factory != null) {
            System.out.println("KẾT NỐI POSTGRESQL THÀNH CÔNG!");
        } else {
            System.out.println("KẾT NỐI THẤT BẠI. Hãy kiểm tra lại URL, User, Pass.");
        }
    }
}