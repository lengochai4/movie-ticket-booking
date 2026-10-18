<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Vé Điện Tử - Cinetik" scope="request"/>
<c:set var="extraCss" value="booking.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="booking-page-container">
    <div class="eticket-standalone-layout">

        <%---- 1. Thanh công cụ phía trên vé ----%>
        <div class="eticket-action-bar">
            <a href="${pageContext.request.contextPath}/order/history" class="btn-ticket-back" style="margin: 0;">
                ← Quay lại lịch sử đặt vé
            </a>
            <button type="button" class="btn-ticket-print-gold" onclick="window.print()">
                🖨️ In Vé / Lưu PDF
            </button>
        </div>

        <%---- 2. TẤM VÉ XEM PHIM CỔ ĐIỂN TINH TẾ (CHÍNH XÁC NHƯ BÊN ĐẶT VÉ) ----%>
        <article class="cinema-ticket-card" id="eTicketCard" style="position: relative; top: 0;">
            <%---- Hai vết cắt khuyết bán nguyệt mép trên ----%>
            <div class="ticket-cutout-left"></div>
            <div class="ticket-cutout-right"></div>

            <%---- Header vé phong cách rạp chiếu phim thanh lịch ----%>
            <div class="ticket-header-admit">
                <span class="ticket-badge-pill">★ Cinetik Booking ★</span>
                <h1 class="ticket-title-admit">ADMIT ONE</h1>
                <div class="ticket-subtitle-theater">
                    ${not empty order.cinemaName ? order.cinemaName : 'Cinetik'}
                </div>
            </div>

            <%---- Đường đứt nét xé vé nối 2 vết cắt bán nguyệt ----%>
            <hr class="ticket-perforation">

            <%---- Tiêu đề & Thông tin phim ----%>
            <div class="ticket-info-group">
                <span class="ticket-field-label">PHIM / MOVIE</span>
                <div class="ticket-movie-title" style="font-size: 19px; margin-bottom: 6px;">
                    ${not empty order.movieTitle ? order.movieTitle : 'Avatar: Dòng Chảy Của Nước'}
                </div>
                <span class="ticket-seat-tag" style="font-size: 11px; padding: 2px 8px; font-weight: 600;">
                    ${not empty order.movieFormat ? order.movieFormat : '3D IMAX Phụ Đề'} · ${not empty order.ageRating ? order.ageRating : 'T16'}
                </span>
            </div>

            <%---- Bảng 3 ô Suất - Ngày - Phòng cổ điển ----%>
            <div class="ticket-meta-box" style="margin-top: 14px;">
                <div class="ticket-meta-item">
                    <span class="ticket-field-label">Suất chiếu</span>
                    <span class="ticket-meta-val">${not empty order.startTime ? order.startTime : '09:15'}</span>
                </div>
                <div class="ticket-meta-item">
                    <span class="ticket-field-label">Ngày</span>
                    <span class="ticket-meta-val">${not empty order.showDate ? order.showDate : '16/10/2026'}</span>
                </div>
                <div class="ticket-meta-item">
                    <span class="ticket-field-label">Phòng</span>
                    <span class="ticket-meta-val">${not empty order.roomNumber ? order.roomNumber : '05'}</span>
                </div>
            </div>

            <%---- Danh sách ghế đã chọn ----%>
            <div class="ticket-info-group" style="margin-top: 10px;">
                <span class="ticket-field-label">GHẾ ĐÃ CHỌN / SEATS</span>
                <div class="ticket-seats-list">
                    <c:choose>
                        <c:when test="${not empty order.seatPills}">
                            <c:forEach items="${order.seatPills}" var="seat">
                                <span class="ticket-seat-tag">${seat}</span>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <span class="ticket-seat-tag">VIP E5</span>
                            <span class="ticket-seat-tag">VIP E6</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <%---- Chi tiết bắp nước kèm theo ----%>
            <c:if test="${not empty order.combos || empty order}">
                <div class="ticket-pricing-list">
                    <div class="ticket-pricing-row">
                        <span>Bắp nước kèm theo:</span>
                        <strong>${not empty order.combos ? order.combos : 'Combo Solo x 1, Bắp Rang Bơ x 1'}</strong>
                    </div>
                </div>
            </c:if>

            <%---- Khung đen tổng tiền viền kim loại sang trọng ----%>
            <div class="ticket-total-box" style="margin: 16px 0 16px 0;">
                <span class="ticket-total-label">TỔNG CỘNG</span>
                <span class="ticket-total-amount">
                    <c:choose>
                        <c:when test="${not empty order.totalPrice}">
                            <fmt:formatNumber value="${order.totalPrice}" type="number" groupingUsed="true"/>đ
                        </c:when>
                        <c:otherwise>220.000đ</c:otherwise>
                    </c:choose>
                </span>
            </div>

            <%---- Cặp vết cắt khuyết bán nguyệt thứ 2 & Đường xé vé chia phần QR ----%>
            <div class="ticket-cutout-left" style="top: auto; bottom: 345px;"></div>
            <div class="ticket-cutout-right" style="top: auto; bottom: 345px;"></div>
            <hr class="ticket-perforation" style="margin: 4px -24px 16px -24px;">

            <%---- PHẦN QUÉT MÃ QR SOÁT VÉ VÀO CỬA (GIỮ NGUYÊN MÃ QR & BARCODE) ----%>
            <section class="ticket-qr-section">
                <p class="ticket-qr-instruction">
                    📱 Đưa mã QR dưới đây cho nhân viên soát vé tại quầy hoặc máy quét cửa phòng chiếu
                </p>

                <%---- Khung trắng giữ nguyên mã QR sắc nét quét được thật ----%>
                <div class="ticket-qr-card">
                    <img src="https://api.qrserver.com/v1/create-qr-code/?size=190x190&data=${not empty order.orderCode ? order.orderCode : 'CNK-2026-88921'}&color=110e0c" 
                         alt="Mã QR Soát Vé ${not empty order.orderCode ? order.orderCode : 'CNK-2026-88921'}">
                </div>

                <%---- Mã vạch Barcode tuyến tính & Số vé serial ----%>
                <div class="ticket-barcode-wrap" style="border-top: none; margin-top: 14px; padding-top: 0;">
                    <div class="ticket-barcode-stripes"></div>
                    <div class="ticket-barcode-serial">
                        № ${not empty order.orderCode ? order.orderCode : 'CNK-2026-88921'}-VIP
                    </div>
                </div>

                <p class="ticket-qr-notice">
                    * Vé điện tử có giá trị vào phòng chiếu 01 lần duy nhất.<br>
                    Vui lòng có mặt tại rạp trước giờ chiếu ít nhất 15 phút.
                </p>
            </section>
        </article>

    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
