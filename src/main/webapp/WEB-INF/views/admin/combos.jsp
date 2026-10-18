<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Quản Lý Combo Bắp Nước - Cinetik Admin" scope="request"/>
<c:set var="extraCss" value="admin-users.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<style>

.combo-view-toggle {
    display: inline-flex;
    background: #1e1b18;
    border: 1px solid #3c3733;
    border-radius: 8px;
    padding: 3px;
    gap: 4px;
}
.view-toggle-btn {
    padding: 8px 16px;
    border: none;
    border-radius: 6px;
    background: transparent;
    color: #b5a995;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    transition: all 0.2s ease;
}
.view-toggle-btn.active {
    background: linear-gradient(135deg, #d49a24 0%, #b8860b 100%);
    color: #0d0b0a;
    font-weight: 700;
    box-shadow: 0 2px 8px rgba(212, 154, 36, 0.3);
}

/* Lưới Thẻ Combo (Grid View) */
.combo-grid-container {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
}
.combo-admin-card {
    background: #161413;
    border: 1px solid rgba(252, 202, 70, 0.2);
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
    display: flex;
    flex-direction: column;
    transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}
.combo-admin-card:hover {
    transform: translateY(-3px);
    border-color: rgba(252, 202, 70, 0.5);
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.7);
}
.combo-card-image-wrap {
    position: relative;
    width: 100%;
    height: 180px;
    background: #23201e;
    overflow: hidden;
}
.combo-card-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
}
.combo-admin-card:hover .combo-card-image {
    transform: scale(1.05);
}
.combo-card-price-badge {
    position: absolute;
    bottom: 12px;
    right: 12px;
    background: rgba(18, 16, 14, 0.9);
    border: 1px solid #fcca46;
    color: #fcca46;
    font-size: 15px;
    font-weight: 800;
    padding: 5px 12px;
    border-radius: 20px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.6);
}
.combo-card-cat-badge {
    position: absolute;
    top: 12px;
    left: 12px;
    background: rgba(120, 0, 0, 0.85);
    border: 1px solid rgba(252, 202, 70, 0.4);
    color: #f6ead2;
    font-size: 11px;
    font-weight: 700;
    text-transform: uppercase;
    padding: 4px 10px;
    border-radius: 4px;
    letter-spacing: 0.5px;
}
.combo-card-body {
    padding: 16px;
    display: flex;
    flex-direction: column;
    flex: 1;
}
.combo-card-title {
    font-size: 16px;
    font-weight: 700;
    color: #f6ead2;
    margin-bottom: 6px;
}
.combo-card-desc {
    font-size: 12.5px;
    color: #b5a995;
    line-height: 1.5;
    flex: 1;
    margin-bottom: 14px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}
.combo-card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 1px solid #23201e;
    padding-top: 12px;
}

/* Modal Live Preview */
.combo-preview-box {
    width: 110px;
    height: 110px;
    border-radius: 8px;
    background: #1c1816;
    border: 1px dashed rgba(252, 202, 70, 0.4);
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}
.combo-preview-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

/* Nút Hành Động trong bảng Admin (Sửa, Xóa) */
.table-actions-cell {
    text-align: center;
    white-space: nowrap;
}
.table-actions-wrap {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    justify-content: center;
}
.btn-table-action {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding: 7px 13px;
    border-radius: 6px;
    font-size: 13px;
    font-weight: 600;
    font-family: inherit;
    cursor: pointer;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    white-space: nowrap;
    line-height: 1;
    border: 1px solid transparent;
    outline: none;
    text-decoration: none;
}
.btn-table-action svg {
    flex-shrink: 0;
    transition: transform 0.2s ease;
}
.btn-table-action.edit {
    background: rgba(252, 202, 70, 0.12) !important;
    color: #fcca46 !important;
    border-color: rgba(252, 202, 70, 0.35) !important;
}
.btn-table-action.edit:hover {
    background: #fcca46 !important;
    color: #12100e !important;
    border-color: #fcca46 !important;
    box-shadow: 0 0 12px rgba(252, 202, 70, 0.45) !important;
    transform: translateY(-1px);
}
.btn-table-action.edit:hover svg {
    transform: scale(1.1);
}
.btn-table-action.delete {
    background: rgba(239, 68, 68, 0.12) !important;
    color: #f87171 !important;
    border-color: rgba(239, 68, 68, 0.35) !important;
}
.btn-table-action.delete:hover {
    background: #ef4444 !important;
    color: #ffffff !important;
    border-color: #ef4444 !important;
    box-shadow: 0 0 12px rgba(239, 68, 68, 0.45) !important;
    transform: translateY(-1px);
}
.btn-table-action.delete:hover svg {
    transform: scale(1.1);
}

