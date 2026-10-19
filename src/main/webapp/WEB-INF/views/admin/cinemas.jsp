<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Quản Lý Rạp & Phòng Chiếu - Cinetik Admin" scope="request"/>
<c:set var="extraCss" value="admin-users.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<style>
    /* Custom CSS riêng cho khu vực Rạp, Phòng và Ma trận Ghế */
    .cinema-admin-grid {
        display: grid;
        grid-template-columns: 360px 1fr;
        gap: 24px;
    }
    @media (max-width: 992px) {
        .cinema-admin-grid {
            grid-template-columns: 1fr;
        }
    }
    .cinema-item-card {
        background: #191614;
        border: 1px solid #3c3733;
        border-radius: 10px;
        padding: 16px;
        margin-bottom: 12px;
        cursor: pointer;
        transition: all 0.2s ease;
        position: relative;
    }
    .cinema-item-card:hover {
        border-color: #fcca46;
        transform: translateY(-2px);
    }
    .cinema-item-card.active {
        background: linear-gradient(135deg, rgba(252, 202, 70, 0.12) 0%, rgba(30, 26, 23, 0.95) 100%);
        border-color: #fcca46;
        box-shadow: 0 4px 15px rgba(252, 202, 70, 0.15);
    }
    .room-badge-pill {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 6px 12px;
        border-radius: 6px;
        background: #23201e;
        border: 1px solid #3c3733;
        color: #e6dfd5;
        font-size: 13px;
        cursor: pointer;
        transition: all 0.2s;
        text-decoration: none;
    }
    .room-badge-pill:hover, .room-badge-pill.active {
        background: #fcca46;
        color: #1a1412;
        border-color: #fcca46;
        font-weight: 700;
    }

    /* Khung màn hình và lưới ghế */
    .screen-visual {
        text-align: center;
        margin: 20px auto 30px;
        max-width: 500px;
    }
    .screen-curve {
        height: 6px;
        background: linear-gradient(90deg, transparent 0%, #fcca46 50%, transparent 100%);
        border-radius: 50%;
        box-shadow: 0 4px 16px rgba(252, 202, 70, 0.4);
        margin-bottom: 8px;
    }
    .screen-label {
        font-size: 11px;
        letter-spacing: 3px;
        color: #8c827a;
        font-weight: 600;
    }

    /* Ma trận ghế trực quan */
    .seat-matrix-wrapper {
        display: flex;
        flex-direction: column;
        gap: 8px;
        align-items: center;
        padding: 24px;
        background: #141110;
        border: 1px solid #2d2824;
        border-radius: 12px;
        overflow-x: auto;
    }
    .seat-row-box {
        display: flex;
        align-items: center;
        gap: 8px;
    }
    .seat-row-tag {
        width: 24px;
        font-size: 12px;
        font-weight: 700;
        color: #8c827a;
        text-align: center;
    }
    .admin-seat-cell {
        width: 38px;
        height: 34px;
        border-radius: 6px;
        font-size: 11px;
        font-weight: 700;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.15s ease;
        user-select: none;
    }
    .admin-seat-cell.standard {
        background: #25201c;
        border: 1px solid #453e39;
        color: #d1c7bc;
    }
    .admin-seat-cell.vip {
        background: rgba(252, 202, 70, 0.15);
        border: 1px solid #fcca46;
        color: #fcca46;
    }
    .admin-seat-cell.couple {
        background: rgba(244, 63, 94, 0.15);
        border: 1px solid #f43f5e;
        color: #f43f5e;
        width: 60px;
    }
    .admin-seat-cell.maintenance {
        background: rgba(239, 68, 68, 0.2);
        border: 1px dashed #ef4444;
        color: #ef4444;
        text-decoration: line-through;
    }
    .admin-seat-cell:hover {
        transform: scale(1.1);
        box-shadow: 0 4px 10px rgba(0,0,0,0.4);
    }

    /* Chú thích màu ghế */
    .seat-legend-bar {
        display: flex;
        justify-content: center;
        gap: 20px;
        flex-wrap: wrap;
        padding: 12px 18px;
        background: #191614;
        border-radius: 8px;
        border: 1px solid #2d2824;
        margin-bottom: 20px;
        font-size: 12.5px;
    }
    .legend-item {
        display: flex;
        align-items: center;
        gap: 8px;
    }
    .legend-box {
        width: 16px;
        height: 16px;
        border-radius: 4px;
    }
</style>

<main class="admin-container">
    <div class="admin-layout" style="max-width: 1250px;">

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
            <a href="${pageContext.request.contextPath}/admin/cinemas" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: linear-gradient(135deg, rgba(252, 202, 70, 0.2) 0%, rgba(229, 169, 59, 0.08) 100%); border: 1px solid #fcca46; color: #fcca46; font-size: 13px; font-weight: 700; text-decoration: none;">
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

        <%---- ================= 1. HEADER TRANG ================= ----%>
        <header class="admin-header" style="align-items: center;">
            <div class="admin-header-title">
                <h1>Quản Lý Cụm Rạp, Phòng & Sơ Đồ Ghế</h1>
                <p>Thiết lập chi nhánh rạp, quản trị các phòng chiếu phim và tự động tạo ma trận ghế ngồi thông minh</p>
            </div>
            <div class="admin-header-actions" style="display: flex; gap: 10px;">
                <button type="button" class="btn-filter-submit" style="display: flex; align-items: center; gap: 6px; padding: 10px 18px; font-weight: 700;" onclick="openAddCinemaModal()">
                    ➕ Thêm Cụm Rạp
                </button>
                <c:if test="${not empty selectedCinemaId}">
                    <button type="button" class="btn-header-back" style="background: #25201b; border-color: #fcca46; color: #fcca46; font-weight: 700;" data-cinema-id="${selectedCinemaId}" data-cinema-name="${fn:escapeXml(selectedCinema.name)}"
                        onclick="openAddRoomModal(this.dataset.cinemaId, this.dataset.cinemaName)">
                        🚪 Thêm Phòng Chiếu
                    </button>
                </c:if>
            </div>
        </header>

        <%---- THÔNG BÁO THÀNH CÔNG NẾU CÓ ----%>
        <c:if test="${not empty successMessage}">
            <div style="background: rgba(34, 197, 94, 0.15); border: 1px solid #22c55e; color: #4ade80; border-radius: 8px; padding: 12px 20px; font-weight: 600; text-align: center; font-size: 14px;">
                ✅ <c:out value="${successMessage}"/>
            </div>
        </c:if>

        <%---- THÔNG BÁO LỖI NẾU CÓ ----%>
        <c:if test="${not empty errorMessage}">
            <div style="background: rgba(239, 68, 68, 0.15); border: 1px solid #ef4444; color: #f87171; border-radius: 8px; padding: 12px 20px; font-weight: 600; text-align: center; font-size: 14px;">
                ⚠️ <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <%---- ================= 2. 4 THẺ THỐNG KÊ NHANH ================= ----%>
        <section class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon total">🏛️</div>
                <div class="stat-info">
                    <span class="stat-value">${empty totalCinemas ? 0 : totalCinemas}</span>
                    <span class="stat-label">Cụm rạp chi nhánh</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon customer" style="background: rgba(59, 130, 246, 0.15); color: #60a5fa;">🚪</div>
                <div class="stat-info">
                    <span class="stat-value">${empty totalRooms ? 0 : totalRooms}</span>
                    <span class="stat-label">Tổng số phòng chiếu</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon locked" style="background: rgba(252, 202, 70, 0.15); color: #fcca46;">💺</div>
                <div class="stat-info">
                    <span class="stat-value">${empty totalSeats ? 0 : totalSeats}</span>
                    <span class="stat-label">Tổng số ghế toàn hệ thống</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon total" style="background: rgba(34, 197, 94, 0.15); color: #4ade80;">✅</div>
                <div class="stat-info">
                    <span class="stat-value">${empty activeSeats ? 0 : activeSeats}</span>
                    <span class="stat-label">Ghế sẵn sàng đón khách</span>
                </div>
            </div>
        </section>

        <%---- ================= 3. BỐ CỤC: DANH SÁCH RẠP & PHÒNG CHIẾU ================= ----%>
        <div class="cinema-admin-grid">

            <%---- CỘT TRÁI: DANH SÁCH CÁC CỤM RẠP (CINEMA LIST) ----%>
            <section class="table-card" style="padding: 20px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; border-bottom: 1px solid #2d2824; padding-bottom: 10px;">
                    <h3 style="color: #fcca46; font-size: 16px; margin: 0; display: flex; align-items: center; gap: 8px;">
                        🏛️ Cụm Rạp Chi Nhánh
                    </h3>
                    <span style="font-size: 12px; color: #8c827a;">Chọn rạp để xem phòng</span>
                </div>

                <div class="cinema-list-scroll" style="max-height: 520px; overflow-y: auto;">
                    <c:choose>
                        <%---- KHI CÓ DỮ LIỆU ĐỘNG TỪ DATABASE ----%>
                        <c:when test="${not empty cinemaList}">
                            <c:forEach var="c" items="${cinemaList}">
                                <div class="cinema-item-card ${c.cinemaID == selectedCinemaId ? 'active' : ''}" 
                                     onclick="window.location.href='${pageContext.request.contextPath}/admin/cinemas?cinemaId=${c.cinemaID}'">
                                    <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                                        <strong style="color: #fff; font-size: 15px;">${c.name}</strong>
                                        <div style="display: flex; gap: 6px;">
                                            <button type="button" class="btn-table-action edit" title="Sửa rạp"
                                                    data-id="${c.cinemaID}" data-name="${fn:escapeXml(c.name)}" data-address="${fn:escapeXml(c.address)}"
                                                    onclick="event.stopPropagation(); openEditCinemaModal(this.dataset.id, this.dataset.name, this.dataset.address)">
                                                ✏️
                                            </button>
                                            <button type="button" class="btn-table-action delete" title="Xóa rạp"
                                                    data-id="${c.cinemaID}" data-name="${fn:escapeXml(c.name)}"
                                                    onclick="event.stopPropagation(); openDeleteCinemaModal(this.dataset.id, this.dataset.name)">
                                                🗑️
                                            </button>
                                        </div>
                                    </div>
                                    <p style="font-size: 12.5px; color: #94a3b8; margin: 6px 0 10px; line-height: 1.4;">
                                        📍 ${c.address}
                                    </p>
                                    <div style="display: flex; justify-content: space-between; align-items: center; font-size: 12px; color: #fcca46;">
                                        <span>🚪 ${not empty c.rooms ? c.rooms.size() : 0} phòng chiếu</span>
                                        <span style="font-weight: 600;">Xem chi tiết ➔</span>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>

                        <%---- CHƯA CÓ RẠP NÀO ----%>
                        <c:otherwise>
                            <div style="text-align: center; padding: 30px 10px; color: #a89f91; font-size: 14px;">
                                Chưa có cụm rạp nào. Bấm "Thêm Cụm Rạp" để tạo rạp đầu tiên.
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </section>

            <%---- CỘT PHẢI: QUẢN LÝ PHÒNG CHIẾU CỦA RẠP ĐANG CHỌN ----%>
            <section class="table-card" style="padding: 20px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; border-bottom: 1px solid #2d2824; padding-bottom: 10px; flex-wrap: wrap; gap: 10px;">
                    <div>
                        <h3 style="color: #fcca46; font-size: 16px; margin: 0; display: flex; align-items: center; gap: 8px;">
                            🚪 Danh Sách Phòng: 
                            <span style="color: #fff; font-weight: 500;">
                                ${not empty selectedCinema ? fn:escapeXml(selectedCinema.name) : 'Chưa chọn rạp'}
                            </span>
                        </h3>
                    </div>
                    <c:if test="${not empty selectedCinemaId}">
                        <button type="button" class="btn-filter-submit" style="padding: 6px 14px; font-size: 12.5px;"
                                data-cinema-id="${selectedCinemaId}" data-cinema-name="${fn:escapeXml(selectedCinema.name)}"
                        onclick="openAddRoomModal(this.dataset.cinemaId, this.dataset.cinemaName)">
                            ➕ Thêm Phòng Mới
                        </button>
                    </c:if>
                </div>

                <%---- Danh sách các nút chọn phòng nhanh ----%>
                <div style="display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 20px;">
                    <c:choose>
                        <c:when test="${not empty roomList}">
                            <c:forEach var="r" items="${roomList}">
                                <a href="${pageContext.request.contextPath}/admin/cinemas?cinemaId=${selectedCinemaId}&roomId=${r.roomID}" 
                                   class="room-badge-pill ${r.roomID == selectedRoomId ? 'active' : ''}">
                                    🎬 ${fn:escapeXml(r.name)} (${empty seatCounts[r.roomID] ? 0 : seatCounts[r.roomID]} ghế)
                                </a>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <span style="font-size: 13px; color: #a89f91;">Rạp này chưa có phòng chiếu nào.</span>
                        </c:otherwise>
                    </c:choose>
                </div>

                <%---- Bảng quản lý chi tiết các phòng ----%>
                <div class="table-responsive">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>Tên phòng</th>
                                <th>Sức chứa</th>
                                <th>Cụm rạp</th>
                                <th style="text-align: right;">Hành động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty roomList}">
                                    <c:forEach var="r" items="${roomList}">
                                        <tr style="${r.roomID == selectedRoomId ? 'background: rgba(252,202,70,0.06);' : ''}">
                                            <td>
                                                <strong style="color: #fcca46;">${r.name}</strong>
                                                <c:if test="${r.roomID == selectedRoomId}">
                                                    <span style="font-size: 11px; background: #fcca46; color: #1a1412; padding: 2px 6px; border-radius: 4px; font-weight: 700; margin-left: 6px;">Đang xem sơ đồ</span>
                                                </c:if>
                                            </td>
                                            <td><span class="user-role-badge">${empty seatCounts[r.roomID] ? 0 : seatCounts[r.roomID]} ghế</span></td>
                                            <td style="color: #94a3b8; font-size: 13px;">${r.cinema.name}</td>
                                            <td style="text-align: right;">
                                                <a href="${pageContext.request.contextPath}/admin/cinemas?cinemaId=${selectedCinemaId}&roomId=${r.roomID}" 
                                                   class="btn-action edit" title="Xem & tạo ghế">💺 Sơ đồ ghế</a>
                                                <button type="button" class="btn-action edit" title="Sửa phòng"
                                                        data-id="${r.roomID}" data-name="${fn:escapeXml(r.name)}" data-cinema-id="${selectedCinemaId}" data-cinema-name="${fn:escapeXml(selectedCinema.name)}"
                                                        onclick="openEditRoomModal(this.dataset.id, this.dataset.name, this.dataset.cinemaId, this.dataset.cinemaName)">✏️</button>
                                                <button type="button" class="btn-action delete" title="Xóa phòng"
                                                        data-id="${r.roomID}" data-name="${fn:escapeXml(r.name)}" data-cinema-id="${selectedCinemaId}"
                                                        onclick="openDeleteRoomModal(this.dataset.id, this.dataset.name, this.dataset.cinemaId)">🗑️</button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="4" style="text-align: center; padding: 24px 10px; color: #a89f91;">
                                            <c:choose>
                                                <c:when test="${not empty selectedCinemaId}">Rạp chưa có phòng chiếu. Bấm "Thêm Phòng Mới" để tạo phòng.</c:when>
                                                <c:otherwise>Hãy tạo cụm rạp trước khi thêm phòng chiếu.</c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </section>
        </div>

        <%---- ================= 4. KHU VỰC MA TRẬN GHẾ TRỰC QUAN (SEAT MATRIX) ================= ----%>
        <section class="table-card" style="padding: 24px; margin-top: 24px;">
            <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #2d2824; padding-bottom: 16px; margin-bottom: 20px; flex-wrap: wrap; gap: 12px;">
                <div>
                    <h2 style="color: #fcca46; font-size: 20px; margin: 0 0 6px; display: flex; align-items: center; gap: 8px;">
                        💺 Sơ Đồ Ghế Ngồi: 
                        <span style="color: #fff;">
                            <c:choose>
                                <c:when test="${not empty selectedRoom}">${fn:escapeXml(selectedRoom.name)} (${fn:escapeXml(selectedCinema.name)})</c:when>
                                <c:otherwise>Chưa chọn phòng</c:otherwise>
                            </c:choose>
                        </span>
                    </h2>
                    <p style="margin: 0; font-size: 13px; color: #94a3b8;">
                        Click vào từng ghế để đổi trạng thái [Hoạt động / Bảo trì] hoặc bấm "Tạo tự động" để sinh toàn bộ sơ đồ ghế
                    </p>
                </div>
                <div>
                    <c:if test="${not empty selectedRoomId}">
                        <button type="button" class="btn-filter-submit" style="background: linear-gradient(135deg, #f59e0b, #d97706); display: flex; align-items: center; gap: 8px; font-weight: 700; padding: 10px 20px;"
                                data-room-id="${selectedRoomId}" data-room-name="${fn:escapeXml(selectedRoom.name)}" data-cinema-id="${selectedCinemaId}"
                                onclick="openGenerateSeatsModal(this.dataset.roomId, this.dataset.roomName, this.dataset.cinemaId)">
                            ⚡ Tạo Tự Động Sơ Đồ Ghế
                        </button>
                    </c:if>
                </div>
            </div>

            <%---- Chú thích loại ghế ----%>
            <div class="seat-legend-bar">
                <div class="legend-item">
                    <div class="legend-box" style="background: #25201c; border: 1px solid #453e39;"></div>
                    <span>Ghế Thường (Standard)</span>
                </div>
                <div class="legend-item">
                    <div class="legend-box" style="background: rgba(252, 202, 70, 0.25); border: 1px solid #fcca46;"></div>
                    <span style="color: #fcca46; font-weight: 600;">Ghế VIP</span>
                </div>
                <div class="legend-item">
                    <div class="legend-box" style="background: rgba(244, 63, 94, 0.25); border: 1px solid #f43f5e;"></div>
                    <span style="color: #f43f5e; font-weight: 600;">Ghế Đôi (Couple)</span>
                </div>
                <div class="legend-item">
                    <div class="legend-box" style="background: rgba(239, 68, 68, 0.3); border: 1px dashed #ef4444;"></div>
                    <span style="color: #ef4444;">Đang bảo trì / Hỏng</span>
                </div>
            </div>

            <%---- Màn hình chiếu phim cong ----%>
            <div class="screen-visual">
                <div class="screen-curve"></div>
                <span class="screen-label">MÀN HÌNH CHIẾU PHIM</span>
            </div>

            <%---- Ma trận ghế ----%>
            <div class="seat-matrix-wrapper">
                <c:choose>
                    <%---- Dữ liệu ghế thật từ Database ----%>
                    <c:when test="${not empty seatList}">
                        <%-- Gom nhóm theo hàng ghế và hiển thị --%>
                        <c:set var="currentRow" value=""/>
                        <div style="display: flex; flex-direction: column; gap: 10px; align-items: center;">
                            <c:forEach var="s" items="${seatList}">
                                <c:if test="${s.seatRow != currentRow}">
                                    <c:if test="${not empty currentRow}">
                                        </div>
                                    </c:if>
                                    <c:set var="currentRow" value="${s.seatRow}"/>
                                    <div class="seat-row-box">
                                        <span class="seat-row-tag">${currentRow}</span>
                                </c:if>

                                <form action="${pageContext.request.contextPath}/admin/cinemas/seats/toggle" method="POST" style="margin: 0; display: inline;">
                                    <input type="hidden" name="formToken" value="${formToken}">
                                    <input type="hidden" name="seatId" value="${s.seatID}">
                                    <input type="hidden" name="cinemaId" value="${selectedCinemaId}">
                                    <input type="hidden" name="roomId" value="${selectedRoomId}">
                                    <button type="submit" 
                                            class="admin-seat-cell ${s.status == 'MAINTENANCE' ? 'maintenance' : (s.seatType == 'VIP' ? 'vip' : (s.seatType == 'COUPLE' ? 'couple' : 'standard'))}"
                                            title="Mã: ${s.seatCode} | Loại: ${s.seatType} | Click để đổi trạng thái">
                                        ${s.seatCode}
                                    </button>
                                </form>
                            </c:forEach>
                            </div>
                        </div>
                    </c:when>

                    <%---- Chưa có phòng, hoặc phòng chưa có sơ đồ ghế ----%>
                    <c:otherwise>
                        <div style="text-align: center; padding: 30px 10px; color: #a89f91;">
                            <p style="font-size: 16px; margin-bottom: 16px;">
                                ${empty selectedRoomId ? 'Chưa có phòng chiếu để hiển thị sơ đồ ghế.' : 'Phòng này chưa có sơ đồ ghế.'}
                            </p>
                            <c:if test="${not empty selectedRoomId}">
                                <button type="button" class="btn-filter-submit" style="padding: 10px 24px; font-weight: 700;"
                                        data-room-id="${selectedRoomId}" data-room-name="${fn:escapeXml(selectedRoom.name)}" data-cinema-id="${selectedCinemaId}"
                                onclick="openGenerateSeatsModal(this.dataset.roomId, this.dataset.roomName, this.dataset.cinemaId)">
                                    ⚡ Tạo sơ đồ ghế tự động
                                </button>
                            </c:if>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>

    </div>
