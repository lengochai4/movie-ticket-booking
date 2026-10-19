<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Quản Lý Người Dùng - Cinetik Admin" scope="request"/>
<c:set var="extraCss" value="admin-users.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="admin-container">
    <div class="admin-layout">

        <%---- ================= 0. THANH ĐIỀU HƯỚNG QUẢN TRỊ ADMIN ================= ----%>
        <nav style="display: flex; gap: 8px; border-bottom: 1px solid #2d2824; padding-bottom: 12px; overflow-x: auto;">
            <a href="${pageContext.request.contextPath}/admin/dashboard" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                📊 Thống Kê
            </a>
            <a href="${pageContext.request.contextPath}/admin/movies" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                🎬 Quản Lý Phim
            </a>
            <a href="${pageContext.request.contextPath}/admin/showtimes" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                📅 Quản Lý Lịch Chiếu
            </a>
            <a href="${pageContext.request.contextPath}/admin/cinemas" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                🏛️ Quản Lý Rạp & Phòng
            </a>
            <a href="${pageContext.request.contextPath}/admin/combos" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                🍿 Quản Lý Bắp Nước
            </a>
            <a href="${pageContext.request.contextPath}/admin/bookings" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                🎟️ Quản Lý Đặt Vé
            </a>
            <a href="${pageContext.request.contextPath}/admin/users" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: linear-gradient(135deg, rgba(252, 202, 70, 0.2) 0%, rgba(229, 169, 59, 0.08) 100%); border: 1px solid #fcca46; color: #fcca46; font-size: 13px; font-weight: 700; text-decoration: none;">
                👥 Quản Lý Người Dùng
            </a>
        </nav>

        <%---- ================= 1. HEADER TRANG QUẢN TRỊ ================= ----%>
        <header class="admin-header">
            <div class="admin-header-title">
                <h1>Quản Lý Người Dùng</h1>
                <p>Quản trị danh sách tài khoản thành viên, phân quyền hạn và kiểm soát trạng thái truy cập</p>
            </div>
            <div class="admin-header-actions">
                <a href="${pageContext.request.contextPath}/movies" class="btn-header-back">
                    <span>⬅ Quay lại trang chủ</span>
                </a>
            </div>
        </header>

        <%---- ================= 2. THÔNG BÁO HỆ THỐNG ================= ----%>
        <c:set var="msgError" value="${not empty errorMessage ? errorMessage : sessionScope.errorMessage}" />
        <c:set var="msgSuccess" value="${not empty successMessage ? successMessage : sessionScope.successMessage}" />

        <c:if test="${not empty msgError}">
            <div style="background: rgba(120, 0, 0, 0.4); border: 1px solid #c92a2a; color: #ff8787; padding: 12px 18px; border-radius: 8px; font-size: 14px;">
                <span>⚠️ ${msgError}</span>
            </div>
            <c:remove var="errorMessage" scope="session" />
        </c:if>
        <c:if test="${not empty msgSuccess}">
            <div style="background: rgba(43, 138, 62, 0.25); border: 1px solid #2b8a3e; color: #69db7c; padding: 12px 18px; border-radius: 8px; font-size: 14px;">
                <span>✅ ${msgSuccess}</span>
            </div>
            <c:remove var="successMessage" scope="session" />
        </c:if>

        <%---- ================= 3. THỐNG KÊ NHANH (CARDS) ================= ----%>
        <section class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon total">👥</div>
                <div class="stat-info">
                    <span class="stat-value">${not empty totalUsers ? totalUsers : (not empty userList ? userList.size() : 0)}</span>
                    <span class="stat-label">Tổng số tài khoản</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon customer">👤</div>
                <div class="stat-info">
                    <span class="stat-value">${not empty userCount ? userCount : (not empty customerCount ? customerCount : 0)}</span>
                    <span class="stat-label">Người dùng (USER)</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon staff">🛡️</div>
                <div class="stat-info">
                    <span class="stat-value">${not empty staffCount ? staffCount : 0}</span>
                    <span class="stat-label">Nhân viên (STAFF)</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon staff" style="background: rgba(252, 202, 70, 0.1); border-color: rgba(252, 202, 70, 0.3);">👑</div>
                <div class="stat-info">
                    <span class="stat-value">${not empty adminCount ? adminCount : 0}</span>
                    <span class="stat-label">Quản trị viên (ADMIN)</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon locked">🔒</div>
                <div class="stat-info">
                    <span class="stat-value">${not empty lockedCount ? lockedCount : 0}</span>
                    <span class="stat-label">Tài khoản bị khóa</span>
                </div>
            </div>
        </section>

        <%---- ================= 4. THANH TÌM KIẾM & BỘ LỌC ================= ----%>
        <section class="filter-card">
            <form action="${pageContext.request.contextPath}/admin/users" method="GET" class="filter-form">
                <%---- Ô tìm kiếm từ khóa ----%>
                <div class="search-group">
                    <span class="search-icon-inside">🔍</span>
                    <input type="text" 
                           name="keyword" 
                           class="search-input" 
                           placeholder="Tìm kiếm theo họ tên, email, số điện thoại..." 
                           value="${param.keyword}">
                </div>

                <%---- Lọc theo vai trò ----%>
                <select name="role" class="filter-select">
                    <option value="">-- Tất cả vai trò --</option>
                    <option value="ADMIN" ${param.role == 'ADMIN' ? 'selected' : ''}>Quản trị viên (ADMIN)</option>
                    <option value="STAFF" ${param.role == 'STAFF' ? 'selected' : ''}>Nhân viên (STAFF)</option>
                    <option value="USER" ${param.role == 'USER' || param.role == 'CUSTOMER' ? 'selected' : ''}>Người dùng (USER)</option>
                </select>

                <%---- Lọc theo trạng thái ----%>
                <select name="status" class="filter-select">
                    <option value="">-- Tất cả trạng thái --</option>
                    <option value="ACTIVE" ${param.status == 'ACTIVE' ? 'selected' : ''}>Đang hoạt động</option>
                    <option value="LOCKED" ${param.status == 'LOCKED' ? 'selected' : ''}>Đang bị khóa</option>
                </select>

                <button type="submit" class="btn-filter-submit">Lọc Dữ Liệu</button>
                <a href="${pageContext.request.contextPath}/admin/users" class="btn-filter-reset">Đặt lại</a>
            </form>
        </section>

        <%---- ================= 5. BẢNG DANH SÁCH NGƯỜI DÙNG ================= ----%>
        <section class="table-card">
            <div class="table-responsive">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th style="width: 60px;">ID</th>
                            <th>Thành viên</th>
                            <th>Số điện thoại</th>
                            <th>Vai trò</th>
                            <th>Trạng thái</th>
                            <th>Ngày tham gia</th>
                            <th style="text-align: right; width: 220px;">Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty userList}">
                                <c:forEach var="u" items="${userList}">
                                    <tr>
                                        <%---- ID ----%>
                                        <td>#${u.id}</td>

                                        <%---- Người dùng: Avatar + Tên + Email ----%>
                                        <td>
                                            <div class="user-cell">
                                                <div class="user-mini-avatar">
                                                    ${not empty u.fullName ? u.fullName.substring(0, 1).toUpperCase() : 'U'}
                                                </div>
                                                <div class="user-cell-meta">
                                                    <span class="user-full-name">${u.fullName}</span>
                                                    <span class="user-email-text">${u.email}</span>
                                                </div>
                                            </div>
                                        </td>

                                        <%---- Số điện thoại ----%>
                                        <td>${not empty u.phone ? u.phone : '<span style="color: #6e6459;">Chưa cập nhật</span>'}</td>

                                        <%---- Vai trò ----%>
                                        <td>
                                            <span class="badge-role ${u.role}">
                                                <c:choose>
                                                    <c:when test="${u.role == 'ADMIN'}">👑 Quản trị</c:when>
                                                    <c:when test="${u.role == 'STAFF'}">🛡️ Nhân viên</c:when>
                                                    <c:otherwise>👤 Người dùng</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </td>

                                        <%---- Trạng thái (ACTIVE / LOCKED) ----%>
                                        <td>
                                            <span class="badge-status ${u.status}">
                                                <c:choose>
                                                    <c:when test="${u.status == 'ACTIVE'}">Hoạt động</c:when>
                                                    <c:otherwise>Đã bị khóa</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </td>

                                        <%---- Ngày tham gia ----%>
                                        <td>${not empty u.createdAt ? u.createdAt : '15/10/2026'}</td>

                                        <%---- Cột thao tác: Phân quyền & Khóa / Mở ----%>
                                        <td style="text-align: right;">
                                            <div class="action-buttons" style="justify-content: flex-end;">
                                                <%---- Nút mở Modal Phân quyền ----%>
                                                <button type="button" 
                                                        class="btn-action role"
                                                        onclick="openRoleModal('${u.id}', '${u.fullName}', '${u.email}', '${u.role}', '${u.status}')"
                                                        title="Phân quyền hoặc chỉnh sửa">
                                                    🛡️ Phân quyền
                                                </button>

                                                <%---- Form Khóa / Mở khóa ----%>
                                                <form action="${pageContext.request.contextPath}/admin/users/toggle-status" method="POST" style="margin: 0;" onsubmit="return confirmToggle('${u.fullName}', '${u.status}');">
                                                    <input type="hidden" name="userId" value="${u.id}">
                                                    <input type="hidden" name="currentStatus" value="${u.status}">

                                                    <c:choose>
                                                        <c:when test="${u.status == 'ACTIVE'}">
                                                            <button type="submit" class="btn-action lock" title="Khóa tài khoản này">
                                                                🔒 Khóa
                                                            </button>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <button type="submit" class="btn-action unlock" title="Mở khóa tài khoản này">
                                                                🔓 Mở
                                                            </button>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7">
                                        <div class="empty-state">
                                            <span class="empty-state-icon">📂</span>
                                            <p>Không tìm thấy người dùng nào phù hợp với điều kiện tìm kiếm.</p>
                                        </div>
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <%---- PHÂN TRANG (PAGINATION) ----%>
            <div class="pagination-wrapper">
                <span>Hiển thị <strong>${not empty userList ? userList.size() : 0}</strong> tài khoản</span>
                <div class="pagination-controls">
                    <a href="#" class="page-btn">« Trước</a>
                    <a href="#" class="page-btn active">1</a>
                    <a href="#" class="page-btn">2</a>
                    <a href="#" class="page-btn">3</a>
                    <a href="#" class="page-btn">Sau »</a>
                </div>
            </div>
        </section>

    </div>