/* Modal Quản Trị Cinetik */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.78);
    backdrop-filter: blur(5px);
    display: none;
    align-items: center;
    justify-content: center;
    z-index: 99999;
    padding: 20px;
    box-sizing: border-box;
}
.modal-overlay.active,
.modal-overlay.show {
    display: flex !important;
}
.modal-box {
    background: #181513;
    border: 1px solid rgba(252, 202, 70, 0.35);
    border-radius: 12px;
    width: 100%;
    max-width: 650px;
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.9);
    overflow: hidden;
    position: relative;
    animation: modalPop 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}
@keyframes modalPop {
    0% { opacity: 0; transform: scale(0.95); }
    100% { opacity: 1; transform: scale(1); }
}
.modal-header {
    padding: 18px 24px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #1f1b18;
}
.modal-header h3 {
    margin: 0;
    font-size: 18px;
    color: #fcca46;
    font-family: inherit;
    font-weight: 700;
}
.btn-close-modal {
    background: transparent;
    border: none;
    color: #8c8273;
    font-size: 20px;
    cursor: pointer;
    line-height: 1;
    padding: 4px;
    transition: color 0.2s;
}
.btn-close-modal:hover {
    color: #fcca46;
}
.modal-body {
    padding: 24px;
    max-height: calc(85vh - 140px);
    overflow-y: auto;
}
.form-group {
    display: flex;
    flex-direction: column;
    gap: 6px;
    margin-bottom: 4px;
}
.form-label {
    font-size: 13px;
    font-weight: 600;
    color: #e4d8c5;
}
.form-input, .form-select {
    width: 100%;
    background: #23201e;
    border: 1px solid #3c3733;
    border-radius: 6px;
    padding: 10px 14px;
    color: #f6ead2;
    font-size: 14px;
    font-family: inherit;
    outline: none;
    box-sizing: border-box;
    transition: border-color 0.2s, box-shadow 0.2s;
}
.form-input:focus, .form-select:focus {
    border-color: #fcca46;
    box-shadow: 0 0 8px rgba(252, 202, 70, 0.25);
}
.modal-footer {
    padding: 16px 24px;
    border-top: 1px solid rgba(255, 255, 255, 0.08);
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    background: #1f1b18;
}
.btn-cancel {
    padding: 9px 18px;
    border-radius: 6px;
    background: transparent;
    border: 1px solid #3c3733;
    color: #b5a995;
    font-size: 13.5px;
    font-weight: 500;
    cursor: pointer;
    font-family: inherit;
    transition: all 0.2s;
}
.btn-cancel:hover {
    border-color: #8c8273;
    color: #f6ead2;
}
.btn-confirm {
    padding: 9px 22px;
    border-radius: 6px;
    background: linear-gradient(135deg, #d49a24 0%, #b8860b 100%);
    border: none;
    color: #0d0b0a;
    font-size: 13.5px;
    font-weight: 700;
    cursor: pointer;
    font-family: inherit;
    box-shadow: 0 2px 10px rgba(212, 154, 36, 0.35);
    transition: all 0.2s;
}
.btn-confirm:hover {
    filter: brightness(1.1);
    transform: translateY(-1px);
}
</style>

<main class="admin-container">
    <div class="admin-layout" style="max-width: 1280px;">

        <%---- THANH ĐIỀU HƯỚNG QUẢN TRỊ ADMIN ----%>
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
            <a href="${pageContext.request.contextPath}/admin/combos" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: linear-gradient(135deg, rgba(252, 202, 70, 0.2) 0%, rgba(229, 169, 59, 0.08) 100%); border: 1px solid #fcca46; color: #fcca46; font-size: 13px; font-weight: 700; text-decoration: none;">
                🍿 Quản Lý Bắp Nước
            </a>
            <a href="${pageContext.request.contextPath}/admin/bookings" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                🎟️ Quản Lý Đặt Vé
            </a>
            <a href="${pageContext.request.contextPath}/admin/users" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                👥 Quản Lý Người Dùng
            </a>
        </nav>

        <%---- 1. HEADER TRANG COMBO BẮP NƯỚC ----%>
        <header class="admin-header" style="align-items: center;">
            <div class="admin-header-title">
                <h1>Quản Lý Combo Bắp Nước</h1>
                <p>Thiết lập giá niêm yết, tải lên hình ảnh món ăn, chỉnh sửa mô tả combo và quản lý tồn kho tại quầy</p>
            </div>
            <div class="admin-header-actions" style="display: flex; gap: 12px; align-items: center;">
                <%---- Bộ nút chuyển chế độ xem: Dạng thẻ vs Dạng bảng ----%>
                <div class="combo-view-toggle">
                    <button type="button" class="view-toggle-btn active" id="btnViewGrid" onclick="switchComboView('grid')">
                        🎴 Dạng Thẻ
                    </button>
                    <button type="button" class="view-toggle-btn" id="btnViewTable" onclick="switchComboView('table')">
                        📋 Dạng Bảng
                    </button>
                </div>

                <%---- Nút Mở Modal Thêm Combo -----%>
                <button type="button" class="btn-filter-submit" style="display: flex; align-items: center; gap: 6px; padding: 10px 20px; font-weight: 700;" onclick="openAddComboModal()">
                    ➕ Thêm Combo Mới
                </button>
            </div>
        </header>

        <%---- THẺ THỐNG KÊ NHANH (KPIS) ----%>
        <section class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon total">🍿</div>
                <div class="stat-info">
                    <span class="stat-value" id="statTotalCombos">${not empty totalCombos ? totalCombos : 0} Món</span>
                    <span class="stat-label">Tổng số combo & món lẻ</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon customer" style="background: rgba(34, 197, 94, 0.15); color: #4ade80;">🟢</div>
                <div class="stat-info">
                    <span class="stat-value" id="statActiveCombos">${not empty activeCombos ? activeCombos : 0} Món</span>
                    <span class="stat-label">Đang mở bán</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon staff" style="background: rgba(239, 68, 68, 0.15); color: #f87171;">🔴</div>
                <div class="stat-info">
                    <span class="stat-value" id="statOutOfStock">${not empty outOfStockCombos ? outOfStockCombos : 0} Món</span>
                    <span class="stat-label">Tạm hết hàng</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon" style="background: rgba(56, 189, 248, 0.15); color: #38bdf8;">🏷️</div>
                <div class="stat-info">
                    <span class="stat-value" id="statAvgPrice"><fmt:formatNumber value="${not empty avgComboPrice ? avgComboPrice : 0}" type="number" maxFractionDigits="0"/> đ</span>
                    <span class="stat-label">Giá combo trung bình</span>
                </div>
            </div>
        </section>

        <%---- THÔNG BÁO HỆ THỐNG (flash message từ session) ----%>
        <c:set var="msgError" value="${not empty errorMessage ? errorMessage : sessionScope.errorMessage}" />
        <c:set var="msgSuccess" value="${not empty successMessage ? successMessage : sessionScope.successMessage}" />

        <c:if test="${not empty msgError}">
            <div style="background: rgba(120, 0, 0, 0.4); border: 1px solid #c92a2a; color: #ff8787; padding: 12px 18px; border-radius: 8px; font-size: 14px;">
                <span>⚠️ <c:out value="${msgError}"/></span>
            </div>
            <c:remove var="errorMessage" scope="session" />
        </c:if>
        <c:if test="${not empty msgSuccess}">
            <div style="background: rgba(43, 138, 62, 0.25); border: 1px solid #2b8a3e; color: #69db7c; padding: 12px 18px; border-radius: 8px; font-size: 14px;">
                <span>✅ <c:out value="${msgSuccess}"/></span>
            </div>
            <c:remove var="successMessage" scope="session" />
        </c:if>

        <%---- BỘ LỌC & TÌM KIẾM COMBO ----%>
        <section class="filter-card">
            <form id="filterComboForm" class="filter-form" onsubmit="event.preventDefault(); applyComboFilter();">
                <%---- Ô tìm kiếm tên combo hoặc mô tả ----%>
                <div class="search-group" style="flex: 2;">
                    <span class="search-icon-inside">🔍</span>
                    <input type="text" 
                           id="filterKeyword" 
                           class="search-input" 
                           placeholder="Tìm kiếm theo tên combo, bắp ngọt, caramel, nước ngọt..." 
                           oninput="applyComboFilter()">
                </div>

                <%---- Lọc theo Danh mục ----%>
                <select id="filterCategory" class="filter-select" onchange="applyComboFilter()">
                    <option value="">-- Tất cả danh mục --</option>
                    <option value="Combo">Combo Tiết Kiệm</option>
                    <option value="Bắp Rang">Bắp Rang Bơ</option>
                    <option value="Nước">Nước Giải Khát</option>
                    <option value="Đặc Biệt">Combo Đặc Biệt / VIP</option>
                </select>

                <%---- Lọc theo Trạng thái ----%>
                <select id="filterStatus" class="filter-select" onchange="applyComboFilter()">
                    <option value="">-- Tất cả trạng thái --</option>
                    <option value="ACTIVE">Đang bán</option>
                    <option value="OUT_OF_STOCK">Tạm hết hàng</option>
                </select>

                <button type="button" class="btn-filter-reset" onclick="resetComboFilters()">Đặt lại</button>
            </form>
        </section>

        <%---- CHẾ ĐỘ 1: DẠNG THẺ MENU (GRID VIEW) ----%>
        <section id="comboGridSection" class="combo-grid-container">
            <c:forEach items="${comboList}" var="cb">
            <div class="combo-admin-card" data-name="<c:out value='${cb.name}'/>" data-cat="<c:out value='${cb.category}'/>" data-status="${cb.status}">
                <div class="combo-card-image-wrap">
                    <img src="<c:out value='${not empty cb.imageURL ? cb.imageURL : "https://images.unsplash.com/photo-1572177191856-3cde618dee1f?w=400&auto=format&fit=crop"}'/>"
                         alt="<c:out value='${cb.name}'/>" class="combo-card-image">
                    <span class="combo-card-cat-badge"><c:out value="${cb.category}"/></span>
                    <span class="combo-card-price-badge"><fmt:formatNumber value="${cb.price}" type="number" maxFractionDigits="0"/> đ</span>
                </div>
                <div class="combo-card-body">
                    <div class="combo-card-title"><c:out value="${cb.name}"/></div>
                    <div class="combo-card-desc"><c:out value="${cb.description}"/></div>
                    <div class="combo-card-footer">
                        <c:choose>
                            <c:when test="${cb.status == 'ACTIVE'}">
                                <span class="badge-status" style="background: rgba(34, 197, 94, 0.15); color: #4ade80; border: 1px solid rgba(34, 197, 94, 0.3);">● Đang bán</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge-status" style="background: rgba(239, 68, 68, 0.15); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.3);">● Tạm hết hàng</span>
                            </c:otherwise>
                        </c:choose>
                        <div class="table-actions-wrap">
                            <button type="button" class="btn-table-action edit" title="Sửa combo"
                                    data-id="${cb.comboID}" data-name="<c:out value='${cb.name}'/>" data-cat="<c:out value='${cb.category}'/>"
                                    data-price="<fmt:formatNumber value='${cb.price}' groupingUsed='false' maxFractionDigits='0'/>"
                                    data-img="<c:out value='${cb.imageURL}'/>" data-desc="<c:out value='${cb.description}'/>" data-status="${cb.status}"
                                    onclick="const d = this.dataset; openEditComboModal(d.id, d.name, d.cat, d.price, d.img, d.desc, d.status)">
                                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                                </svg>
                                <span>Sửa</span>
                            </button>
                            <button type="button" class="btn-table-action delete" title="Xóa combo"
                                    data-id="${cb.comboID}" data-name="<c:out value='${cb.name}'/>"
                                    onclick="openDeleteComboModal(this.dataset.id, this.dataset.name)">
                                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                                    <polyline points="3 6 5 6 21 6"></polyline>
                                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                                    <line x1="10" y1="11" x2="10" y2="17"></line>
                                    <line x1="14" y1="11" x2="14" y2="17"></line>
                                </svg>
                                <span>Xóa</span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            </c:forEach>

            <c:if test="${empty comboList}">
                <div style="grid-column: 1 / -1; text-align: center; padding: 45px 20px; color: #94a3b8; font-size: 14px;">
                    🍿 Chưa có combo nào. Bấm "Thêm Combo" để tạo món đầu tiên.
                </div>
            </c:if>

        </section>

        <%---- CHẾ ĐỘ 2: DẠNG BẢNG CHI TIẾT (TABLE VIEW) ----%>
        <section class="table-card" id="comboTableSection" style="display: none;">
            <div class="table-responsive">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th style="width: 90px;">MÃ MÓN</th>
                            <th style="width: 100px;">HÌNH ẢNH</th>
                            <th style="width: 220px;">TÊN COMBO / MÓN</th>
                            <th style="width: 140px;">DANH MỤC</th>
                            <th>MÔ TẢ THÀNH PHẦN</th>
                            <th style="width: 130px;">GIÁ BÁN (VND)</th>
                            <th style="width: 130px;">TRẠNG THÁI</th>
                            <th style="width: 140px; text-align: center;">THAO TÁC</th>
                        </tr>
                    </thead>
                    <tbody id="comboTableBody">
                        <c:forEach items="${comboList}" var="cb">
                        <tr data-name="<c:out value='${cb.name}'/>" data-cat="<c:out value='${cb.category}'/>" data-status="${cb.status}">
                            <td><strong style="color: #fcca46;" title="${cb.comboID}">#${fn:substring(cb.comboID, 0, 8)}</strong></td>
                            <td>
                                <img src="<c:out value='${not empty cb.imageURL ? cb.imageURL : "https://images.unsplash.com/photo-1572177191856-3cde618dee1f?w=400&auto=format&fit=crop"}'/>"
                                     alt="<c:out value='${cb.name}'/>" style="width: 55px; height: 55px; object-fit: cover; border-radius: 6px; border: 1px solid #3c3733;">
                            </td>
                            <td><strong style="color: #f6ead2;"><c:out value="${cb.name}"/></strong></td>
                            <td><span class="badge-role" style="background: rgba(120, 0, 0, 0.4); color: #fcca46; border: 1px solid #fcca46;"><c:out value="${cb.category}"/></span></td>
                            <td style="color: #b5a995; font-size: 13px;"><c:out value="${cb.description}"/></td>
                            <td><strong style="color: #fcca46; font-size: 14.5px;"><fmt:formatNumber value="${cb.price}" type="number" maxFractionDigits="0"/> đ</strong></td>
                            <td>
                                <c:choose>
                            <c:when test="${cb.status == 'ACTIVE'}">
                                <span class="badge-status" style="background: rgba(34, 197, 94, 0.15); color: #4ade80; border: 1px solid rgba(34, 197, 94, 0.3);">● Đang bán</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge-status" style="background: rgba(239, 68, 68, 0.15); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.3);">● Tạm hết hàng</span>
                            </c:otherwise>
                        </c:choose>
                            </td>
                            <td class="table-actions-cell">
                                <div class="table-actions-wrap">
                                    <button type="button" class="btn-table-action edit" title="Sửa combo"
                                            data-id="${cb.comboID}" data-name="<c:out value='${cb.name}'/>" data-cat="<c:out value='${cb.category}'/>"
                                    data-price="<fmt:formatNumber value='${cb.price}' groupingUsed='false' maxFractionDigits='0'/>"
                                    data-img="<c:out value='${cb.imageURL}'/>" data-desc="<c:out value='${cb.description}'/>" data-status="${cb.status}"
                                            onclick="const d = this.dataset; openEditComboModal(d.id, d.name, d.cat, d.price, d.img, d.desc, d.status)">
                                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                                </svg>
                                        <span>Sửa</span>
                                    </button>
                                    <button type="button" class="btn-table-action delete" title="Xóa combo"
                                            data-id="${cb.comboID}" data-name="<c:out value='${cb.name}'/>"
                                            onclick="openDeleteComboModal(this.dataset.id, this.dataset.name)">
                                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                                    <polyline points="3 6 5 6 21 6"></polyline>
                                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                                    <line x1="10" y1="11" x2="10" y2="17"></line>
                                    <line x1="14" y1="11" x2="14" y2="17"></line>
                                </svg>
                                        <span>Xóa</span>
                                    </button>
                                </div>
                            </td>
                        </tr>
                        </c:forEach>

                        <c:if test="${empty comboList}">
                            <tr>
                                <td colspan="8" style="text-align: center; padding: 30px; color: #94a3b8;">Chưa có combo nào.</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </section>

    </div>