</main>

<%---- ================= 5. CÁC MODAL FORM ================= ----%>

<%---- MODAL THÊM / SỬA CỤM RẠP ----%>
<div id="modalCinema" style="display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.75); z-index: 9999; align-items: center; justify-content: center;">
    <div style="background: #181d26; border: 1px solid rgba(252, 202, 70, 0.4); border-radius: 12px; padding: 24px; max-width: 500px; width: 90%; color: #fff;">
        <h3 id="modalCinemaTitle" style="margin-top: 0; color: #fcca46;">Thêm Cụm Rạp Mới</h3>
        <form action="${pageContext.request.contextPath}/admin/cinemas/cinema/save" method="POST">
            <input type="hidden" name="formToken" value="${formToken}">
            <input type="hidden" name="cinemaId" id="cinemaInputId">
            
            <div style="margin-bottom: 14px;">
                <label style="display: block; font-size: 13px; color: #cbd5e1; margin-bottom: 6px;">Tên cụm rạp:</label>
                <input type="text" name="name" id="cinemaInputName" required placeholder="VD: Cinetik Landmark 81" 
                       style="width: 100%; padding: 10px 12px; border-radius: 6px; background: #0f1217; border: 1px solid rgba(255,255,255,0.15); color: #fff; box-sizing: border-box;">
            </div>

            <div style="margin-bottom: 18px;">
                <label style="display: block; font-size: 13px; color: #cbd5e1; margin-bottom: 6px;">Địa chỉ chi tiết:</label>
                <textarea name="address" id="cinemaInputAddress" rows="3" required placeholder="VD: Tầng B1, TTTM Vincom Landmark 81, Bình Thạnh, TP.HCM"
                          style="width: 100%; padding: 10px 12px; border-radius: 6px; background: #0f1217; border: 1px solid rgba(255,255,255,0.15); color: #fff; box-sizing: border-box;"></textarea>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 10px;">
                <button type="button" onclick="closeModal('modalCinema')" style="background: #272f3d; color: #cbd5e1; border: none; padding: 9px 18px; border-radius: 6px; cursor: pointer;">Hủy</button>
                <button type="submit" style="background: #fcca46; color: #1a1412; font-weight: 700; border: none; padding: 9px 20px; border-radius: 6px; cursor: pointer;">Lưu Cụm Rạp</button>
            </div>
        </form>
    </div>