</main>

<%---- ================= 6. MODAL PHÂN QUYỀN & TRẠNG THÁI ================= ----%>
<div id="roleModal" class="modal-overlay">
    <div class="modal-card">
        <div class="modal-header">
            <h3>Phân Quyền & Quản Lý User</h3>
            <button type="button" class="modal-close-btn" onclick="closeRoleModal()">&times;</button>
        </div>

        <form action="${pageContext.request.contextPath}/admin/users/update-role" method="POST">
            <input type="hidden" id="modalUserId" name="userId">

            <div class="modal-body">
                <div class="modal-form-group">
                    <label>Họ và tên thành viên</label>
                    <input type="text" id="modalUserName" class="modal-input readonly" readonly>
                </div>

                <div class="modal-form-group">
                    <label>Email định danh</label>
                    <input type="email" id="modalUserEmail" class="modal-input readonly" readonly>
                </div>

                <div class="modal-form-group">
                    <label for="modalUserRole">Vai trò / Quyền hạn hệ thống</label>
                    <select id="modalUserRole" name="role" class="modal-input" required>
                        <option value="USER">Người dùng thành viên (USER)</option>
                        <option value="STAFF">Nhân viên rạp (STAFF)</option>
                        <option value="ADMIN">Quản trị viên toàn hệ thống (ADMIN)</option>
                    </select>
                </div>

                <div class="modal-form-group">
                    <label for="modalUserStatus">Trạng thái tài khoản</label>
                    <select id="modalUserStatus" name="status" class="modal-input" required>
                        <option value="ACTIVE">Đang hoạt động (ACTIVE)</option>
                        <option value="LOCKED">Đã khóa tài khoản (LOCKED)</option>
                    </select>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn-modal-cancel" onclick="closeRoleModal()">Hủy bỏ</button>
                <button type="submit" class="btn-modal-save">Lưu Cập Nhật</button>
            </div>
        </form>
    </div>
</div>

<script>
    // Hàm mở Modal phân quyền
    function openRoleModal(id, name, email, role, status) {
        document.getElementById('modalUserId').value = id;
        document.getElementById('modalUserName').value = name;
        document.getElementById('modalUserEmail').value = email;
        document.getElementById('modalUserRole').value = role;
        document.getElementById('modalUserStatus').value = status;

        document.getElementById('roleModal').classList.add('show');
    }

    // Hàm đóng Modal
    function closeRoleModal() {
        document.getElementById('roleModal').classList.remove('show');
    }

    // Đóng khi click ngoài khung modal
    window.addEventListener('click', function(e) {
        const modal = document.getElementById('roleModal');
        if (e.target === modal) {
            closeRoleModal();
        }
    });

    // Xác nhận khi ấn Khóa hoặc Mở khóa
    function confirmToggle(name, status) {
        const actionText = (status === 'ACTIVE') ? 'KHÓA' : 'MỞ KHÓA';
        return confirm('Bạn có chắc chắn muốn ' + actionText + ' tài khoản của [' + name + '] không?');
    }
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