</main>

<%---- MODAL THÊM / CHỈNH SỬA COMBO BẮP NƯỚC ----%>
<div class="modal-overlay" id="comboModal">
    <div class="modal-box" style="max-width: 650px;">
        <div class="modal-header">
            <h3 id="comboModalTitle" style="display: flex; align-items: center; gap: 8px;">
                🍿 Thêm Combo Bắp Nước Mới
            </h3>
            <button type="button" class="btn-close-modal" onclick="closeComboModal()">✕</button>
        </div>

        <form action="${pageContext.request.contextPath}/admin/combos/save" method="POST" id="comboForm">
            <input type="hidden" name="comboID" id="inputComboID">

            <div class="modal-body" style="display: flex; flex-direction: column; gap: 16px;">
                
                <%---- Tên combo & Danh mục ----%>
                <div style="display: grid; grid-template-columns: 1.5fr 1fr; gap: 14px;">
                    <div class="form-group">
                        <label class="form-label">Tên Combo / Món Ăn <span style="color: #ef4444;">*</span></label>
                        <input type="text" name="name" id="inputComboName" class="form-input" placeholder="Ví dụ: Combo Siêu Tiết Kiệm..." required>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Danh Mục <span style="color: #ef4444;">*</span></label>
                        <select name="category" id="inputComboCategory" class="form-select" required>
                            <option value="Combo">Combo Tiết Kiệm</option>
                            <option value="Bắp Rang">Bắp Rang Bơ</option>
                            <option value="Nước">Nước Giải Khát</option>
                            <option value="Đặc Biệt">Combo Đặc Biệt / VIP</option>
                        </select>
                    </div>
                </div>

                <%---- Giá bán niêm yết & Trạng thái ----%>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
                    <div class="form-group">
                        <label class="form-label">Giá Bán Niêm Yết (VND) <span style="color: #ef4444;">*</span></label>
                        <input type="text" name="price" id="inputComboPrice" class="form-input" placeholder="Ví dụ: 89.000" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Trạng Thái Kinh Doanh <span style="color: #ef4444;">*</span></label>
                        <select name="status" id="inputComboStatus" class="form-select" required>
                            <option value="ACTIVE">🟢 Đang mở bán</option>
                            <option value="OUT_OF_STOCK">🔴 Tạm hết hàng</option>
                        </select>
                    </div>
                </div>

                <%---- URL Hình ảnh món ăn kèm Live Preview ----%>
                <div class="form-group">
                    <label class="form-label">URL Hình Ảnh Món Ăn <span style="color: #ef4444;">*</span></label>
                    <div style="display: flex; gap: 14px; align-items: center;">
                        <div class="combo-preview-box">
                            <img id="comboImagePreview" src="https://images.unsplash.com/photo-1572177191856-3cde618dee1f?w=400&auto=format&fit=crop" 
                                 alt="Preview" class="combo-preview-img" onerror="this.src='https://images.unsplash.com/photo-1572177191856-3cde618dee1f?w=400&auto=format&fit=crop'">
                        </div>
                        <div style="flex: 1;">
                            <input type="url" name="imageUrl" id="inputComboImageUrl" class="form-input" 
                                   placeholder="Dán link ảnh bắp nước (https://...)" 
                                   value="https://images.unsplash.com/photo-1572177191856-3cde618dee1f?w=400&auto=format&fit=crop"
                                   required oninput="updateComboPreview(this.value)">
                            <div style="font-size: 11.5px; color: #8c8273; margin-top: 5px;">Hỗ trợ ảnh định dạng JPG, PNG hoặc WebP sắc nét, tỷ lệ ngang hoặc vuông.</div>
                        </div>
                    </div>
                </div>

                <%---- Mô tả thành phần combo ----%>
                <div class="form-group">
                    <label class="form-label">Mô Tả Thành Phần & Chi Tiết Combo <span style="color: #ef4444;">*</span></label>
                    <textarea name="description" id="inputComboDesc" class="form-input" rows="3" 
                              placeholder="Liệt kê chi tiết: số lượng bắp, size nước, vị bơ/phô mai/caramel đi kèm..." required></textarea>
                </div>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn-cancel" onclick="closeComboModal()">Hủy</button>
                <button type="submit" class="btn-confirm" id="btnSubmitCombo">Lưu Combo</button>
            </div>
        </form>
    </div>