</div>

<%---- MODAL XÓA CỤM RẠP ----%>
<div id="modalDeleteCinema" style="display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.75); z-index: 9999; align-items: center; justify-content: center;">
    <div style="background: #181d26; border: 1px solid rgba(239, 68, 68, 0.4); border-radius: 12px; padding: 24px; max-width: 440px; width: 90%; color: #fff;">
        <h3 style="margin-top: 0; color: #f87171;">⚠️ Xác Nhận Xóa Cụm Rạp</h3>
        <p style="font-size: 14px; color: #cbd5e1;">Bạn có chắc chắn muốn xóa cụm rạp <strong id="deleteCinemaName" style="color: #fcca46;"></strong>? Các phòng chiếu và sơ đồ ghế của rạp sẽ bị xóa theo. Hệ thống chỉ cho xóa khi rạp không còn suất chiếu và chưa có vé nào được bán.</p>
        <form action="${pageContext.request.contextPath}/admin/cinemas/cinema/delete" method="POST">
            <input type="hidden" name="formToken" value="${formToken}">
            <input type="hidden" name="cinemaId" id="deleteCinemaId">
            <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px;">
                <button type="button" onclick="closeModal('modalDeleteCinema')" style="background: #272f3d; color: #cbd5e1; border: none; padding: 9px 18px; border-radius: 6px; cursor: pointer;">Hủy</button>
                <button type="submit" style="background: #ef4444; color: #fff; font-weight: 700; border: none; padding: 9px 20px; border-radius: 6px; cursor: pointer;">Xác Nhận Xóa</button>
            </div>
        </form>
    </div>
