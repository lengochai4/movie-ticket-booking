package com.cinema.util;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Pool kết nối theo kiểu Murach: lấy DataSource JNDI do Tomcat giữ.
 * Tên resource: java:comp/env/jdbc/CinemaDB (khai báo trong META-INF/context.xml).
 * Ngoài Tomcat thì getDataSource() trả null và JPA dùng pool nội bộ trong persistence.xml.
 */
public class ConnectionPool {

    private static DataSource dataSource;

    private ConnectionPool() {
    }

    public static DataSource getDataSource() {
        if (dataSource != null) {
            return dataSource;
        }
        try {
            InitialContext context = new InitialContext();
            dataSource = (DataSource) context.lookup("java:comp/env/jdbc/CinemaDB");
            return dataSource;
        } catch (NamingException e) {
            return null;
        }
    }

    public static Connection getConnection() throws SQLException {
        DataSource source = getDataSource();
        if (source == null) {
            throw new SQLException("Chưa có connection pool jdbc/CinemaDB.");
        }
        return source.getConnection();
    }

    public static void freeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // trả kết nối về pool
            }
        }
    }
}