</div>

<%---- ================= 7. MODAL XÁC NHẬN XÓA COMBO ================= ----%>
<div class="modal-overlay" id="deleteComboModal">
    <div class="modal-box" style="max-width: 460px;">
        <div class="modal-header">
            <h3 style="color: #f87171; display: flex; align-items: center; gap: 8px;">
                ⚠️ Xác Nhận Xóa Combo
            </h3>
            <button type="button" class="btn-close-modal" onclick="closeDeleteComboModal()">✕</button>
        </div>

        <form action="${pageContext.request.contextPath}/admin/combos/delete" method="POST">
            <input type="hidden" name="comboID" id="deleteComboID">

            <div class="modal-body" style="font-size: 14px; line-height: 1.6; color: #b5a995;">
                <p>Bạn có chắc chắn muốn xóa vĩnh viễn combo bắp nước sau:</p>
                <div style="background: #23201e; border: 1px solid #3c3733; border-radius: 8px; padding: 12px; margin: 12px 0;">
                    <div style="font-weight: 700; color: #f6ead2; font-size: 15px;" id="deleteComboTitle">Combo Solo</div>
                    <div style="font-size: 12px; color: #8c8273; margin-top: 4px;">Món ăn này sẽ không còn hiển thị trong bước đặt bắp nước của khách hàng.</div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn-cancel" onclick="closeDeleteComboModal()">Hủy bỏ</button>
                <button type="submit" class="btn-confirm" style="background: linear-gradient(135deg, #ef4444 0%, #b91c1c 100%);">
                    Xác Nhận Xóa
                </button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