</div>

<%---- MODAL THÊM / SỬA PHÒNG CHIẾU ----%>
<div id="modalRoom" style="display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.75); z-index: 9999; align-items: center; justify-content: center;">
    <div style="background: #181d26; border: 1px solid rgba(252, 202, 70, 0.4); border-radius: 12px; padding: 24px; max-width: 500px; width: 90%; color: #fff;">
        <h3 id="modalRoomTitle" style="margin-top: 0; color: #fcca46;">Thêm Phòng Chiếu Mới</h3>
        <form action="${pageContext.request.contextPath}/admin/cinemas/room/save" method="POST">
            <input type="hidden" name="formToken" value="${formToken}">
            <input type="hidden" name="roomId" id="roomInputId">
            <input type="hidden" name="cinemaId" id="roomInputCinemaId">
            
            <div style="margin-bottom: 12px;">
                <label style="display: block; font-size: 13px; color: #cbd5e1; margin-bottom: 6px;">Thuộc cụm rạp:</label>
                <input type="text" id="roomDisplayCinemaName" readonly style="width: 100%; padding: 10px 12px; border-radius: 6px; background: #222834; border: 1px solid #3c3733; color: #fcca46; font-weight: 600; box-sizing: border-box;">
            </div>

            <div style="margin-bottom: 14px;">
                <label style="display: block; font-size: 13px; color: #cbd5e1; margin-bottom: 6px;">Tên phòng chiếu:</label>
                <input type="text" name="name" id="roomInputName" required placeholder="VD: Phòng 01 (Laser 2D)" 
                       style="width: 100%; padding: 10px 12px; border-radius: 6px; background: #0f1217; border: 1px solid rgba(255,255,255,0.15); color: #fff; box-sizing: border-box;">
            </div>

            <p style="font-size: 12.5px; color: #94a3b8; margin: 0 0 18px;">
                Sức chứa của phòng bằng số ghế và được tính tự động khi tạo sơ đồ ghế.
            </p>

            <div style="display: flex; justify-content: flex-end; gap: 10px;">
                <button type="button" onclick="closeModal('modalRoom')" style="background: #272f3d; color: #cbd5e1; border: none; padding: 9px 18px; border-radius: 6px; cursor: pointer;">Hủy</button>
                <button type="submit" style="background: #fcca46; color: #1a1412; font-weight: 700; border: none; padding: 9px 20px; border-radius: 6px; cursor: pointer;">Lưu Phòng</button>
            </div>
        </form>
    </div>
