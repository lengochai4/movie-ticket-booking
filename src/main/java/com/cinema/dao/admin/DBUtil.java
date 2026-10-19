package com.cinema.dao.admin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Đóng tài nguyên JDBC theo mẫu sách Murach (Chương 12).
 */
public class DBUtil {

    public static void closeStatement(Statement s) {
        try {
            if (s != null) {
                s.close();
            }
        } catch (SQLException e) {
            System.err.println("[DBUtil] Lỗi closeStatement: " + e.getMessage());
        }
    }

    public static void closePreparedStatement(Statement ps) {
        closeStatement(ps);
    }

    public static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("[DBUtil] Lỗi closeResultSet: " + e.getMessage());
        }
    }

    // Hủy giao dịch đang dở khi có lỗi
    public static void rollback(Connection c) {
        try {
            if (c != null && !c.getAutoCommit()) {
                c.rollback();
            }
        } catch (SQLException e) {
            System.err.println("[DBUtil] Lỗi rollback: " + e.getMessage());
        }
    }

    // Trả kết nối về chế độ tự commit trước khi đưa lại vào pool
    public static void resetAutoCommit(Connection c) {
        try {
            if (c != null) {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[DBUtil] Lỗi resetAutoCommit: " + e.getMessage());
        }
    }
}