<%---- ================= 8. JAVASCRIPT ĐIỀU KHIỂN ================= ----%>
<script>
// Chuyển đổi giữa Chế độ xem Thẻ Menu (Grid) và Bảng (Table)
function switchComboView(mode) {
    const gridSection = document.getElementById('comboGridSection');
    const tableSection = document.getElementById('comboTableSection');
    const btnGrid = document.getElementById('btnViewGrid');
    const btnTable = document.getElementById('btnViewTable');

    if (mode === 'table') {
        gridSection.style.display = 'none';
        tableSection.style.display = 'block';
        btnGrid.classList.remove('active');
        btnTable.classList.add('active');
    } else {
        gridSection.style.display = 'grid';
        tableSection.style.display = 'none';
        btnGrid.classList.add('active');
        btnTable.classList.remove('active');
    }
}

// Cập nhật Live Preview hình ảnh combo
function updateComboPreview(url) {
    const previewImg = document.getElementById('comboImagePreview');
    if (url && url.trim().length > 5) {
        previewImg.src = url.trim();
    }
}

// Bộ lọc tìm kiếm và danh mục
function applyComboFilter() {
    const keyword = (document.getElementById('filterKeyword').value || '').toLowerCase().trim();
    const cat = document.getElementById('filterCategory').value;
    const status = document.getElementById('filterStatus').value;

    // Lọc dạng Card
    const cards = document.querySelectorAll('#comboGridSection .combo-admin-card');
    cards.forEach(card => {
        const name = (card.getAttribute('data-name') || '').toLowerCase();
        const cardCat = card.getAttribute('data-cat') || '';
        const cardStatus = card.getAttribute('data-status') || '';

        let show = true;
        if (keyword && !name.includes(keyword)) show = false;
        if (cat && cardCat !== cat) show = false;
        if (status && cardStatus !== status) show = false;

        card.style.display = show ? '' : 'none';
    });

    // Lọc dạng Table
    const rows = document.querySelectorAll('#comboTableBody tr');
    rows.forEach(row => {
        const name = (row.getAttribute('data-name') || '').toLowerCase();
        const rowCat = row.getAttribute('data-cat') || '';
        const rowStatus = row.getAttribute('data-status') || '';

        let show = true;
        if (keyword && !name.includes(keyword)) show = false;
        if (cat && rowCat !== cat) show = false;
        if (status && rowStatus !== status) show = false;

        row.style.display = show ? '' : 'none';
    });
}