</div>

<%---- MODAL XÓA PHÒNG CHIẾU ----%>
<div id="modalDeleteRoom" style="display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.75); z-index: 9999; align-items: center; justify-content: center;">
    <div style="background: #181d26; border: 1px solid rgba(239, 68, 68, 0.4); border-radius: 12px; padding: 24px; max-width: 440px; width: 90%; color: #fff;">
        <h3 style="margin-top: 0; color: #f87171;">⚠️ Xác Nhận Xóa Phòng Chiếu</h3>
        <p style="font-size: 14px; color: #cbd5e1;">Bạn có chắc chắn muốn xóa <strong id="deleteRoomName" style="color: #fcca46;"></strong>? Sơ đồ ghế của phòng sẽ bị xóa theo. Hệ thống chỉ cho xóa khi phòng không còn suất chiếu và chưa có vé nào được bán.</p>
        <form action="${pageContext.request.contextPath}/admin/cinemas/room/delete" method="POST">
            <input type="hidden" name="formToken" value="${formToken}">
            <input type="hidden" name="roomId" id="deleteRoomId">
            <input type="hidden" name="cinemaId" id="deleteRoomCinemaId">
            <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px;">
                <button type="button" onclick="closeModal('modalDeleteRoom')" style="background: #272f3d; color: #cbd5e1; border: none; padding: 9px 18px; border-radius: 6px; cursor: pointer;">Hủy</button>
                <button type="submit" style="background: #ef4444; color: #fff; font-weight: 700; border: none; padding: 9px 20px; border-radius: 6px; cursor: pointer;">Xác Nhận Xóa</button>
            </div>
        </form>
    </div>
