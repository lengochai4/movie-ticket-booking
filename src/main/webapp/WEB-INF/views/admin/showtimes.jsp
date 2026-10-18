<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Quản Lý Lịch Chiếu - Cinetik Admin" scope="request"/>
<c:set var="extraCss" value="admin-users.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<style>
/* CSS Nâng Cao Cho Trang Quản Lý Lịch Chiếu (Showtimes) */
.showtime-view-toggle {
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

/* Date Strip Selector */
.date-strip-container {
    display: flex;
    align-items: center;
    gap: 10px;
    background: #161413;
    border: 1px solid rgba(252, 202, 70, 0.2);
    border-radius: 10px;
    padding: 12px 16px;
    overflow-x: auto;
}
.date-strip-label {
    font-size: 13px;
    font-weight: 700;
    color: #fcca46;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    white-space: nowrap;
    display: flex;
    align-items: center;
    gap: 6px;
    padding-right: 12px;
    border-right: 1px solid #2d2824;
}
.date-pill-list {
    display: flex;
    gap: 8px;
    flex: 1;
    overflow-x: auto;
}
.date-pill {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 8px 16px;
    border-radius: 8px;
    background: #23201e;
    border: 1px solid #3c3733;
    color: #e4d8c5;
    cursor: pointer;
    min-width: 85px;
    transition: all 0.2s ease;
    text-decoration: none;
}
.date-pill:hover {
    border-color: #fcca46;
    color: #fcca46;
    transform: translateY(-2px);
}
.date-pill.active {
    background: linear-gradient(135deg, rgba(252, 202, 70, 0.2) 0%, rgba(229, 169, 59, 0.08) 100%);
    border-color: #fcca46;
    color: #fcca46;
    box-shadow: 0 0 12px rgba(252, 202, 70, 0.25);
}
.date-pill .day-name {
    font-size: 11px;
    text-transform: uppercase;
    font-weight: 600;
    opacity: 0.8;
}
.date-pill .day-num {
    font-size: 16px;
    font-weight: 700;
    margin-top: 2px;
}
.date-custom-input {
    background: #23201e;
    border: 1px solid #3c3733;
    border-radius: 8px;
    color: #f6ead2;
    padding: 8px 12px;
    font-size: 13px;
    outline: none;
    transition: border-color 0.2s;
    color-scheme: dark;
}
.date-custom-input:focus {
    border-color: #fcca46;
}

/* Timeline Layout */
.timeline-card {
    background: #161413;
    border: 1px solid rgba(252, 202, 70, 0.2);
    border-radius: 10px;
    padding: 24px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
}
.timeline-header-hours {
    display: grid;
    grid-template-columns: 160px repeat(16, 1fr);
    border-bottom: 2px solid #2d2824;
    padding-bottom: 12px;
    margin-bottom: 16px;
}
.timeline-hour-col {
    text-align: center;
    font-size: 12px;
    font-weight: 600;
    color: #8c8273;
    border-left: 1px dashed rgba(255, 255, 255, 0.08);
}
.timeline-room-row {
    display: grid;
    grid-template-columns: 160px 1fr;
    align-items: center;
    padding: 14px 0;
    border-bottom: 1px solid #23201e;
}
.timeline-room-row:last-child {
    border-bottom: none;
}
.timeline-room-label {
    display: flex;
    flex-direction: column;
    padding-right: 14px;
}
.timeline-room-title {
    font-weight: 700;
    font-size: 14px;
    color: #f6ead2;
}
.timeline-room-capacity {
    font-size: 12px;
    color: #8c8273;
    margin-top: 2px;
}
.timeline-room-track {
    position: relative;
    height: 60px;
    background: rgba(255, 255, 255, 0.02);
    border-radius: 8px;
    border: 1px dashed rgba(255, 255, 255, 0.06);
    overflow: hidden;
}
.timeline-block {
    position: absolute;
    top: 5px;
    bottom: 5px;
    border-radius: 6px;
    padding: 6px 10px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    cursor: pointer;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.4);
    transition: all 0.2s ease;
    border-left: 3px solid #fcca46;
}
.timeline-block:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.6);
    filter: brightness(1.15);
    z-index: 10;
}
.timeline-block.theme-imax {
    background: linear-gradient(135deg, rgba(30, 64, 175, 0.9) 0%, rgba(15, 23, 42, 0.9) 100%);
    border-left-color: #60a5fa;
}
.timeline-block.theme-gold {
    background: linear-gradient(135deg, rgba(161, 98, 7, 0.9) 0%, rgba(41, 37, 36, 0.9) 100%);
    border-left-color: #fcca46;
}
.timeline-block.theme-dolby {
    background: linear-gradient(135deg, rgba(126, 34, 206, 0.9) 0%, rgba(30, 27, 75, 0.9) 100%);
    border-left-color: #c084fc;
}
.timeline-block.theme-standard {
    background: linear-gradient(135deg, rgba(21, 128, 61, 0.85) 0%, rgba(20, 30, 20, 0.9) 100%);
    border-left-color: #4ade80;
}
.timeline-block-title {
    font-size: 12px;
    font-weight: 700;
    color: #ffffff;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.timeline-block-meta {
    font-size: 10.5px;
    color: rgba(255, 255, 255, 0.8);
    display: flex;
    justify-content: space-between;
    margin-top: 2px;
}

