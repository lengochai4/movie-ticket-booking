<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Đặt Vé Thành Công - Cinetik" scope="request"/>
<c:set var="extraCss" value="booking.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="booking-page-container">
    <div class="booking-main-layout" style="max-width: 860px; margin: 0 auto;">

        <%---- 1. FILM REEL TICKET BANNER ----%>
        <jsp:include page="/WEB-INF/views/booking/movie-banner.jsp" />

        <%---- 2. BƯỚC TIẾN TRÌNH: HOÀN TẤT BƯỚC 4 ----%>
        <nav class="booking-stepper">
            <div class="step-item completed">
                <span class="step-circle">✓</span>
                <span>Chọn ghế</span>
            </div>
            <div class="step-divider"></div>
            <div class="step-item completed">
                <span class="step-circle">✓</span>
                <span>Bắp nước</span>
            </div>
            <div class="step-divider"></div>
            <div class="step-item completed">
                <span class="step-circle">✓</span>
                <span>Thanh toán</span>
            </div>
            <div class="step-divider"></div>
            <div class="step-item active completed">
                <span class="step-circle">✓</span>
                <span>Thông tin vé</span>
            </div>
        </nav>

        <%---- 3. THÔNG BÁO CHÚC MỪNG HOÀNH TRÁNG ----%>
        <div style="background: linear-gradient(135deg, rgba(43, 138, 62, 0.22), rgba(25, 35, 25, 0.6)); border: 1.5px solid #69db7c; border-radius: 14px; padding: 24px; margin-bottom: 30px; text-align: center; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4);">
            <div style="font-size: 40px; margin-bottom: 8px;">🎉</div>
            <h1 style="font-family: 'Playfair Display', serif; font-size: 26px; color: #69db7c; margin: 0 0 8px 0;">
                Đặt Vé & Thanh Toán Thành Công!
            </h1>
            <p style="font-size: 14.5px; color: #e9ecef; margin: 0; line-height: 1.6;">
                Cảm ơn bạn đã lựa chọn rạp chiếu phim <strong>Cinetik</strong>. Đơn đặt vé của bạn đã được xác nhận thanh toán thành công và chuyển sang trạng thái <strong>ĐÃ BÁN (PAID)</strong>.
            </p>
        </div>

        <%---- 4. TẤM VÉ XEM PHIM ĐIỆN TỬ CỔ ĐIỂN TINH XẢO ----%>
        <div class="cinema-ticket-card" style="max-width: 600px; margin: 0 auto; box-shadow: 0 15px 45px rgba(0,0,0,0.65);">
            <div class="ticket-cutout-left"></div>
            <div class="ticket-cutout-right"></div>

            <div class="ticket-header-admit">
                <span class="ticket-badge-pill">★ VÉ XEM PHIM ĐIỆN TỬ CHÍNH THỨC ★</span>
                <h2 class="ticket-title-admit" style="font-size: 28px; margin-top: 8px;">ADMIT ONE</h2>
                <div class="ticket-subtitle-theater" style="letter-spacing: 2px;">
                    CINETIK CINEMA SYSTEM
                </div>
            </div>

            <hr class="ticket-perforation">

            <%---- Mã đơn hàng & Trạng thái vé ----%>
            <div style="display: flex; justify-content: space-between; align-items: center; background: rgba(0, 0, 0, 0.35); padding: 12px 16px; border-radius: 8px; margin-bottom: 16px; border: 1px solid rgba(255, 255, 255, 0.08);">
                <div>
                    <span style="font-size: 11px; color: #a89f91; text-transform: uppercase; letter-spacing: 0.5px;">MÃ ĐƠN HÀNG</span>
                    <div style="font-family: monospace; font-size: 18px; font-weight: 800; color: #fcca46;">
                        ${not empty bookingDetails.orderCode ? bookingDetails.orderCode : bookingId}
                    </div>
                </div>
                <div style="text-align: right;">
                    <span style="font-size: 11px; color: #a89f91; text-transform: uppercase; letter-spacing: 0.5px;">TRẠNG THÁI VÉ</span>
                    <div>
                        <span style="display: inline-block; background: rgba(43, 138, 62, 0.25); border: 1px solid #69db7c; color: #69db7c; font-size: 12.5px; font-weight: 700; padding: 3px 10px; border-radius: 20px;">
                            🟢 ĐÃ BÁN (PAID)
                        </span>
                    </div>
                </div>
            </div>

            <%---- Chi tiết suất chiếu, ghế và tổng tiền ----%>
            <c:if test="${not empty bookingDetails.movieTitle}">
                <div style="margin-bottom: 16px; padding: 12px 16px; background: rgba(0,0,0,0.25); border-radius: 8px; border: 1px solid rgba(252, 202, 70, 0.2);">
                    <div style="font-weight: 700; color: #fcca46; font-size: 16px;">${bookingDetails.movieTitle}</div>
                    <div style="font-size: 13px; color: #d0c7b7; margin-top: 4px;">
                        ${bookingDetails.cinemaName} - ${bookingDetails.roomName}
                    </div>
                    <div style="font-size: 13px; color: #a89f91; margin-top: 4px;">
                        Suất chiếu: <fmt:formatDate value="${bookingDetails.showStart}" pattern="HH:mm - dd/MM/yyyy"/>
                    </div>
                    <div style="font-size: 13px; color: #ffffff; margin-top: 4px;">
                        Ghế: <strong style="color: #fcca46;">${bookingDetails.seats}</strong>
                        <c:if test="${not empty bookingDetails.combos}"> | Bắp nước: ${bookingDetails.combos}</c:if>
                    </div>
                    <div style="font-size: 13.5px; color: #69db7c; margin-top: 6px; font-weight: 700;">
                        Tổng thanh toán: <fmt:formatNumber value="${bookingDetails.totalPrice}" type="number"/>đ
                        <c:if test="${bookingDetails.pointsEarned > 0}"> 
                            <span style="color: #fcca46; font-size: 12px; font-weight: normal; margin-left: 6px;">(★ Tích lũy +${bookingDetails.pointsEarned} điểm)</span>
                        </c:if>
                    </div>
                </div>
            </c:if>

            <%---- Mã QR Check-in vào rạp ----%>
            <div style="text-align: center; margin: 20px 0; padding: 16px; background: #ffffff; border-radius: 12px; display: inline-block; width: fit-content; margin: 0 auto 20px auto;">
                <img src="https://api.qrserver.com/v1/create-qr-code/?size=160x160&data=CINETIK_${bookingId}&color=1a1615" 
                     alt="Mã QR vé điện tử" 
                     style="display: block; width: 160px; height: 160px; margin: 0 auto;">
                <div style="font-family: monospace; font-size: 11px; font-weight: 700; color: #1a1615; margin-top: 8px; letter-spacing: 1px;">
                    MÃ SOÁT VÉ: ${bookingId}
                </div>
            </div>

            <p style="font-size: 12.5px; color: #b5a995; text-align: center; margin: 0 0 20px 0; line-height: 1.5;">
                Quý khách vui lòng xuất trình mã QR này tại cổng soát vé hoặc máy quét tự động trước giờ chiếu 15 phút.
            </p>

            <%---- Mã vạch Barcode ----%>
            <div class="ticket-barcode-wrap" style="margin-top: 10px;">
                <div class="ticket-barcode-stripes"></div>
                <div class="ticket-barcode-serial">№ CNK-${bookingId}-VERIFIED-PAID</div>
            </div>
        </div>

        <%---- CÁC NÚT ĐIỀU HƯỚNG TIỆN ÍCH ----%>
        <div style="display: flex; gap: 14px; justify-content: center; margin-top: 36px; flex-wrap: wrap;">
            <a href="${pageContext.request.contextPath}/" 
               class="btn-ticket-submit" 
               style="text-decoration: none; padding: 14px 28px; width: auto; font-size: 14.5px;">
                🏠 Về Trang Chủ
            </a>
            <a href="${pageContext.request.contextPath}/movies" 
               class="btn-ticket-back" 
               style="text-decoration: none; padding: 14px 28px; width: auto; font-size: 14.5px;">
                🎬 Đặt Thêm Vé Phim Khác
            </a>
            <button type="button" 
                    onclick="window.print()" 
                    class="btn-ticket-back" 
                    style="padding: 14px 24px; width: auto; font-size: 14.5px; background: rgba(255,255,255,0.08); border-color: rgba(255,255,255,0.2);">
                🖨️ In Vé Điện Tử
            </button>
        </div>

    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
