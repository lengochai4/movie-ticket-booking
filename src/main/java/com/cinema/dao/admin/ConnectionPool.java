package com.cinema.dao.admin;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.postgresql.ds.PGSimpleDataSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Connection pool theo mẫu sách Murach (Chương 12): lấy DataSource "jdbc/cinema" qua JNDI,
 * khai báo trong META-INF/context.xml.
 *
 * Khi Tomcat chạy bằng file context riêng (SmartTomcat trong IntelliJ, conf/Catalina/localhost/...),
 * Tomcat bỏ qua META-INF/context.xml nên không có JNDI. Khi đó tự tạo pool bằng thư viện pool có sẵn
 * của Tomcat (tomcat-dbcp) với thông tin kết nối trong META-INF/persistence.xml.
 */
public class ConnectionPool {

    private static final String JNDI_NAME = "java:comp/env/jdbc/cinema";
    private static final String TOMCAT_DBCP = "org.apache.tomcat.dbcp.dbcp2.BasicDataSource";

    private static ConnectionPool pool = null;
    private static DataSource dataSource = null;

    private ConnectionPool() {
        try {
            InitialContext ic = new InitialContext();
            dataSource = (DataSource) ic.lookup(JNDI_NAME);
            System.out.println("[ConnectionPool] Dùng DataSource JNDI " + JNDI_NAME);
        } catch (NamingException e) {
            dataSource = createTomcatPool();
            System.out.println("[ConnectionPool] Không có JNDI " + JNDI_NAME + ", dùng pool tomcat-dbcp tự tạo");
        }
    }

    public static synchronized ConnectionPool getInstance() {
        if (pool == null) {
            pool = new ConnectionPool();
        }
        return pool;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // Trả kết nối về pool (close() của connection lấy từ pool không đóng kết nối thật)
    public void freeConnection(Connection c) {
        if (c == null) return;
        try {
            c.close();
        } catch (SQLException e) {
            System.err.println("[ConnectionPool] Lỗi freeConnection: " + e.getMessage());
        }
    }

    // Tạo BasicDataSource của tomcat-dbcp (có sẵn trong thư mục lib của Tomcat) bằng reflection
    // để không phải thêm thư viện vào pom.xml
    private static DataSource createTomcatPool() {
        Map<String, String> jdbc = readJdbcSettings();
        Class<?> type;
        try {
            type = Class.forName(TOMCAT_DBCP);
        } catch (ClassNotFoundException e) {
            // Chạy ngoài Tomcat (seeder, chương trình test bằng main): không có tomcat-dbcp,
            // dùng DataSource của driver PostgreSQL (không gom kết nối, chỉ dùng cho công cụ)
            System.out.println("[ConnectionPool] Chạy ngoài Tomcat, dùng PGSimpleDataSource");
            PGSimpleDataSource ds = new PGSimpleDataSource();
            ds.setURL(jdbc.get("url"));
            ds.setUser(jdbc.get("user"));
            ds.setPassword(jdbc.get("password"));
            return ds;
        }
        try {
            Object ds = type.getDeclaredConstructor().newInstance();
            call(type, ds, "setDriverClassName", String.class, jdbc.get("driver"));
            call(type, ds, "setUrl", String.class, jdbc.get("url"));
            call(type, ds, "setUsername", String.class, jdbc.get("user"));
            call(type, ds, "setPassword", String.class, jdbc.get("password"));
            // Driver PostgreSQL nằm trong WEB-INF/lib nên phải nạp bằng classloader của ứng dụng
            call(type, ds, "setDriverClassLoader", ClassLoader.class, ConnectionPool.class.getClassLoader());
            call(type, ds, "setInitialSize", int.class, 1);
            call(type, ds, "setMaxTotal", int.class, 10);
            call(type, ds, "setMaxIdle", int.class, 5);
            call(type, ds, "setValidationQuery", String.class, "SELECT 1");
            call(type, ds, "setTestOnBorrow", boolean.class, true);
            try {
                call(type, ds, "setMaxWait", Duration.class, Duration.ofSeconds(10));
            } catch (ReflectiveOperationException e) {
                call(type, ds, "setMaxWaitMillis", long.class, 10_000L);
            }
            return (DataSource) ds;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Không tạo được connection pool (cần chạy trong Tomcat có tomcat-dbcp)", e);
        }
    }

    private static void call(Class<?> type, Object target, String method, Class<?> paramType, Object value)
            throws ReflectiveOperationException {
        Method m = type.getMethod(method, paramType);
        m.invoke(target, value);
    }

    // Đọc driver/url/user/password từ META-INF/persistence.xml (nguồn cấu hình chung của nhóm)
    private static Map<String, String> readJdbcSettings() {
        Map<String, String> result = new HashMap<>();
        try (InputStream in = ConnectionPool.class.getClassLoader().getResourceAsStream("META-INF/persistence.xml")) {
            if (in == null) {
                throw new IllegalStateException("Không tìm thấy META-INF/persistence.xml");
            }
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
            NodeList props = doc.getElementsByTagName("property");
            for (int i = 0; i < props.getLength(); i++) {
                Element p = (Element) props.item(i);
                String name = p.getAttribute("name");
                if (name.startsWith("jakarta.persistence.jdbc.")) {
                    result.put(name.substring("jakarta.persistence.jdbc.".length()), p.getAttribute("value"));
                }
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Không đọc được cấu hình kết nối trong persistence.xml", e);
        }
        if (result.get("url") == null || result.get("driver") == null) {
            throw new IllegalStateException("persistence.xml thiếu jakarta.persistence.jdbc.url hoặc jdbc.driver");
        }
        return result;
    }
}
