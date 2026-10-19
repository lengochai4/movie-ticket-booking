package com.cinema.dao.admin;

import com.cinema.dto.ComboDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * CRUD combo bắp nước (JDBC: ConnectionPool + PreparedStatement), bảng combos.
 */
public class ComboDAO {

    private static final String SELECT_COMBO =
            "SELECT comboid, name, description, price, category, imageurl, status FROM combos ";

    public List<ComboDTO> findAll() {
        return query(SELECT_COMBO + "ORDER BY category, price");
    }

    // Danh sách combo đang mở bán, dùng cho bước chọn combo khi đặt vé (module của Hải)
    public List<ComboDTO> findActive() {
        return query(SELECT_COMBO + "WHERE status = ? ORDER BY category, price", ComboDTO.STATUS_ACTIVE);
    }

    public ComboDTO findById(String comboId) {
        List<ComboDTO> list = query(SELECT_COMBO + "WHERE comboid = ?", comboId);
        return list.isEmpty() ? null : list.get(0);
    }

    public void insert(ComboDTO combo) {
        if (combo.getComboID() == null) {
            combo.setComboID(UUID.randomUUID().toString());
        }
        write("INSERT INTO combos (name, description, price, category, imageurl, status, comboid) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)", combo, "insert");
    }

    public void update(ComboDTO combo) {
        write("UPDATE combos SET name = ?, description = ?, price = ?, category = ?, imageurl = ?, status = ? "
                + "WHERE comboid = ?", combo, "update");
    }

    // Trả về false nếu không có bản ghi nào bị xóa (mã không tồn tại)
    public boolean delete(String comboId) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement("DELETE FROM combos WHERE comboid = ?");
            ps.setString(1, comboId);
            int deleted = ps.executeUpdate();
            JpaCacheSync.evictCombos();
            return deleted > 0;
        } catch (SQLException e) {
            System.err.println("[ComboDAO] Lỗi delete: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    // Thêm/sửa: thứ tự tham số giống nhau cho cả INSERT và UPDATE
    private void write(String sql, ComboDTO c, String action) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setString(1, c.getName());
            ps.setString(2, c.getDescription());
            ps.setBigDecimal(3, c.getPrice());
            ps.setString(4, c.getCategory());
            ps.setString(5, c.getImageURL());
            ps.setString(6, c.getStatus());
            ps.setString(7, c.getComboID());
            ps.executeUpdate();
            JpaCacheSync.evictCombos();
        } catch (SQLException e) {
            System.err.println("[ComboDAO] Lỗi " + action + ": " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    private List<ComboDTO> query(String sql, String... params) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = pool.getConnection();
            ps = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            rs = ps.executeQuery();
            List<ComboDTO> list = new ArrayList<>();
            while (rs.next()) {
                ComboDTO c = new ComboDTO();
                c.setComboID(rs.getString("comboid"));
                c.setName(rs.getString("name"));
                c.setDescription(rs.getString("description"));
                c.setPrice(rs.getBigDecimal("price"));
                c.setCategory(rs.getString("category"));
                c.setImageURL(rs.getString("imageurl"));
                c.setStatus(rs.getString("status"));
                list.add(c);
            }
            return list;
        } catch (SQLException e) {
            System.err.println("[ComboDAO] Lỗi truy vấn: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }
}