</div>

<%---- MODAL TẠO TỰ ĐỘNG MA TRẬN SƠ ĐỒ GHẾ (AUTO GENERATE SEATS) ----%>
<div id="modalGenerateSeats" style="display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.75); z-index: 9999; align-items: center; justify-content: center;">
    <div style="background: #181d26; border: 1px solid rgba(252, 202, 70, 0.4); border-radius: 12px; padding: 24px; max-width: 540px; width: 90%; color: #fff;">
        <h3 style="margin-top: 0; color: #fcca46; display: flex; align-items: center; gap: 8px;">
            ⚡ Tạo Tự Động Sơ Đồ Ghế
        </h3>
        <p style="font-size: 13.5px; color: #94a3b8; margin-bottom: 16px;">
            Hệ thống sẽ <strong style="color: #f87171;">xóa sơ đồ ghế cũ</strong>, tạo mới các mã ghế từ hàng A và cập nhật lại sức chứa cho phòng <strong id="genRoomName" style="color: #fcca46;"></strong>.
            Chỉ thực hiện được khi ghế của phòng chưa có vé nào được bán.
        </p>
        <form action="${pageContext.request.contextPath}/admin/cinemas/seats/generate" method="POST">
            <input type="hidden" name="formToken" value="${formToken}">
            <input type="hidden" name="roomId" id="genRoomId">
            <input type="hidden" name="cinemaId" id="genCinemaId">

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 14px;">
                <div>
                    <label style="display: block; font-size: 13px; color: #cbd5e1; margin-bottom: 6px;">Số hàng ghế (Rows):</label>
                    <input type="number" name="rows" value="8" min="2" max="20" required 
                           style="width: 100%; padding: 10px 12px; border-radius: 6px; background: #0f1217; border: 1px solid rgba(255,255,255,0.15); color: #fff; box-sizing: border-box;">
                    <span style="font-size: 11px; color: #64748b;">Ví dụ: 8 hàng là từ A đến H</span>
                </div>
                <div>
                    <label style="display: block; font-size: 13px; color: #cbd5e1; margin-bottom: 6px;">Số ghế mỗi hàng (Cols):</label>
                    <input type="number" name="cols" value="10" min="4" max="30" required 
                           style="width: 100%; padding: 10px 12px; border-radius: 6px; background: #0f1217; border: 1px solid rgba(255,255,255,0.15); color: #fff; box-sizing: border-box;">
                    <span style="font-size: 11px; color: #64748b;">Ví dụ: 10 ghế/hàng</span>
                </div>
            </div>

            <div style="margin-bottom: 14px;">
                <label style="display: block; font-size: 13px; color: #cbd5e1; margin-bottom: 6px;">Các hàng ghế VIP (ngăn cách bởi dấu phẩy):</label>
                <input type="text" name="vipRows" value="E,F,G" placeholder="VD: E,F,G"
                       style="width: 100%; padding: 10px 12px; border-radius: 6px; background: #0f1217; border: 1px solid rgba(255,255,255,0.15); color: #fcca46; font-weight: 600; box-sizing: border-box;">
            </div>

            <div style="margin-bottom: 20px;">
                <label style="display: block; font-size: 13px; color: #cbd5e1; margin-bottom: 6px;">Các hàng ghế ĐÔI - COUPLE (ngăn cách bởi dấu phẩy):</label>
                <input type="text" name="coupleRows" value="H" placeholder="VD: H"
                       style="width: 100%; padding: 10px 12px; border-radius: 6px; background: #0f1217; border: 1px solid rgba(255,255,255,0.15); color: #f43f5e; font-weight: 600; box-sizing: border-box;">
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 10px;">
                <button type="button" onclick="closeModal('modalGenerateSeats')" style="background: #272f3d; color: #cbd5e1; border: none; padding: 9px 18px; border-radius: 6px; cursor: pointer;">Hủy</button>
                <button type="submit" style="background: linear-gradient(135deg, #f59e0b, #d97706); color: #1a1412; font-weight: 700; border: none; padding: 9px 20px; border-radius: 6px; cursor: pointer;">Bắt Đầu Sinh Ghế</button>
            </div>
        </form>
    </div>
