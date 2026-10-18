package com.cinema.controller.catalog;

import com.cinema.dao.admin.ComboDAO;
import com.cinema.entity.Combo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@WebServlet(urlPatterns = {"/admin/combos", "/admin/combos/save", "/admin/combos/delete"})
public class AdminComboServlet extends HttpServlet {

    private static final List<String> CATEGORIES = Arrays.asList("Combo", "Bắp Rang", "Nước", "Đặc Biệt");
    private static final List<String> STATUSES = Arrays.asList(Combo.STATUS_ACTIVE, Combo.STATUS_OUT_OF_STOCK);

    private final ComboDAO comboDAO = new ComboDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        List<Combo> comboList = comboDAO.findAll();

        // Thống kê nhanh từ dữ liệu thật
        int activeCount = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Combo c : comboList) {
            if (c.isActive()) {
                activeCount++;
            }
            if (c.getPrice() != null) {
                totalPrice = totalPrice.add(c.getPrice());
            }
        }
        BigDecimal avgPrice = comboList.isEmpty()
                ? BigDecimal.ZERO
                : totalPrice.divide(BigDecimal.valueOf(comboList.size()), 0, RoundingMode.HALF_UP);

        request.setAttribute("comboList", comboList);
        request.setAttribute("totalCombos", comboList.size());
        request.setAttribute("activeCombos", activeCount);
        request.setAttribute("outOfStockCombos", comboList.size() - activeCount);
        request.setAttribute("avgComboPrice", avgPrice);

        // Chuyển tiếp tới giao diện quản lý combo bắp nước
        request.getRequestDispatcher("/WEB-INF/views/admin/combos.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        if ("/admin/combos/save".equals(path)) {
            handleSave(request);
            response.sendRedirect(request.getContextPath() + "/admin/combos");
            return;
        } else if ("/admin/combos/delete".equals(path)) {
            handleDelete(request);
            response.sendRedirect(request.getContextPath() + "/admin/combos");
            return;
        }

        doGet(request, response);
    }

    private void handleSave(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String comboId = trimToNull(request.getParameter("comboID"));
        String name = trimToNull(request.getParameter("name"));
        String category = trimToNull(request.getParameter("category"));
        String priceStr = trimToNull(request.getParameter("price"));
        String status = trimToNull(request.getParameter("status"));
        String imageURL = trimToNull(request.getParameter("imageUrl"));
        String description = trimToNull(request.getParameter("description"));

        // 1. Kiểm tra dữ liệu đầu vào
        if (name == null) {
            session.setAttribute("errorMessage", "Tên combo không được để trống.");
            return;
        }
        BigDecimal price = parsePrice(priceStr);
        if (price == null || price.signum() <= 0) {
            session.setAttribute("errorMessage", "Giá bán không hợp lệ. Vui lòng nhập số tiền lớn hơn 0 (VD: 89.000).");
            return;
        }
        if (category == null || !CATEGORIES.contains(category)) {
            session.setAttribute("errorMessage", "Danh mục combo không hợp lệ.");
            return;
        }
        if (status == null || !STATUSES.contains(status)) {
            status = Combo.STATUS_ACTIVE;
        }

        // 2. Lưu vào database
        try {
            if (comboId != null) {
                Combo combo = comboDAO.findById(comboId);
                if (combo == null) {
                    session.setAttribute("errorMessage", "Combo cần sửa không còn tồn tại.");
                    return;
                }
                combo.setName(name);
                combo.setCategory(category);
                combo.setPrice(price);
                combo.setStatus(status);
                combo.setImageURL(imageURL);
                combo.setDescription(description);
                comboDAO.update(combo);
                System.out.println("[ADMIN COMBO] Đã cập nhật combo: " + name + " (ID: " + comboId + ")");
                session.setAttribute("successMessage", "Đã cập nhật combo \"" + name + "\".");
            } else {
                Combo combo = new Combo(name, description, price, category, imageURL, status);
                comboDAO.insert(combo);
                System.out.println("[ADMIN COMBO] Đã thêm combo mới: " + name);
                session.setAttribute("successMessage", "Đã thêm combo \"" + name + "\".");
            }
        } catch (RuntimeException e) {
            System.err.println("[ADMIN COMBO] Lỗi khi lưu combo: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("errorMessage", "Không thể lưu combo do lỗi hệ thống. Vui lòng thử lại.");
        }
    }

    private void handleDelete(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String comboId = trimToNull(request.getParameter("comboID"));
        if (comboId == null) {
            session.setAttribute("errorMessage", "Thiếu mã combo cần xóa.");
            return;
        }
        try {
            comboDAO.delete(comboId);
            System.out.println("[ADMIN COMBO] Đã xóa combo ID: " + comboId);
            session.setAttribute("successMessage", "Đã xóa combo thành công.");
        } catch (RuntimeException e) {
            System.err.println("[ADMIN COMBO] Lỗi khi xóa combo: " + e.getMessage());
            if (isForeignKeyViolation(e)) {
                session.setAttribute("errorMessage",
                        "Không thể xóa combo đã có trong đơn đặt vé. Hãy chuyển combo sang \"Tạm hết hàng\" thay vì xóa.");
            } else {
                e.printStackTrace();
                session.setAttribute("errorMessage", "Không thể xóa combo do lỗi hệ thống. Vui lòng thử lại.");
            }
        }
    }

    // Nhận giá dạng "89.000", "89,000", "89000" hoặc "89.000 đ" -> 89000
    private BigDecimal parsePrice(String priceStr) {
        if (priceStr == null) return null;
        String digits = priceStr.replaceAll("[^0-9]", "");
        if (digits.isEmpty() || digits.length() > 9) return null;
        return new BigDecimal(digits);
    }

    // PostgreSQL trả SQLState 23503 khi vi phạm khóa ngoại (bản ghi đang được bảng khác tham chiếu)
    private boolean isForeignKeyViolation(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SQLException && "23503".equals(((SQLException) t).getSQLState())) {
                return true;
            }
        }
        return false;
    }

    private String trimToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