function resetComboFilters() {
    document.getElementById('filterKeyword').value = '';
    document.getElementById('filterCategory').value = '';
    document.getElementById('filterStatus').value = '';
    applyComboFilter();
}

// Mở Modal Thêm Combo Mới
function openAddComboModal() {
    document.getElementById('comboModalTitle').innerHTML = '🍿 Thêm Combo Bắp Nước Mới';
    document.getElementById('inputComboID').value = '';
    document.getElementById('comboForm').reset();
    document.getElementById('inputComboImageUrl').value = 'https://images.unsplash.com/photo-1572177191856-3cde618dee1f?w=400&auto=format&fit=crop';
    updateComboPreview('https://images.unsplash.com/photo-1572177191856-3cde618dee1f?w=400&auto=format&fit=crop');
    const modal = document.getElementById('comboModal');
    modal.style.display = 'flex';
    modal.classList.add('active');
    modal.classList.add('show');
}

// Mở Modal Sửa Combo
function openEditComboModal(id, name, cat, price, img, desc, status) {
    document.getElementById('comboModalTitle').textContent = '✏️ Chỉnh Sửa Combo #' + id.substring(0, 8);
    document.getElementById('inputComboID').value = id;
    document.getElementById('inputComboName').value = name;
    document.getElementById('inputComboCategory').value = cat;
    document.getElementById('inputComboPrice').value = price;
    document.getElementById('inputComboImageUrl').value = img || '';
    document.getElementById('inputComboDesc').value = desc;
    document.getElementById('inputComboStatus').value = status;
    updateComboPreview(img);
    const modal = document.getElementById('comboModal');
    modal.style.display = 'flex';
    modal.classList.add('active');
    modal.classList.add('show');
}

function closeComboModal() {
    const modal = document.getElementById('comboModal');
    modal.style.display = 'none';
    modal.classList.remove('active');
    modal.classList.remove('show');
}

// Mở Modal Xóa Combo
function openDeleteComboModal(id, name) {
    document.getElementById('deleteComboID').value = id;
    document.getElementById('deleteComboTitle').innerText = name;
    const modal = document.getElementById('deleteComboModal');
    modal.style.display = 'flex';
    modal.classList.add('active');
    modal.classList.add('show');
}

function closeDeleteComboModal() {
    const modal = document.getElementById('deleteComboModal');
    modal.style.display = 'none';
    modal.classList.remove('active');
    modal.classList.remove('show');
}

// Click ra ngoài đóng modal
window.addEventListener('click', function(e) {
    const comboModal = document.getElementById('comboModal');
    const deleteModal = document.getElementById('deleteComboModal');
    if (e.target === comboModal) closeComboModal();
    if (e.target === deleteModal) closeDeleteComboModal();
});

</script>