</div>

<script>
    // Mỗi form chỉ được gửi một lần: bấm liên tiếp sẽ không tạo thêm bản ghi
    document.querySelectorAll('form[method="POST"]').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            if (form.dataset.submitting) {
                e.preventDefault();
                return;
            }
            form.dataset.submitting = '1';
            form.querySelectorAll('button[type="submit"]').forEach(function (btn) {
                btn.disabled = true;
                btn.style.opacity = '0.6';
                btn.style.cursor = 'wait';
            });
        });
    });
    // Quay lại bằng nút Back: tải lại trang để form không bị kẹt ở trạng thái đã gửi
    window.addEventListener('pageshow', function (e) {
        if (e.persisted) window.location.reload();
    });

    function closeModal(id) {
        document.getElementById(id).style.display = 'none';
    }

    function openAddCinemaModal() {
        document.getElementById('modalCinemaTitle').innerText = 'Thêm Cụm Rạp Mới';
        document.getElementById('cinemaInputId').value = '';
        document.getElementById('cinemaInputName').value = '';
        document.getElementById('cinemaInputAddress').value = '';
        document.getElementById('modalCinema').style.display = 'flex';
    }

    function openEditCinemaModal(id, name, address) {
        document.getElementById('modalCinemaTitle').innerText = 'Chỉnh Sửa Cụm Rạp';
        document.getElementById('cinemaInputId').value = id;
        document.getElementById('cinemaInputName').value = name;
        document.getElementById('cinemaInputAddress').value = address;
        document.getElementById('modalCinema').style.display = 'flex';
    }

    function openDeleteCinemaModal(id, name) {
        document.getElementById('deleteCinemaId').value = id;
        document.getElementById('deleteCinemaName').innerText = name;
        document.getElementById('modalDeleteCinema').style.display = 'flex';
    }

    function openAddRoomModal(cinemaId, cinemaName) {
        document.getElementById('modalRoomTitle').innerText = 'Thêm Phòng Chiếu Mới';
        document.getElementById('roomInputId').value = '';
        document.getElementById('roomInputCinemaId').value = cinemaId;
        document.getElementById('roomDisplayCinemaName').value = cinemaName;
        document.getElementById('roomInputName').value = '';
        document.getElementById('modalRoom').style.display = 'flex';
    }

    function openEditRoomModal(id, name, cinemaId, cinemaName) {
        document.getElementById('modalRoomTitle').innerText = 'Chỉnh Sửa Phòng Chiếu';
        document.getElementById('roomInputId').value = id;
        document.getElementById('roomInputCinemaId').value = cinemaId;
        document.getElementById('roomDisplayCinemaName').value = cinemaName;
        document.getElementById('roomInputName').value = name;
        document.getElementById('modalRoom').style.display = 'flex';
    }

    function openDeleteRoomModal(roomId, roomName, cinemaId) {
        document.getElementById('deleteRoomId').value = roomId;
        document.getElementById('deleteRoomName').innerText = roomName;
        document.getElementById('deleteRoomCinemaId').value = cinemaId;
        document.getElementById('modalDeleteRoom').style.display = 'flex';
    }

    function openGenerateSeatsModal(roomId, roomName, cinemaId) {
        document.getElementById('genRoomId').value = roomId;
        document.getElementById('genCinemaId').value = cinemaId;
        document.getElementById('genRoomName').innerText = roomName;
        document.getElementById('modalGenerateSeats').style.display = 'flex';
    }
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