/* Badge Styles */
.badge-room {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 3px 8px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 600;
    background: #23201e;
    color: #e4d8c5;
    border: 1px solid #3c3733;
}
.badge-format {
    display: inline-block;
    padding: 3px 7px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.5px;
}
.format-imax { background: rgba(59, 130, 246, 0.2); color: #60a5fa; border: 1px solid rgba(59, 130, 246, 0.4); }
.format-dolby { background: rgba(168, 85, 247, 0.2); color: #c084fc; border: 1px solid rgba(168, 85, 247, 0.4); }
.format-3d { background: rgba(234, 179, 8, 0.2); color: #fde047; border: 1px solid rgba(234, 179, 8, 0.4); }
.format-2d { background: rgba(34, 197, 94, 0.2); color: #4ade80; border: 1px solid rgba(34, 197, 94, 0.4); }

.seat-progress-container {
    width: 120px;
    background: #23201e;
    border-radius: 10px;
    height: 8px;
    overflow: hidden;
    margin-top: 5px;
    border: 1px solid #3c3733;
}
.seat-progress-bar {
    height: 100%;
    background: linear-gradient(90deg, #4ade80 0%, #22c55e 100%);
    border-radius: 10px;
}
.seat-progress-bar.high {
    background: linear-gradient(90deg, #f59e0b 0%, #ef4444 100%);
}

/* Time Conflict Box */
.conflict-box {
    padding: 10px 14px;
    border-radius: 6px;
    font-size: 13px;
    display: none;
    align-items: center;
    gap: 8px;
    margin-top: 10px;
}
.conflict-box.safe {
    display: flex;
    background: rgba(34, 197, 94, 0.15);
    color: #4ade80;
    border: 1px solid rgba(34, 197, 94, 0.3);
}
.conflict-box.error {
    display: flex;
    background: rgba(239, 68, 68, 0.15);
    color: #f87171;
    border: 1px solid rgba(239, 68, 68, 0.3);
}

/* Nút Hành Động trong bảng Admin (Sửa, Hủy) */
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
    background: rgba(252, 202, 70, 0.12);
    color: #fcca46;
    border-color: rgba(252, 202, 70, 0.35);
}
.btn-table-action.edit:hover {
    background: #fcca46;
    color: #12100e;
    border-color: #fcca46;
    box-shadow: 0 0 12px rgba(252, 202, 70, 0.45);
    transform: translateY(-1px);
}
.btn-table-action.edit:hover svg {
    transform: scale(1.1);
}
.btn-table-action.delete {
    background: rgba(239, 68, 68, 0.12);
    color: #f87171;
    border-color: rgba(239, 68, 68, 0.35);
}
.btn-table-action.delete:hover {
    background: #ef4444;
    color: #ffffff;
    border-color: #ef4444;
    box-shadow: 0 0 12px rgba(239, 68, 68, 0.45);
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

        <%---- ================= 0. THANH ĐIỀU HƯỚNG QUẢN TRỊ ADMIN ================= ----%>
        <nav style="display: flex; gap: 8px; border-bottom: 1px solid #2d2824; padding-bottom: 12px; overflow-x: auto;">
            <a href="${pageContext.request.contextPath}/admin/dashboard" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                📊 Thống Kê
            </a>
            <a href="${pageContext.request.contextPath}/admin/movies" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                🎬 Quản Lý Phim
            </a>
            <a href="${pageContext.request.contextPath}/admin/showtimes" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: linear-gradient(135deg, rgba(252, 202, 70, 0.2) 0%, rgba(229, 169, 59, 0.08) 100%); border: 1px solid #fcca46; color: #fcca46; font-size: 13px; font-weight: 700; text-decoration: none;">
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
            <a href="${pageContext.request.contextPath}/admin/users" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                👥 Quản Lý Người Dùng
            </a>
        </nav>

        <%---- ================= 1. HEADER TRANG LỊCH CHIẾU ================= ----%>
        <header class="admin-header" style="align-items: center;">
            <div class="admin-header-title">
                <h1>Quản Lý Lịch Chiếu Phim</h1>
                <p>Lên lịch suất chiếu mới theo phim, phân bổ phòng chiếu, chọn khung giờ và quản lý tỷ lệ lấp đầy</p>
            </div>
            <div class="admin-header-actions" style="display: flex; gap: 12px; align-items: center;">
                <%---- Bộ nút chuyển chế độ xem: Dạng bảng vs Dạng Timeline ----%>
                <div class="showtime-view-toggle">
                    <button type="button" class="view-toggle-btn active" id="btnViewTable" onclick="switchView('table')">
                        📋 Dạng Bảng
                    </button>
                    <button type="button" class="view-toggle-btn" id="btnViewTimeline" onclick="switchView('timeline')">
                        ⏱️ Timeline Phòng
                    </button>
                </div>

                <%---- Nút Mở Modal Lên Lịch Chiếu ----%>
                <button type="button" class="btn-filter-submit" style="display: flex; align-items: center; gap: 6px; padding: 10px 20px; font-weight: 700;" onclick="openAddShowtimeModal()">
                    ➕ Lên Lịch Chiếu Mới
                </button>
            </div>
        </header>

        <%---- ================= 2. 4 THẺ THỐNG KÊ NHANH (KPIS) ================= ----%>
        <section class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon total">🎬</div>
                <div class="stat-info">
                    <span class="stat-value">${not empty todayShowtimesCount ? todayShowtimesCount : (not empty showtimeList ? showtimeList.size() : 0)} Suất</span>
                    <span class="stat-label">Tổng suất chiếu hôm nay</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon customer" style="background: rgba(34, 197, 94, 0.15); color: #4ade80;">📊</div>
                <div class="stat-info">
                    <span class="stat-value">${not empty avgOccupancy ? avgOccupancy : '0%'}</span>
                    <span class="stat-label">Tỷ lệ lấp đầy ghế TB</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon staff" style="background: rgba(56, 189, 248, 0.15); color: #38bdf8;">🚪</div>
                <div class="stat-info">
                    <span class="stat-value">${not empty activeRoomsCount ? activeRoomsCount : 0} / ${not empty totalRoomsCount ? totalRoomsCount : 0} Phòng</span>
                    <span class="stat-label">Phòng chiếu hoạt động</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon" style="background: rgba(229, 169, 59, 0.15); color: #fcca46;">⏳</div>
                <div class="stat-info">
                    <span class="stat-value">${not empty upcoming90MinCount ? upcoming90MinCount : 0} Suất</span>
                    <span class="stat-label">Sắp chiếu trong 90 phút</span>
                </div>
            </div>
        </section>

        <%---- ================= 3. THANH CHỌN NGÀY CHIẾU TRỰC QUAN ================= ----%>
        <section class="date-strip-container">
            <div class="date-strip-label">
                <span>📅 Ngày chiếu</span>
            </div>
            <div class="date-pill-list" id="datePillList">
                <a href="javascript:void(0)" class="date-pill active" onclick="selectDatePill(this, '2026-10-16')">
                    <span class="day-name">Hôm nay</span>
                    <span class="day-num">16/10</span>
                </a>
                <a href="javascript:void(0)" class="date-pill" onclick="selectDatePill(this, '2026-10-17')">
                    <span class="day-name">Thứ 7</span>
                    <span class="day-num">17/10</span>
                </a>
                <a href="javascript:void(0)" class="date-pill" onclick="selectDatePill(this, '2026-10-18')">
                    <span class="day-name">Chủ Nhật</span>
                    <span class="day-num">18/10</span>
                </a>
                <a href="javascript:void(0)" class="date-pill" onclick="selectDatePill(this, '2026-10-19')">
                    <span class="day-name">Thứ Hai</span>
                    <span class="day-num">19/10</span>
                </a>
                <a href="javascript:void(0)" class="date-pill" onclick="selectDatePill(this, '2026-10-20')">
                    <span class="day-name">Thứ Ba</span>
                    <span class="day-num">20/10</span>
                </a>
                <a href="javascript:void(0)" class="date-pill" onclick="selectDatePill(this, '2026-10-21')">
                    <span class="day-name">Thứ Tư</span>
                    <span class="day-num">21/10</span>
                </a>
                <a href="javascript:void(0)" class="date-pill" onclick="selectDatePill(this, '2026-10-22')">
                    <span class="day-name">Thứ Năm</span>
                    <span class="day-num">22/10</span>
                </a>
            </div>
            <div style="display: flex; align-items: center; gap: 8px;">
                <input type="date" id="customDatePicker" class="date-custom-input" value="2026-10-16" onchange="onCustomDateChange(this.value)">
            </div>
        </section>

        <%---- ================= 4. BỘ LỌC CHI TIẾT ================= ----%>
        <section class="filter-card">
            <form id="filterShowtimeForm" class="filter-form" onsubmit="event.preventDefault(); applyShowtimeFilter();">
                <%---- Lọc theo Phim ----%>
                <select id="filterMovie" class="filter-select" style="flex: 1.5;" onchange="applyShowtimeFilter()">
                    <option value="">-- Tất cả phim đang chiếu --</option>
                    <c:forEach items="${movieList}" var="m">
                        <option value="${m.title}">${m.title}</option>
                    </c:forEach>
                </select>

                <%---- Lọc theo Phòng chiếu ----%>
                <select id="filterRoom" class="filter-select" onchange="applyShowtimeFilter()">
                    <option value="">-- Tất cả phòng chiếu --</option>
                    <c:forEach items="${roomList}" var="r">
                        <option value="${r.name}">${r.name} - ${r.cinema != null ? r.cinema.name : 'Cinema'}</option>
                    </c:forEach>
                </select>

                <%---- Lọc theo Định dạng ----%>
                <select id="filterFormat" class="filter-select" onchange="applyShowtimeFilter()">
                    <option value="">-- Định dạng chiếu --</option>
                    <option value="IMAX 3D">IMAX 3D</option>
                    <option value="Dolby Atmos">Dolby Atmos</option>
                    <option value="2D Phụ Đề">2D Phụ Đề</option>
                    <option value="2D Lồng Tiếng">2D Lồng Tiếng</option>
                    <option value="3D Phụ Đề">3D Phụ Đề</option>
                </select>

                <%---- Lọc theo Trạng thái ----%>
                <select id="filterStatus" class="filter-select" onchange="applyShowtimeFilter()">
                    <option value="">-- Tất cả trạng thái --</option>
                    <option value="OPEN">Đang mở bán</option>
                    <option value="SOON">Sắp chiếu</option>
                    <option value="ENDED">Đã kết thúc</option>
                </select>

                <button type="button" class="btn-filter-reset" onclick="resetFilters()">Đặt lại</button>
            </form>
        </section>

        <%---- ================= 5. CHẾ ĐỘ 1: BẢNG DANH SÁCH SUẤT CHIẾU (TABLE VIEW) ================= ----%>
        <section class="table-card" id="tableViewContainer">
            <div class="table-responsive">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th style="width: 100px;">MÃ SUẤT</th>
                            <th style="width: 280px;">PHIM CHIẾU</th>
                            <th style="width: 160px;">PHÒNG & ĐỊNH DẠNG</th>
                            <th style="width: 150px;">KHUNG GIỜ</th>
                            <th style="width: 120px;">GIÁ VÉ (VND)</th>
                            <th style="width: 160px;">TỶ LỆ LẤP ĐẦY</th>
                            <th style="width: 120px;">TRẠNG THÁI</th>
                            <th style="width: 130px; text-align: center;">THAO TÁC</th>
                        </tr>
                    </thead>
                    <tbody id="showtimeTableBody">
                    <c:choose>
                        <c:when test="${not empty showtimeList}">
                            <c:forEach items="${showtimeList}" var="st">
                                <tr data-movie="${st.movie.title}" data-room="${st.room.name}" data-format="2D Digital" data-status="OPEN">
                                    <td><strong style="color: #fcca46;">#ST-${st.showtimeID}</strong></td>
                                    <td>
                                        <div style="display: flex; gap: 12px; align-items: center;">
                                            <img src="${not empty st.movie.posterUrl ? st.movie.posterUrl : 'https://placehold.co/44x60/23211f/fcca46?text=POSTER'}" 
                                                 alt="${st.movie.title}" style="width: 44px; height: 60px; object-fit: cover; border-radius: 4px; border: 1px solid #3c3733;">
                                            <div>
                                                <div style="font-weight: 700; color: #f6ead2;">${st.movie.title}</div>
                                                <div style="font-size: 12px; color: #8c8273; margin-top: 2px;">
                                                    ⏱️ ${st.movie.duration} phút | <span style="color: #f87171; font-weight: 700;">${st.movie.ageRating}</span>
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <div class="badge-room">${st.room.name}</div>
                                        <div style="margin-top: 4px;"><span class="badge-format format-2d">2D Digital</span></div>
                                    </td>
                                    <td>
                                        <div style="font-size: 15px; font-weight: 700; color: #fcca46;">${st.formattedStartTime} - ${st.formattedEndTime}</div>
                                        <div style="font-size: 11px; color: #8c8273; margin-top: 2px;">${st.formattedDate}</div>
                                    </td>
                                    <td>
                                        <strong style="color: #e4d8c5; font-size: 14px;">
                                            <fmt:formatNumber value="${st.price != null ? st.price : 90000}" type="currency" currencySymbol="đ" maxFractionDigits="0"/>
                                        </strong>
                                    </td>
                                    <td>
                                        <div style="font-size: 12px; color: #f6ead2; font-weight: 600;">0 / ${st.room.capacity > 0 ? st.room.capacity : 150} vé (0%)</div>
                                        <div class="seat-progress-container">
                                            <div class="seat-progress-bar low" style="width: 0%;"></div>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="badge-status" style="background: rgba(34, 197, 94, 0.15); color: #4ade80; border: 1px solid rgba(34, 197, 94, 0.3);">
                                            ● Đang bán
                                        </span>
                                    </td>
                                    <td class="table-actions-cell">
                                        <div class="table-actions-wrap">
                                            <button type="button" class="btn-table-action edit" title="Chỉnh sửa suất chiếu" 
                                                    onclick="openEditShowtimeModal('${st.showtimeID}', '${st.movie.title}', '${st.room.name}', '2D Digital', '${st.formattedDateIso}', '${st.formattedStartTime}', '${st.formattedEndTime}', '${st.price}')">
                                                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                                                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                                                </svg>
                                                <span>Sửa</span>
                                            </button>
                                            <button type="button" class="btn-table-action delete" title="Hủy suất chiếu" 
                                                    onclick="openDeleteShowtimeModal('${st.showtimeID}', '${st.movie.title}', '${st.room.name}', '${st.timeRange}', 0)">
                                                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                                                    <polyline points="3 6 5 6 21 6"></polyline>
                                                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                                                    <line x1="10" y1="11" x2="10" y2="17"></line>
                                                    <line x1="14" y1="11" x2="14" y2="17"></line>
                                                </svg>
                                                <span>Hủy</span>
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="8" style="text-align: center; padding: 40px; color: #8c8273;">
                                    <div style="font-size: 28px; margin-bottom: 8px;">🎬</div>
                                    <div>Chưa có suất chiếu nào được lên lịch cho ngày này.</div>
                                    <button type="button" class="btn-action-primary" style="margin-top: 12px; display: inline-flex;" onclick="openAddShowtimeModal()">
                                        + Lên lịch suất chiếu mới
                                    </button>
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

        <%---- ================= 6. CHẾ ĐỘ 2: TIMELINE PHÒNG CHIẾU TRỰC QUAN (TIMELINE VIEW) ================= ----%>
        <section class="timeline-card" id="timelineViewContainer" style="display: none;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                <h3 style="color: #fcca46; font-size: 18px; margin: 0; display: flex; align-items: center; gap: 8px;">
                    ⏱️ Sơ Đồ Khung Giờ Chiếu Theo Phòng (08:00 - 24:00)
                </h3>
                <div style="display: flex; gap: 14px; font-size: 12px; color: #b5a995;">
                    <span style="display: inline-flex; align-items: center; gap: 5px;">
                        <span style="width: 10px; height: 10px; background: #60a5fa; border-radius: 2px;"></span> IMAX Laser
                    </span>
                    <span style="display: inline-flex; align-items: center; gap: 5px;">
                        <span style="width: 10px; height: 10px; background: #c084fc; border-radius: 2px;"></span> Dolby Cinema
                    </span>
                    <span style="display: inline-flex; align-items: center; gap: 5px;">
                        <span style="width: 10px; height: 10px; background: #fcca46; border-radius: 2px;"></span> VIP Gold Class
                    </span>
                    <span style="display: inline-flex; align-items: center; gap: 5px;">
                        <span style="width: 10px; height: 10px; background: #4ade80; border-radius: 2px;"></span> Standard 2D
                    </span>
                </div>
            </div>

            <%---- Header trục thời gian (08:00 -> 24:00) ----%>
            <div class="timeline-header-hours">
                <div style="font-weight: 700; color: #fcca46; font-size: 13px;">PHÒNG CHIẾU</div>
                <div class="timeline-hour-col">08h</div>
                <div class="timeline-hour-col">09h</div>
                <div class="timeline-hour-col">10h</div>
                <div class="timeline-hour-col">11h</div>
                <div class="timeline-hour-col">12h</div>
                <div class="timeline-hour-col">13h</div>
                <div class="timeline-hour-col">14h</div>
                <div class="timeline-hour-col">15h</div>
                <div class="timeline-hour-col">16h</div>
                <div class="timeline-hour-col">17h</div>
                <div class="timeline-hour-col">18h</div>
                <div class="timeline-hour-col">19h</div>
                <div class="timeline-hour-col">20h</div>
                <div class="timeline-hour-col">21h</div>
                <div class="timeline-hour-col">22h</div>
                <div class="timeline-hour-col">23h</div>
            </div>

            <%---- Hàng Phòng 01: IMAX ----%>
            <div class="timeline-room-row">
                <div class="timeline-room-label">
                    <span class="timeline-room-title">Phòng 01</span>
                    <span class="timeline-room-capacity">IMAX Laser • 250 chỗ</span>
                </div>
                <div class="timeline-room-track">
                    <%---- Suất 1: 09:15 - 12:01 (từ 08h là 1.25h -> 7.8% đến 4h -> 25%) ----%>
                    <div class="timeline-block theme-imax" style="left: 7.8%; width: 17.3%;" 
                         onclick="openEditShowtimeModal('ST-101', 'Avatar 3', 'Phòng 01', 'IMAX 3D', '2026-10-16', '09:15', '12:01', '140.000')"
                         title="Avatar: Dòng Chảy Của Nước (09:15 - 12:01) - 220/250 vé">
                        <div class="timeline-block-title">Avatar: Dòng Chảy Của Nước</div>
                        <div class="timeline-block-meta"><span>09:15 - 12:01</span> <span>88%</span></div>
                    </div>
                    <%---- Suất 2: 13:30 - 16:16 ----%>
                    <div class="timeline-block theme-imax" style="left: 34.3%; width: 17.3%;"
                         onclick="openEditShowtimeModal('ST-106', 'Avatar 3', 'Phòng 01', 'IMAX 3D', '2026-10-16', '13:30', '16:16', '140.000')"
                         title="Avatar: Dòng Chảy Của Nước (13:30 - 16:16) - 240/250 vé">
                        <div class="timeline-block-title">Avatar: Dòng Chảy Của Nước</div>
                        <div class="timeline-block-meta"><span>13:30 - 16:16</span> <span>96%</span></div>
                    </div>
                    <%---- Suất 3: 20:00 - 22:18 ----%>
                    <div class="timeline-block theme-imax" style="left: 75.0%; width: 14.4%;"
                         onclick="openEditShowtimeModal('ST-105', 'Joker 2', 'Phòng 01', 'IMAX 3D', '2026-10-16', '20:00', '22:18', '140.000')"
                         title="Joker: Folie à Deux (20:00 - 22:18) - 215/250 vé">
                        <div class="timeline-block-title">Joker: Folie à Deux</div>
                        <div class="timeline-block-meta"><span>20:00 - 22:18</span> <span>86%</span></div>
                    </div>
                </div>
            </div>

            <%---- Hàng Phòng 02: Dolby Cinema ----%>
            <div class="timeline-room-row">
                <div class="timeline-room-label">
                    <span class="timeline-room-title">Phòng 02</span>
                    <span class="timeline-room-capacity">Dolby Atmos • 180 chỗ</span>
                </div>
                <div class="timeline-room-track">
                    <%---- Suất 1: 10:30 - 13:16 ----%>
                    <div class="timeline-block theme-dolby" style="left: 15.6%; width: 17.3%;"
                         onclick="openEditShowtimeModal('ST-102', 'Dune 2', 'Phòng 02', 'Dolby Atmos', '2026-10-16', '10:30', '13:16', '120.000')"
                         title="Dune 2 (10:30 - 13:16) - 126/180 vé">
                        <div class="timeline-block-title">Dune: Phần 2</div>
                        <div class="timeline-block-meta"><span>10:30 - 13:16</span> <span>70%</span></div>
                    </div>
                    <%---- Suất 2: 15:00 - 17:46 ----%>
                    <div class="timeline-block theme-dolby" style="left: 43.7%; width: 17.3%;"
                         title="Dune 2 (15:00 - 17:46) - 150/180 vé">
                        <div class="timeline-block-title">Dune: Phần 2</div>
                        <div class="timeline-block-meta"><span>15:00 - 17:46</span> <span>83%</span></div>
                    </div>
                    <%---- Suất 3: 19:00 - 21:18 ----%>
                    <div class="timeline-block theme-dolby" style="left: 68.7%; width: 14.4%;"
                         title="Joker 2 (19:00 - 21:18) - 162/180 vé">
                        <div class="timeline-block-title">Joker: Folie à Deux</div>
                        <div class="timeline-block-meta"><span>19:00 - 21:18</span> <span>90%</span></div>
                    </div>
                </div>
            </div>

            <%---- Hàng Phòng 03: VIP Gold Class ----%>
            <div class="timeline-room-row">
                <div class="timeline-room-label">
                    <span class="timeline-room-title">Phòng 03</span>
                    <span class="timeline-room-capacity">VIP Lounge • 60 chỗ</span>
                </div>
                <div class="timeline-room-track">
                    <%---- Suất 1: 14:00 - 16:11 ----%>
                    <div class="timeline-block theme-gold" style="left: 37.5%; width: 13.6%;"
                         title="Mai (14:00 - 16:11) - 45/60 vé">
                        <div class="timeline-block-title">Mai</div>
                        <div class="timeline-block-meta"><span>14:00 - 16:11</span> <span>75%</span></div>
                    </div>
                    <%---- Suất 2: 18:30 - 20:41 ----%>
                    <div class="timeline-block theme-gold" style="left: 65.6%; width: 13.6%;"
                         onclick="openEditShowtimeModal('ST-104', 'Mai', 'Phòng 03', '2D Phụ Đề', '2026-10-16', '18:30', '20:41', '160.000')"
                         title="Mai (18:30 - 20:41) - 58/60 vé">
                        <div class="timeline-block-title">Mai (VIP)</div>
                        <div class="timeline-block-meta"><span>18:30 - 20:41</span> <span>96%</span></div>
                    </div>
                    <%---- Suất 3: 21:30 - 23:18 ----%>
                    <div class="timeline-block theme-gold" style="left: 84.3%; width: 11.2%;"
                         title="Quỷ Cẩu (21:30 - 23:18) - 50/60 vé">
                        <div class="timeline-block-title">Quỷ Cẩu</div>
                        <div class="timeline-block-meta"><span>21:30 - 23:18</span> <span>83%</span></div>
                    </div>
                </div>
            </div>

            <%---- Hàng Phòng 04: Standard 2D ----%>
            <div class="timeline-room-row">
                <div class="timeline-room-label">
                    <span class="timeline-room-title">Phòng 04</span>
                    <span class="timeline-room-capacity">Standard • 140 chỗ</span>
                </div>
                <div class="timeline-room-track">
                    <%---- Suất 1: 09:00 - 10:50 ----%>
                    <div class="timeline-block theme-standard" style="left: 6.2%; width: 11.4%;"
                         title="Conan (09:00 - 10:50) - 130/140 vé">
                        <div class="timeline-block-title">Conan Movie 27</div>
                        <div class="timeline-block-meta"><span>09:00 - 10:50</span> <span>93%</span></div>
                    </div>
                    <%---- Suất 2: 14:00 - 15:50 ----%>
                    <div class="timeline-block theme-standard" style="left: 37.5%; width: 11.4%;"
                         onclick="openEditShowtimeModal('ST-103', 'Conan', 'Phòng 04', '2D Lồng Tiếng', '2026-10-16', '14:00', '15:50', '90.000')"
                         title="Conan (14:00 - 15:50) - 129/140 vé">
                        <div class="timeline-block-title">Conan Movie 27</div>
                        <div class="timeline-block-meta"><span>14:00 - 15:50</span> <span>92%</span></div>
                    </div>
                    <%---- Suất 3: 17:00 - 19:11 ----%>
                    <div class="timeline-block theme-standard" style="left: 56.2%; width: 13.6%;"
                         title="Mai (17:00 - 19:11) - 110/140 vé">
                        <div class="timeline-block-title">Mai</div>
                        <div class="timeline-block-meta"><span>17:00 - 19:11</span> <span>78%</span></div>
                    </div>
                    <%---- Suất 4: 20:30 - 22:18 ----%>
                    <div class="timeline-block theme-standard" style="left: 78.1%; width: 11.2%;"
                         title="Quỷ Cẩu (20:30 - 22:18) - 125/140 vé">
                        <div class="timeline-block-title">Quỷ Cẩu</div>
                        <div class="timeline-block-meta"><span>20:30 - 22:18</span> <span>89%</span></div>
                    </div>
                </div>
            </div>
        </section>

    </div>
</main>

<%---- ================= 7. MODAL THÊM / CHỈNH SỬA SUẤT CHIẾU ================= ----%>
<div class="modal-overlay" id="showtimeModal">
    <div class="modal-box" style="max-width: 680px;">
        <div class="modal-header">
            <h3 id="showtimeModalTitle" style="display: flex; align-items: center; gap: 8px;">
                ➕ Lên Lịch Chiếu Mới
            </h3>
            <button type="button" class="btn-close-modal" onclick="closeShowtimeModal()">✕</button>
        </div>

        <form action="${pageContext.request.contextPath}/admin/showtimes/save" method="POST" id="showtimeForm">
            <input type="hidden" name="showtimeID" id="inputShowtimeID">

            <div class="modal-body" style="display: flex; flex-direction: column; gap: 16px;">
                
                <%---- Phim chiếu & Poster preview tóm tắt ----%>
                <div class="form-group">
                    <label class="form-label" style="display: flex; justify-content: space-between;">
                        <span>Chọn Phim Chiếu <span style="color: #ef4444;">*</span></span>
                        <span id="selectedMovieDurationText" style="color: #fcca46; font-size: 12px; font-weight: normal;">⏱️ Thời lượng: 166 phút</span>
                    </label>
                    <div style="display: flex; gap: 14px; align-items: center;">
                        <img id="modalMovieThumb" src="https://image.tmdb.org/t/p/w200/t6HIqrRAclMCA60NsSmeqe9RmNV.jpg" 
                             alt="Poster" style="width: 50px; height: 72px; object-fit: cover; border-radius: 6px; border: 1px solid #3c3733;">
                        <div style="flex: 1;">
                            <select name="movieID" id="inputMovieID" class="form-select" required onchange="onMovieSelectChange()">
                                <c:forEach items="${movieList}" var="m">
                                    <option value="${m.movieID}" data-duration="${m.duration}" data-thumb="${not empty m.posterUrl ? m.posterUrl : 'https://placehold.co/200x300/23211f/fcca46?text=POSTER'}">${m.title} (${m.duration} phút)</option>
                                </c:forEach>
                            </select>
                            <div style="font-size: 11.5px; color: #8c8273; margin-top: 4px;">Hệ thống tự động cộng thời lượng phim + 15 phút dọn rạp để tính giờ kết thúc.</div>
                        </div>
                    </div>
                </div>

                <%---- Cụm Phòng chiếu & Định dạng chiếu ----%>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
                    <div class="form-group">
                        <label class="form-label">Phòng Chiếu <span style="color: #ef4444;">*</span></label>
                        <select name="roomID" id="inputRoomID" class="form-select" required onchange="calculateEndTime()">
                                <c:forEach items="${roomList}" var="r">
                                    <option value="${r.roomID}">${r.name} - ${r.cinema != null ? r.cinema.name : 'Cinema'} (${r.capacity} chỗ)</option>
                                </c:forEach>
                            </select>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Định Dạng Chiếu <span style="color: #ef4444;">*</span></label>
                        <select name="format" id="inputFormat" class="form-select" required onchange="suggestTicketPrice()">
                            <option value="IMAX 3D">IMAX 3D (Kính 3D)</option>
                            <option value="Dolby Atmos">Dolby Atmos (Âm thanh vòm)</option>
                            <option value="2D Phụ Đề">2D Phụ Đề Tiếng Việt</option>
                            <option value="2D Lồng Tiếng">2D Lồng Tiếng Việt</option>
                            <option value="3D Phụ Đề">3D Phụ Đề</option>
                        </select>
                    </div>
                </div>

                <%---- Ngày chiếu, Giờ bắt đầu, Giờ kết thúc ----%>
                <div style="display: grid; grid-template-columns: 1.2fr 1fr 1fr; gap: 14px;">
                    <div class="form-group">
                        <label class="form-label">Ngày Chiếu <span style="color: #ef4444;">*</span></label>
                        <input type="date" name="showDate" id="inputShowDate" class="form-input" value="2026-10-16" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label">Giờ Bắt Đầu <span style="color: #ef4444;">*</span></label>
                        <input type="time" name="startTime" id="inputStartTime" class="form-input" value="14:30" required onchange="calculateEndTime()">
                    </div>

                    <div class="form-group">
                        <label class="form-label">Giờ Kết Thúc (Dự kiến)</label>
                        <input type="text" name="endTime" id="inputEndTime" class="form-input" value="17:31" readonly style="background: #1e1b18; color: #fcca46; font-weight: 700;">
                    </div>
                </div>

                <%---- Giá vé niêm yết ----%>
                <div class="form-group">
                    <label class="form-label">Giá Vé Cơ Bản (VND) <span style="color: #ef4444;">*</span></label>
                    <input type="text" name="price" id="inputPrice" class="form-input" placeholder="Ví dụ: 120.000" value="140.000" required>
                    <div style="font-size: 11.5px; color: #8c8273; margin-top: 4px;">Giá niêm yết cho ghế thường, ghế VIP tự động cộng thêm 15.000 đ, ghế đôi Sweetbox cộng 45.000 đ.</div>
                </div>

                <%---- Hộp thông báo Xung Đột Lịch Chiếu (Real-time Conflict Indicator) ----%>
                <div id="conflictBox" class="conflict-box safe">
                    <span>✅</span>
                    <span id="conflictText">Khung giờ hợp lệ! Không có suất chiếu nào khác bị trùng tại phòng này.</span>
                </div>

            </div>

            <div class="modal-footer">
                <button type="button" class="btn-cancel" onclick="closeShowtimeModal()">Hủy</button>
                <button type="submit" class="btn-confirm" id="btnSubmitShowtime">Lưu Suất Chiếu</button>
            </div>
        </form>
    </div>
</div>

<%---- ================= 8. MODAL XÁC NHẬN HỦY / XÓA SUẤT CHIẾU ================= ----%>
<div class="modal-overlay" id="deleteShowtimeModal">
    <div class="modal-box" style="max-width: 480px;">
        <div class="modal-header">
            <h3 style="color: #f87171; display: flex; align-items: center; gap: 8px;">
                ⚠️ Xác Nhận Hủy Suất Chiếu
            </h3>
            <button type="button" class="btn-close-modal" onclick="closeDeleteShowtimeModal()">✕</button>
        </div>

        <form action="${pageContext.request.contextPath}/admin/showtimes/delete" method="POST">
            <input type="hidden" name="showtimeID" id="deleteShowtimeID">

            <div class="modal-body" style="font-size: 14px; line-height: 1.6; color: #b5a995;">
                <p>Bạn có chắc chắn muốn hủy suất chiếu của phim:</p>
                <div style="background: #23201e; border: 1px solid #3c3733; border-radius: 8px; padding: 12px; margin: 12px 0;">
                    <div style="font-weight: 700; color: #f6ead2; font-size: 15px;" id="deleteMovieTitle">Avatar: Dòng Chảy Của Nước</div>
                    <div style="font-size: 13px; color: #fcca46; margin-top: 4px;" id="deleteShowtimeMeta">Phòng 01 (IMAX) • 09:15 - 12:01</div>
                    <div style="font-size: 12.5px; color: #f87171; margin-top: 6px; font-weight: 600;" id="deleteBookedNotice">
                        🚨 Đã có 220 vé được thanh toán cho suất này!
                    </div>
                </div>

                <div class="form-group" style="margin-top: 14px;">
                    <label class="form-label">Lý do hủy suất chiếu <span style="color: #ef4444;">*</span></label>
                    <select name="cancelReason" class="form-select" required>
                        <option value="Bảo trì kỹ thuật phòng chiếu">Bảo trì kỹ thuật / Sự cố thiết bị phòng chiếu</option>
                        <option value="Điều chỉnh lịch phát hành của nhà rạp">Điều chỉnh lịch phát hành của nhà rạp</option>
                        <option value="Nhập sai thông tin khung giờ">Nhập sai thông tin khung giờ / Phòng chiếu</option>
                        <option value="Khác">Lý do khác</option>
                    </select>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn-cancel" onclick="closeDeleteShowtimeModal()">Quay lại</button>
                <button type="submit" class="btn-confirm" style="background: linear-gradient(135deg, #ef4444 0%, #b91c1c 100%);">
                    Xác Nhận Hủy Suất
                </button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

<%---- ================= 9. JAVASCRIPT ĐIỀU KHIỂN GIAO DIỆN ================= ----%>
<script>
// Chuyển đổi giữa Chế độ xem Dạng Bảng và Timeline
function switchView(mode) {
    const tableContainer = document.getElementById('tableViewContainer');
    const timelineContainer = document.getElementById('timelineViewContainer');
    const btnTable = document.getElementById('btnViewTable');
    const btnTimeline = document.getElementById('btnViewTimeline');

    if (mode === 'timeline') {
        tableContainer.style.display = 'none';
        timelineContainer.style.display = 'block';
        btnTable.classList.remove('active');
        btnTimeline.classList.add('active');
    } else {
        tableContainer.style.display = 'block';
        timelineContainer.style.display = 'none';
        btnTable.classList.add('active');
        btnTimeline.classList.remove('active');
    }
}

// Chọn Date Pill
function selectDatePill(element, dateStr) {
    document.querySelectorAll('.date-pill').forEach(el => el.classList.remove('active'));
    element.classList.add('active');
    document.getElementById('customDatePicker').value = dateStr;
    document.getElementById('inputShowDate').value = dateStr;
}

// Khi thay đổi ngày bằng date picker
function onCustomDateChange(val) {
    document.getElementById('inputShowDate').value = val;
    document.querySelectorAll('.date-pill').forEach(el => el.classList.remove('active'));
}

// Bộ lọc danh sách bảng
function applyShowtimeFilter() {
    const movieFilter = document.getElementById('filterMovie').value.toLowerCase();
    const roomFilter = document.getElementById('filterRoom').value.toLowerCase();
    const formatFilter = document.getElementById('filterFormat').value.toLowerCase();
    const statusFilter = document.getElementById('filterStatus').value;

    const rows = document.querySelectorAll('#showtimeTableBody tr');
    rows.forEach(row => {
        const rowMovie = (row.getAttribute('data-movie') || '').toLowerCase();
        const rowRoom = (row.getAttribute('data-room') || '').toLowerCase();
        const rowFormat = (row.getAttribute('data-format') || '').toLowerCase();
        const rowStatus = row.getAttribute('data-status') || '';

        let show = true;
        if (movieFilter && !rowMovie.includes(movieFilter)) show = false;
        if (roomFilter && !rowRoom.includes(roomFilter)) show = false;
        if (formatFilter && !rowFormat.includes(formatFilter)) show = false;
        if (statusFilter && rowStatus !== statusFilter) show = false;

        row.style.display = show ? '' : 'none';
    });
}

function resetFilters() {
    document.getElementById('filterMovie').value = '';
    document.getElementById('filterRoom').value = '';
    document.getElementById('filterFormat').value = '';
    document.getElementById('filterStatus').value = '';
    applyShowtimeFilter();
}

// Khi chọn phim khác trong Modal: cập nhật thumbnail và tự tính giờ kết thúc
function onMovieSelectChange() {
    const movieSelect = document.getElementById('inputMovieID');
    const selectedOption = movieSelect.options[movieSelect.selectedIndex];
    const duration = selectedOption.getAttribute('data-duration') || '120';
    const thumb = selectedOption.getAttribute('data-thumb') || '';

    document.getElementById('selectedMovieDurationText').innerText = '⏱️ Thời lượng: ' + duration + ' phút';
    if (thumb) {
        document.getElementById('modalMovieThumb').src = thumb;
    }
    calculateEndTime();
}

// Tự động tính Giờ Kết Thúc = Giờ Bắt Đầu + Thời Lượng Phim + 15 phút dọn rạp
function calculateEndTime() {
    const movieSelect = document.getElementById('inputMovieID');
    const selectedOption = movieSelect.options[movieSelect.selectedIndex];
    const duration = parseInt(selectedOption.getAttribute('data-duration') || '120', 10);
    const startTimeVal = document.getElementById('inputStartTime').value;

    if (!startTimeVal) return;

    const parts = startTimeVal.split(':');
    let startHours = parseInt(parts[0], 10);
    let startMinutes = parseInt(parts[1], 10);

    // Thêm thời lượng phim + 15 phút buffer vệ sinh
    const totalMinutes = startHours * 60 + startMinutes + duration + 15;
    let endHours = Math.floor(totalMinutes / 60) % 24;
    let endMins = totalMinutes % 60;

    const formattedEnd = (endHours < 10 ? '0' : '') + endHours + ':' + (endMins < 10 ? '0' : '') + endMins;
    document.getElementById('inputEndTime').value = formattedEnd;
}

// Gợi ý giá vé theo định dạng
function suggestTicketPrice() {
    const format = document.getElementById('inputFormat').value;
    const priceInput = document.getElementById('inputPrice');
    if (format === 'IMAX 3D') {
        priceInput.value = '140.000';
    } else if (format === 'Dolby Atmos') {
        priceInput.value = '120.000';
    } else if (format === '3D Phụ Đề') {
        priceInput.value = '110.000';
    } else {
        priceInput.value = '90.000';
    }
}

// Mở Modal Lên Lịch Chiếu Mới
function openAddShowtimeModal() {
    document.getElementById('showtimeModalTitle').innerHTML = '➕ Lên Lịch Chiếu Mới';
    document.getElementById('inputShowtimeID').value = '';
    document.getElementById('showtimeForm').reset();
    document.getElementById('inputShowDate').value = document.getElementById('customDatePicker').value || '2026-10-16';
    document.getElementById('inputStartTime').value = '14:30';
    onMovieSelectChange();
    suggestTicketPrice();
    const modal = document.getElementById('showtimeModal');
    modal.style.display = 'flex';
    modal.classList.add('active');
    modal.classList.add('show');
}

// Mở Modal Sửa Suất Chiếu
function openEditShowtimeModal(id, movie, room, format, date, start, end, price) {
    document.getElementById('showtimeModalTitle').innerHTML = '✏️ Chỉnh Sửa Suất Chiếu #' + id;
    document.getElementById('inputShowtimeID').value = id;
    
    // Set movie
    const movieSelect = document.getElementById('inputMovieID');
    for (let i = 0; i < movieSelect.options.length; i++) {
        if (movieSelect.options[i].value === movie) {
            movieSelect.selectedIndex = i;
            break;
        }
    }
    onMovieSelectChange();

    // Set room
    const roomSelect = document.getElementById('inputRoomID');
    for (let i = 0; i < roomSelect.options.length; i++) {
        if (roomSelect.options[i].value === room) {
            roomSelect.selectedIndex = i;
            break;
        }
    }

    // Set format
    const formatSelect = document.getElementById('inputFormat');
    for (let i = 0; i < formatSelect.options.length; i++) {
        if (formatSelect.options[i].value === format) {
            formatSelect.selectedIndex = i;
            break;
        }
    }

    document.getElementById('inputShowDate').value = date;
    document.getElementById('inputStartTime').value = start;
    document.getElementById('inputEndTime').value = end;
    document.getElementById('inputPrice').value = price;

    const modal = document.getElementById('showtimeModal');
    modal.style.display = 'flex';
    modal.classList.add('active');
    modal.classList.add('show');
}

function closeShowtimeModal() {
    const modal = document.getElementById('showtimeModal');
    modal.style.display = 'none';
    modal.classList.remove('active');
    modal.classList.remove('show');
}

// Mở Modal Hủy Suất Chiếu
function openDeleteShowtimeModal(id, title, room, time, booked) {
    document.getElementById('deleteShowtimeID').value = id;
    document.getElementById('deleteMovieTitle').innerText = title;
    document.getElementById('deleteShowtimeMeta').innerText = room + ' • ' + time;
    const bookedNotice = document.getElementById('deleteBookedNotice');
    if (booked > 0) {
        bookedNotice.innerText = '🚨 Cảnh báo: Đã có ' + booked + ' vé được thanh toán cho suất chiếu này!';
        bookedNotice.style.display = 'block';
    } else {
        bookedNotice.style.display = 'none';
    }
    const modal = document.getElementById('deleteShowtimeModal');
    modal.style.display = 'flex';
    modal.classList.add('active');
    modal.classList.add('show');
}

function closeDeleteShowtimeModal() {
    const modal = document.getElementById('deleteShowtimeModal');
    modal.style.display = 'none';
    modal.classList.remove('active');
    modal.classList.remove('show');
}

// Đóng modal khi click ra ngoài overlay
window.addEventListener('click', function(e) {
    const showtimeModal = document.getElementById('showtimeModal');
    const deleteModal = document.getElementById('deleteShowtimeModal');
    if (e.target === showtimeModal) closeShowtimeModal();
    if (e.target === deleteModal) closeDeleteShowtimeModal();
});
</script>
