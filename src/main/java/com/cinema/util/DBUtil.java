package com.cinema.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBUtil - Tiện ích kết nối cơ sở dữ liệu MySQL.
 * Load cấu hình từ db.properties trên classpath.
 */
public class DBUtil {

    private static final String PROPERTIES_FILE = "db.properties";
    private static Properties props;

    static {
        props = new Properties();
        try (InputStream is = DBUtil.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (is == null) {
                throw new RuntimeException("Không tìm thấy file: " + PROPERTIES_FILE + " trên classpath.");
            }
            props.load(is);
            Class.forName(props.getProperty("db.driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Lỗi khởi tạo DBUtil: " + e.getMessage(), e);
        }
    }

    /**
     * Mở kết nối JDBC tới cơ sở dữ liệu.
     *
     * @return Connection đang mở
     * @throws SQLException nếu kết nối thất bại
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.username"),
                props.getProperty("db.password")
        );
    }

    /**
     * Đóng kết nối an toàn (không ném ngoại lệ).
     *
     * @param conn Connection cần đóng (có thể null)
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("[DBUtil] Lỗi khi đóng kết nối: " + e.getMessage());
            }
        }
    }
}
