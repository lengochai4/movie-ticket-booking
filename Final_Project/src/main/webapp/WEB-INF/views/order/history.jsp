<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Lịch Sử Đặt Vé - Cinetik" scope="request"/>
<c:set var="extraCss" value="order.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="order-page-container">
    <div class="order-wrapper">

        <%---- 1. Header Trang Lịch Sử Đơn Hàng ----%>
        <div class="order-header-section">
            <h1 class="order-page-title">
                <span class="title-icon">LỊCH SỬ ĐẶT VÉ CỦA BẠN</span>
            </h1>
            <p class="order-page-subtitle">
                Xem lại danh sách vé đã đặt, thông tin suất chiếu và mã QR vào phòng chiếu.
            </p>
        </div>

        <%---- 2. Bộ Lọc Trạng Thái Đơn Hàng ----%>
        <div class="order-filter-tabs">
            <button type="button" class="order-tab-btn active" onclick="filterOrders('ALL', this)">
                Tất cả đơn (<span id="countAll">3</span>)
            </button>
            <button type="button" class="order-tab-btn" onclick="filterOrders('PAID', this)">
                Đã thanh toán / Vé hợp lệ
            </button>
            <button type="button" class="order-tab-btn" onclick="filterOrders('PENDING', this)">
                Chờ thanh toán
            </button>
            <button type="button" class="order-tab-btn" onclick="filterOrders('CANCELLED', this)">
                Đã hủy
            </button>
        </div>

        <%---- 3. Danh Sách Thẻ Đơn Hàng ----%>
        <div class="order-cards-list" id="orderCardsContainer">

            <c:choose>
                <%-- KHI CÓ DỮ LIỆU ĐỘNG TỪ BACKEND --%>
                <c:when test="${not empty orderList}">
                    <c:forEach items="${orderList}" var="o">
                        <div class="order-card" data-status="${o.status}">
                            <%---- Header Đơn Vé ----%>
                            <div class="order-card-header">
                                <div class="order-meta-left">
                                    <span class="order-code-badge">#${o.orderCode}</span>
                                    <span class="order-time-text">Đặt lúc: ${o.createdDate}</span>
                                </div>
                                <span class="order-status-pill ${o.status == 'PAID' ? 'success' : (o.status == 'PENDING' ? 'pending' : 'cancelled')}">
                                    <c:choose>
                                        <c:when test="${o.status == 'PAID'}">● Đã thanh toán</c:when>
                                        <c:when test="${o.status == 'PENDING'}">⏳ Chờ thanh toán</c:when>
                                        <c:otherwise>✕ Đã hủy</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>

                            <%---- Thân Đơn Vé ----%>
                            <div class="order-card-body">
                                <img src="${o.posterURL}" alt="${o.movieTitle}" class="order-poster-thumb">
                                
                                <div class="order-movie-info">
                                    <span class="order-movie-tag">${not empty o.movieFormat ? o.movieFormat : '2D Phụ Đề'}</span>
                                    <h3 class="order-movie-title">${o.movieTitle}</h3>
                                    
                                    <div class="order-showtime-meta">
                                        <span class="order-showtime-meta-item">🕒 <strong>${o.startTime}</strong> - ${o.showDate}</span>
                                        <span class="order-showtime-meta-item">📍 ${o.cinemaName}</span>
                                        <span class="order-showtime-meta-item">🚪 ${o.roomName}</span>
                                    </div>

                                    <div class="order-seats-wrap">
                                        <span>Ghế đã chọn:</span>
                                        <span class="order-seat-badge">${o.seats}</span>
                                    </div>

                                    <c:if test="${not empty o.combos}">
                                        <div class="order-combos-text">
                                            🍿 Bắp nước: <strong>${o.combos}</strong>
                                        </div>
                                    </c:if>
                                </div>

                                <div class="order-card-action">
                                    <span class="order-total-label">Tổng thanh toán</span>
                                    <span class="order-total-price">
                                        <fmt:formatNumber value="${o.totalPrice}" type="number" groupingUsed="true"/>đ
                                    </span>
                                    <a href="${pageContext.request.contextPath}/order/ticket-detail?id=${o.orderCode}" class="btn-view-ticket">
                                        Xem vé & QR →
                                    </a>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>

                <%-- KHI CHƯA CÓ BACKEND: HIỂN THỊ DỮ LIỆU MẪU ĐỂ FRONTEND KIỂM THỬ GIAO DIỆN --%>
                <c:otherwise>
                    <%---- Mẫu 1: Đơn vé đã thanh toán thành công ----%>
                    <div class="order-card" data-status="PAID">
                        <div class="order-card-header">
                            <div class="order-meta-left">
                                <span class="order-code-badge">#CNK-2026-88921</span>
                                <span class="order-time-text">Đặt lúc: 08:30 · 16/10/2026</span>
                            </div>
                            <span class="order-status-pill success">● Đã thanh toán</span>
                        </div>
                        <div class="order-card-body">
                            <img src="https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=300&auto=format&fit=crop" 
                                 alt="Avatar: Dòng Chảy Của Nước" class="order-poster-thumb">
                            <div class="order-movie-info">
                                <span class="order-movie-tag">3D IMAX Phụ Đề · T16</span>
                                <h3 class="order-movie-title">Avatar: Dòng Chảy Của Nước</h3>
                                <div class="order-showtime-meta">
                                    <span class="order-showtime-meta-item">🕒 <strong>09:15</strong> - 16/10/2026</span>
                                    <span class="order-showtime-meta-item">📍 Cinetik</span>
                                    <span class="order-showtime-meta-item">🚪 Phòng chiếu 05</span>
                                </div>
                                <div class="order-seats-wrap">
                                    <span>Ghế ngồi:</span>
                                    <span class="order-seat-badge">VIP E5</span>
                                    <span class="order-seat-badge">VIP E6</span>
                                </div>
                                <div class="order-combos-text">
                                    🍿 Bắp nước: <strong>Combo Solo x 1, Bắp Rang Bơ x 1</strong>
                                </div>
                            </div>
                            <div class="order-card-action">
                                <span class="order-total-label">Tổng thanh toán</span>
                                <span class="order-total-price">220.000đ</span>
                                <a href="${pageContext.request.contextPath}/order/ticket-detail?id=CNK-2026-88921" class="btn-view-ticket">
                                    Xem vé & QR →
                                </a>
                            </div>
                        </div>
                    </div>

                    <%---- Mẫu 2: Đơn vé đã thanh toán trước đó ----%>
                    <div class="order-card" data-status="PAID">
                        <div class="order-card-header">
                            <div class="order-meta-left">
                                <span class="order-code-badge">#CNK-2026-77340</span>
                                <span class="order-time-text">Đặt lúc: 19:15 · 10/10/2026</span>
                            </div>
                            <span class="order-status-pill success">● Đã xem</span>
                        </div>
                        <div class="order-card-body">
                            <img src="https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=300&auto=format&fit=crop" 
                                 alt="Oppenheimer" class="order-poster-thumb">
                            <div class="order-movie-info">
                                <span class="order-movie-tag">2D Phụ Đề Tiếng Việt · T18</span>
                                <h3 class="order-movie-title">Oppenheimer</h3>
                                <div class="order-showtime-meta">
                                    <span class="order-showtime-meta-item">🕒 <strong>20:00</strong> - 10/10/2026</span>
                                    <span class="order-showtime-meta-item">📍 Cinetik Landmark 81</span>
                                    <span class="order-showtime-meta-item">🚪 Phòng chiếu 02</span>
                                </div>
                                <div class="order-seats-wrap">
                                    <span>Ghế ngồi:</span>
                                    <span class="order-seat-badge">STANDARD C7</span>
                                </div>
                            </div>
                            <div class="order-card-action">
                                <span class="order-total-label">Tổng thanh toán</span>
                                <span class="order-total-price">85.000đ</span>
                                <a href="${pageContext.request.contextPath}/order/ticket-detail?id=CNK-2026-77340" class="btn-view-ticket">
                                    Xem lại vé →
                                </a>
                            </div>
                        </div>
                    </div>

                    <%---- Mẫu 3: Đơn vé đã hủy do hết hạn giữ chỗ ----%>
                    <div class="order-card" data-status="CANCELLED">
                        <div class="order-card-header">
                            <div class="order-meta-left">
                                <span class="order-code-badge">#CNK-2026-66129</span>
                                <span class="order-time-text">Đặt lúc: 14:00 · 05/10/2026</span>
                            </div>
                            <span class="order-status-pill cancelled">✕ Đã hủy</span>
                        </div>
                        <div class="order-card-body">
                            <img src="https://images.unsplash.com/photo-1518676599903-883907c0800b?w=300&auto=format&fit=crop" 
                                 alt="Dune: Part Two" class="order-poster-thumb">
                            <div class="order-movie-info">
                                <span class="order-movie-tag">2D Phụ Đề Tiếng Việt · T16</span>
                                <h3 class="order-movie-title">Dune: Hành Tinh Cát - Phần 2</h3>
                                <div class="order-showtime-meta">
                                    <span class="order-showtime-meta-item">🕒 <strong>18:30</strong> - 05/10/2026</span>
                                    <span class="order-showtime-meta-item">📍 Cinetik Thảo Điền</span>
                                    <span class="order-showtime-meta-item">🚪 Phòng chiếu 01</span>
                                </div>
                                <div class="order-seats-wrap">
                                    <span>Ghế ngồi:</span>
                                    <span class="order-seat-badge">STANDARD B4</span>
                                    <span class="order-seat-badge">STANDARD B5</span>
                                </div>
                            </div>
                            <div class="order-card-action">
                                <span class="order-total-label">Tổng đơn hàng</span>
                                <span class="order-total-price" style="color: #94a3b8; text-decoration: line-through;">170.000đ</span>
                                <button type="button" class="btn-view-ticket" style="background: #252d3a; color: #94a3b8; box-shadow: none; cursor: not-allowed;">
                                    Đơn đã hủy
                                </button>
                            </div>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>

        </div>

    </div>
</main>

<script>
    // Xử lý bộ lọc trạng thái đơn hàng phía client
    function filterOrders(status, button) {
        // Cập nhật tab active
        document.querySelectorAll('.order-tab-btn').forEach(btn => btn.classList.remove('active'));
        button.classList.add('active');

        // Lọc các thẻ đơn
        const cards = document.querySelectorAll('.order-card');
        cards.forEach(card => {
            const cardStatus = card.getAttribute('data-status');
            if (status === 'ALL' || cardStatus === status) {
                card.style.display = 'flex';
            } else {
                card.style.display = 'none';
            }
        });
    }
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
