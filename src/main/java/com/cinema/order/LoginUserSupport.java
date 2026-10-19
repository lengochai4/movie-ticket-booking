package com.cinema.order;

import jakarta.servlet.http.HttpSession;

import java.lang.reflect.Method;

/**
 * Đọc sessionScope.loginUser mà không phụ thuộc class User của Ân.
 */
public final class LoginUserSupport {

    private LoginUserSupport() {
    }

    public static Object getLoginUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return session.getAttribute("loginUser");
    }

    public static String getUserId(Object user) {
        String id = invokeString(user, "getUserId");
        if (isBlank(id)) {
            id = invokeString(user, "getUserID");
        }
        if (isBlank(id)) {
            id = invokeString(user, "getId");
        }
        return id;
    }

    public static String getFullName(Object user) {
        return invokeString(user, "getFullName");
    }

    public static String getEmail(Object user) {
        return invokeString(user, "getEmail");
    }

    public static String getPhone(Object user) {
        return invokeString(user, "getPhone");
    }

    public static String getRole(Object user) {
        return invokeString(user, "getRole");
    }

    public static boolean isAdminOrStaff(Object user) {
        String role = getRole(user);
        if (role == null) {
            return false;
        }
        return "ADMIN".equalsIgnoreCase(role) || "STAFF".equalsIgnoreCase(role);
    }

    public static boolean isAdmin(Object user) {
        String role = getRole(user);
        return "ADMIN".equalsIgnoreCase(role);
    }

    public static boolean isStaff(Object user) {
        String role = getRole(user);
        return "STAFF".equalsIgnoreCase(role);
    }

    public static boolean isUser(Object user) {
        String role = getRole(user);
        return "USER".equalsIgnoreCase(role) || "CUSTOMER".equalsIgnoreCase(role);
    }

    public static boolean isLocked(Object user) {
        String status = invokeString(user, "getStatus");
        return "LOCKED".equalsIgnoreCase(status);
    }

    public static Integer getLoyaltyPoints(Object user) {
        Object value = invoke(user, "getLoyaltyPoints");
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    public static void setLoyaltyPoints(Object user, int points) {
        if (user == null) {
            return;
        }
        try {
            Method method = user.getClass().getMethod("setLoyaltyPoints", Integer.class);
            method.invoke(user, Integer.valueOf(points));
            return;
        } catch (Exception ignored) {
            // thử overload int
        }
        try {
            Method method = user.getClass().getMethod("setLoyaltyPoints", int.class);
            method.invoke(user, points);
        } catch (Exception ignored) {
            // User của Ân chưa có setter thì bỏ qua, điểm vẫn nằm trong loyalty_accounts
        }
    }

    private static String invokeString(Object user, String methodName) {
        Object value = invoke(user, methodName);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private static Object invoke(Object user, String methodName) {
        if (user == null) {
            return null;
        }
        try {
            Method method = user.getClass().getMethod(methodName);
            return method.invoke(user);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
