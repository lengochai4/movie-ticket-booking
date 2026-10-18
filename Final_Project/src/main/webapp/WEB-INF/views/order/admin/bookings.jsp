<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Quản Lý Đặt Vé - Cinetik Admin" scope="request"/>
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
            <a href="${pageContext.request.contextPath}/admin/bookings" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: linear-gradient(135deg, rgba(252, 202, 70, 0.2) 0%, rgba(229, 169, 59, 0.08) 100%); border: 1px solid #fcca46; color: #fcca46; font-size: 13px; font-weight: 700; text-decoration: none;">
                🎟️ Quản Lý Đặt Vé
            </a>
            <a href="${pageContext.request.contextPath}/admin/users" style="display: inline-flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 6px; background: #1a1715; border: 1px solid #3c3733; color: #b5a995; font-size: 13px; text-decoration: none;">
                👥 Quản Lý Người Dùng
            </a>
        </nav>

        <%---- ================= 1. HEADER TRANG QUẢN TRỊ ================= ----%>
        <header class="admin-header">
            <div class="admin-header-title">
                <h1>Quản Lý Đơn Đặt Vé</h1>
                <p>Tra cứu mã vé điện tử, kiểm soát trạng thái thanh toán và quản lý hủy vé xem phim</p>
            </div>
            <div class="admin-header-actions">
                <a href="${pageContext.request.contextPath}/movies" class="btn-header-back">
                    <span>⬅ Quay lại trang chủ</span>
                </a>
            </div>
        </header>

        <%---- ================= 2. THỐNG KÊ NHANH (CARDS) ================= ----%>
        <section class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon total">🎟️</div>
                <div class="stat-info">
                    <span class="stat-value">${not empty totalOrders ? totalOrders : 128}</span>
                    <span class="stat-label">Tổng số đơn vé</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon" style="background: rgba(34, 197, 94, 0.15); color: #4ade80;">💰</div>
                <div class="stat-info">
                    <span class="stat-value">${not empty totalRevenue ? totalRevenue : '28.450.000đ'}</span>
                    <span class="stat-label">Tổng doanh thu</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon customer">✅</div>
                <div class="stat-info">
                    <span class="stat-value">${not empty paidOrders ? paidOrders : 115}</span>
                    <span class="stat-label">Vé hợp lệ (Đã thanh toán)</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon locked">✕</div>
                <div class="stat-info">
                    <span class="stat-value">${not empty cancelledOrders ? cancelledOrders : 13}</span>
                    <span class="stat-label">Đơn vé đã hủy</span>
                </div>
            </div>
        </section>

        <%---- ================= 3. THANH TÌM KIẾM & TRA CỨU MÃ VÉ ================= ----%>
        <section class="filter-card">
            <form action="${pageContext.request.contextPath}/admin/bookings" method="GET" class="filter-form">
                <%---- Tra cứu nhanh theo mã vé, tên, sđt ----%>
                <div class="search-group" style="flex: 2;">
                    <span class="search-icon-inside">🔍</span>
                    <input type="text" 
                           name="keyword" 
                           class="search-input" 
                           placeholder="Tra cứu theo mã vé (VD: CNK-2026-88921), tên khách, SĐT..." 
                           value="${param.keyword}">
                </div>

                <%---- Lọc theo trạng thái ----%>
                <select name="status" class="filter-select">
                    <option value="">-- Tất cả trạng thái --</option>
                    <option value="PAID" ${param.status == 'PAID' ? 'selected' : ''}>Đã thanh toán (Hợp lệ)</option>
                    <option value="PENDING" ${param.status == 'PENDING' ? 'selected' : ''}>Chờ thanh toán</option>
                    <option value="CANCELLED" ${param.status == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
                </select>

                <button type="submit" class="btn-filter-submit">Tìm Kiếm</button>
                <a href="${pageContext.request.contextPath}/admin/bookings" class="btn-filter-reset">Đặt lại</a>
            </form>
        </section>

        <%---- ================= 4. BẢNG DANH SÁCH ĐƠN VÉ ================= ----%>
        <section class="table-card">
            <div class="table-responsive">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th style="width: 140px;">Mã vé</th>
                            <th>Khách hàng</th>
                            <th>Phim & Suất chiếu</th>
                            <th>Ghế ngồi</th>
                            <th>Tổng tiền</th>
                            <th>Trạng thái</th>
                            <th style="text-align: right; width: 170px;">Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <%-- Dữ liệu động khi Backend truyền ${orderList} --%>
                            <c:when test="${not empty orderList}">
                                <c:forEach var="o" items="${orderList}">
                                    <tr>
                                        <td><strong style="color: #fcca46; font-family: monospace;">#${o.orderCode}</strong></td>
                                        <td>
                                            <div><strong>${o.customerName}</strong></div>
                                            <div style="font-size: 12px; color: #94a3b8;">${o.phone}</div>
                                        </td>
                                        <td>
                                            <div style="font-weight: 600;">${o.movieTitle}</div>
                                            <div style="font-size: 12px; color: #94a3b8;">🕒 ${o.startTime} - ${o.showDate} (${o.roomName})</div>
                                        </td>
                                        <td><span class="user-role-badge" style="background: rgba(252,202,70,0.15); color: #fcca46;">${o.seats}</span></td>
                                        <td><strong style="color: #fcca46;"><fmt:formatNumber value="${o.totalPrice}" type="number" groupingUsed="true"/>đ</strong></td>
                                        <td>
                                            <span class="badge-status ${o.status == 'PAID' ? 'active' : 'locked'}">
                                                ${o.status == 'PAID' ? 'Đã thanh toán' : (o.status == 'PENDING' ? 'Chờ duyệt' : 'Đã hủy')}
                                            </span>
                                        </td>
                                        <td style="text-align: right;">
                                            <a href="${pageContext.request.contextPath}/order/ticket-detail?id=${o.orderCode}" 
                                               class="btn-action edit" title="Xem chi tiết vé & QR">👁️</a>
                                            <c:if test="${o.status != 'CANCELLED'}">
                                                <button type="button" class="btn-action lock" title="Hủy vé" 
                                                        onclick="openCancelModal('${o.orderCode}', '${o.movieTitle}', '${o.seats}')">🚫</button>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>

                            <%-- Dữ liệu mẫu test giao diện chuẩn cho Frontend --%>
                            <c:otherwise>
                                <tr>
                                    <td><strong style="color: #fcca46; font-family: monospace;">#CNK-2026-88921</strong></td>
                                    <td>
                                        <div><strong>Nguyễn Văn An</strong></div>
                                        <div style="font-size: 12px; color: #94a3b8;">0912.345.678</div>
                                    </td>
                                    <td>
                                        <div style="font-weight: 600;">Avatar: Dòng Chảy Của Nước</div>
                                        <div style="font-size: 12px; color: #94a3b8;">🕒 09:15 - 16/10/2026 (Phòng 05)</div>
                                    </td>
                                    <td><span class="user-role-badge" style="background: rgba(252,202,70,0.15); color: #fcca46; border: 1px solid rgba(252,202,70,0.3);">VIP E5, VIP E6</span></td>
                                    <td><strong style="color: #fcca46;">220.000đ</strong></td>
                                    <td><span class="badge-status active">Đã thanh toán</span></td>
                                    <td style="text-align: right;">
                                        <a href="${pageContext.request.contextPath}/order/ticket-detail?id=CNK-2026-88921" class="btn-action edit" title="Xem chi tiết vé & QR">👁️</a>
                                        <button type="button" class="btn-action lock" title="Hủy vé này" onclick="openCancelModal('CNK-2026-88921', 'Avatar: Dòng Chảy Của Nước', 'VIP E5, VIP E6')">🚫</button>
                                    </td>
                                </tr>
                                <tr>
                                    <td><strong style="color: #fcca46; font-family: monospace;">#CNK-2026-77340</strong></td>
                                    <td>
                                        <div><strong>Trần Thị Mai</strong></div>
                                        <div style="font-size: 12px; color: #94a3b8;">0988.765.432</div>
                                    </td>
                                    <td>
                                        <div style="font-weight: 600;">Oppenheimer</div>
                                        <div style="font-size: 12px; color: #94a3b8;">🕒 20:00 - 10/10/2026 (Phòng 02)</div>
                                    </td>
                                    <td><span class="user-role-badge" style="background: rgba(252,202,70,0.15); color: #fcca46; border: 1px solid rgba(252,202,70,0.3);">STANDARD C7</span></td>
                                    <td><strong style="color: #fcca46;">85.000đ</strong></td>
                                    <td><span class="badge-status active">Đã thanh toán</span></td>
                                    <td style="text-align: right;">
                                        <a href="${pageContext.request.contextPath}/order/ticket-detail?id=CNK-2026-77340" class="btn-action edit" title="Xem chi tiết vé & QR">👁️</a>
                                        <button type="button" class="btn-action lock" title="Hủy vé này" onclick="openCancelModal('CNK-2026-77340', 'Oppenheimer', 'STANDARD C7')">🚫</button>
                                    </td>
                                </tr>
                                <tr>
                                    <td><strong style="color: #94a3b8; font-family: monospace;">#CNK-2026-66129</strong></td>
                                    <td>
                                        <div><strong>Lê Hoàng Long</strong></div>
                                        <div style="font-size: 12px; color: #94a3b8;">0903.111.222</div>
                                    </td>
                                    <td>
                                        <div style="font-weight: 600;">Dune: Hành Tinh Cát 2</div>
                                        <div style="font-size: 12px; color: #94a3b8;">🕒 18:30 - 05/10/2026 (Phòng 01)</div>
                                    </td>
                                    <td><span class="user-role-badge" style="background: rgba(255,255,255,0.06); color: #94a3b8;">B4, B5</span></td>
                                    <td><strong style="color: #94a3b8; text-decoration: line-through;">170.000đ</strong></td>
                                    <td><span class="badge-status locked">Đã hủy</span></td>
                                    <td style="text-align: right;">
                                        <button type="button" class="btn-action" style="opacity: 0.3; cursor: not-allowed;">✕</button>
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

    </div>
</main>

<%---- ================= 5. MODAL XÁC NHẬN HỦY VÉ ================= ----%>
<div id="cancelTicketModal" style="display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.75); z-index: 9999; align-items: center; justify-content: center;">
    <div style="background: #181d26; border: 1px solid rgba(239, 68, 68, 0.4); border-radius: 12px; padding: 24px; max-width: 450px; width: 90%; color: #fff; box-shadow: 0 10px 30px rgba(0,0,0,0.5);">
        <h3 style="margin-top: 0; color: #f87171; display: flex; align-items: center; gap: 8px;">
            ⚠️ Xác Nhận Hủy Vé
        </h3>
        <p style="font-size: 14px; color: #cbd5e1; line-height: 1.5;">
            Bạn có chắc chắn muốn hủy đơn vé <strong id="modalOrderCode" style="color: #fcca46;"></strong>?
        </p>
        <div style="background: rgba(0,0,0,0.3); padding: 10px 14px; border-radius: 6px; font-size: 13px; margin-bottom: 16px;">
            <div>Phim: <strong id="modalMovieTitle"></strong></div>
            <div>Ghế: <strong id="modalSeats"></strong></div>
        </div>

        <form action="${pageContext.request.contextPath}/admin/bookings/cancel" method="POST">
            <input type="hidden" name="orderCode" id="inputCancelOrderCode">
            <label style="display: block; font-size: 12px; color: #94a3b8; margin-bottom: 6px;">Lý do hủy vé (tùy chọn):</label>
            <input type="text" name="cancelReason" placeholder="Ví dụ: Khách yêu cầu đổi suất chiếu..." 
                   style="width: 100%; padding: 8px 12px; border-radius: 6px; background: #0f1217; border: 1px solid rgba(255,255,255,0.15); color: #fff; box-sizing: border-box; margin-bottom: 18px;">
            
            <div style="display: flex; justify-content: flex-end; gap: 10px;">
                <button type="button" onclick="closeCancelModal()" 
                        style="background: #272f3d; color: #cbd5e1; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer;">
                    Đóng
                </button>
                <button type="submit" 
                        style="background: #ef4444; color: #fff; border: none; padding: 8px 16px; border-radius: 6px; font-weight: 600; cursor: pointer;">
                    Xác Nhận Hủy Vé
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    function openCancelModal(code, movie, seats) {
        document.getElementById('modalOrderCode').textContent = '#' + code;
        document.getElementById('inputCancelOrderCode').value = code;
        document.getElementById('modalMovieTitle').textContent = movie;
        document.getElementById('modalSeats').textContent = seats;
        document.getElementById('cancelTicketModal').style.display = 'flex';
    }

    function closeCancelModal() {
        document.getElementById('cancelTicketModal').style.display = 'none';
    }
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
