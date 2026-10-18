<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Xác Nhận & Thanh Toán - Cinetik" scope="request"/>
<c:set var="extraCss" value="booking.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="booking-page-container">
    <div class="booking-main-layout">


        <%---- 2. FILM REEL TICKET BANNER ----%>
        <jsp:include page="/WEB-INF/views/booking/movie-banner.jsp" />

        <%---- 3. BƯỚC TIẾN TRÌNH ----%>
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
            <div class="step-item active">
                <span class="step-circle">3</span>
                <span>Thanh toán</span>
            </div>
            <div class="step-divider"></div>
            <div class="step-item">
                <span class="step-circle">4</span>
                <span>Thông tin vé</span>
            </div>
        </nav>

        <c:if test="${not empty successMessage}">
            <div style="background: rgba(105, 219, 124, 0.15); border: 1px solid #69db7c; color: #69db7c; border-radius: 8px; padding: 14px 20px; margin-bottom: 24px; font-weight: 600; text-align: center; font-size: 15px;">
                🎉 ${successMessage}
            </div>
        </c:if>

        <%---- 4. NỘI DUNG CHÍNH: FORM THÔNG TIN + CỔNG THANH TOÁN ----%>
        <div class="booking-content-grid">

            <%---- CỘT TRÁI: FORM LIÊN HỆ & PHƯƠNG THỨC THANH TOÁN ----%>
            <section class="booking-left-panel">
                
                <form action="${pageContext.request.contextPath}/booking/complete" method="POST" id="checkoutForm">
                    <input type="hidden" name="showtimeId" value="${param.showtimeId}">
                    <input type="hidden" name="selectedSeats" value="${param.selectedSeats}">
                    <input type="hidden" name="totalSeatPrice" value="${param.totalSeatPrice}">
                    <input type="hidden" name="totalComboPrice" value="${param.totalComboPrice}">
                    <input type="hidden" name="finalTotalPrice" value="${param.finalTotalPrice}">

                    <%---- 1. THÔNG TIN NGƯỜI NHẬN VÉ ----%>
                    <div class="checkout-section-block">
                        <h3>1. Thông Tin Nhận Vé Điện Tử</h3>
                        <p style="font-size: 13px; color: #8c8275; margin: -10px 0 10px 0;">
                            Mã vé điện tử và mã QR vào phòng chiếu sẽ được gửi tới Email & Số điện thoại bên dưới.
                        </p>

                        <div class="checkout-form-grid">
                            <div class="checkout-input-group">
                                <label for="contactName">Họ và tên người nhận vé *</label>
                                <input type="text" 
                                       id="contactName" 
                                       name="contactName" 
                                       class="checkout-input" 
                                       value="${not empty sessionScope.loginUser.fullName ? sessionScope.loginUser.fullName : 'Nguyễn Thị Lan Phương'}" 
                                       required>
                            </div>

                            <div class="checkout-input-group">
                                <label for="contactPhone">Số điện thoại *</label>
                                <input type="tel" 
                                       id="contactPhone" 
                                       name="contactPhone" 
                                       class="checkout-input" 
                                       value="${not empty sessionScope.loginUser.phone ? sessionScope.loginUser.phone : '0988123456'}" 
                                       pattern="[0-9]{10}"
                                       placeholder="0912 345 678"
                                       required>
                            </div>

                            <div class="checkout-input-group" style="grid-column: 1 / -1;">
                                <label for="contactEmail">Email nhận vé điện tử *</label>
                                <input type="email" 
                                       id="contactEmail" 
                                       name="contactEmail" 
                                       class="checkout-input" 
                                       value="${not empty sessionScope.loginUser.email ? sessionScope.loginUser.email : 'thilanphuong24@gmail.com'}" 
                                       placeholder="name@example.com"
                                       required>
                            </div>
                        </div>
                    </div>

                    <%---- 2. PHƯƠNG THỨC THANH TOÁN (VNPAY & MOMO) ----%>
                    <div class="checkout-section-block">
                        <h3>2. Chọn Phương Thức Thanh Toán</h3>
                        
                        <div class="payment-methods-grid" style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
                            <%---- VNPAY ----%>
                            <label class="payment-method-card selected" id="card-vnpay" style="background: #25201b; border: 2px solid #fcca46; border-radius: 12px; padding: 14px 16px; display: flex; align-items: center; gap: 12px; cursor: pointer; transition: all 0.2s ease;">
                                <input type="radio" name="paymentMethod" value="VNPAY" checked style="accent-color: #fcca46; width: 18px; height: 18px; cursor: pointer; flex-shrink: 0;">
                                <div style="width: 44px; height: 44px; background: #ffffff; border-radius: 10px; display: flex; flex-direction: column; align-items: center; justify-content: center; flex-shrink: 0; box-shadow: 0 3px 8px rgba(0,0,0,0.35);">
                                    <span style="font-family: 'Arial Black', sans-serif; font-size: 15px; font-weight: 900; color: #005baa; line-height: 1; letter-spacing: 0.5px;">VN</span>
                                    <span style="font-family: 'Arial Black', sans-serif; font-size: 13px; font-weight: 900; color: #e11b22; line-height: 1; letter-spacing: 0.5px; margin-top: 2px;">PAY</span>
                                </div>
                                <div style="flex: 1; display: flex; flex-direction: column; gap: 2px; min-width: 0;">
                                    <div style="display: flex; align-items: center; justify-content: space-between; gap: 6px;">
                                        <span style="font-size: 14.5px; font-weight: 700; color: #ffffff; white-space: nowrap;">Cổng VNPAY</span>
                                        <span style="font-size: 10px; font-weight: 600; color: #4da3ff; background: rgba(0, 91, 170, 0.25); border: 1px solid rgba(0, 91, 170, 0.5); padding: 2px 6px; border-radius: 10px; flex-shrink: 0;">Phổ biến</span>
                                    </div>
                                    <span style="font-size: 12px; color: #a89f91; line-height: 1.3;">Quét mã QR & Ngân hàng</span>
                                </div>
                            </label>

                            <%---- MOMO ----%>
                            <label class="payment-method-card" id="card-momo" style="background: #191617; border: 2px solid #3c3733; border-radius: 12px; padding: 14px 16px; display: flex; align-items: center; gap: 12px; cursor: pointer; transition: all 0.2s ease;">
                                <input type="radio" name="paymentMethod" value="MOMO" style="accent-color: #d82d8b; width: 18px; height: 18px; cursor: pointer; flex-shrink: 0;">
                                <div style="width: 44px; height: 44px; background: linear-gradient(135deg, #d82d8b, #a50064); border-radius: 10px; display: flex; flex-direction: column; align-items: center; justify-content: center; flex-shrink: 0; box-shadow: 0 3px 8px rgba(165,0,100,0.35);">
                                    <span style="font-family: -apple-system, BlinkMacSystemFont, 'Nunito', 'Segoe UI', sans-serif; font-size: 15px; font-weight: 900; color: #ffffff; line-height: 1; letter-spacing: -0.5px;">mo</span>
                                    <span style="font-family: -apple-system, BlinkMacSystemFont, 'Nunito', 'Segoe UI', sans-serif; font-size: 15px; font-weight: 900; color: #ffffff; line-height: 1; letter-spacing: -0.5px; margin-top: 2px;">mo</span>
                                </div>
                                <div style="flex: 1; display: flex; flex-direction: column; gap: 2px; min-width: 0;">
                                    <div style="display: flex; align-items: center; justify-content: space-between; gap: 6px;">
                                        <span style="font-size: 14.5px; font-weight: 700; color: #ffffff; white-space: nowrap;">Ví MoMo</span>
                                        <span style="font-size: 10px; font-weight: 600; color: #ff69b4; background: rgba(216, 45, 139, 0.25); border: 1px solid rgba(216, 45, 139, 0.5); padding: 2px 6px; border-radius: 10px; flex-shrink: 0;">Khuyên dùng</span>
                                    </div>
                                    <span style="font-size: 12px; color: #a89f91; line-height: 1.3;">Thanh toán tức thì MoMo</span>
                                </div>
                            </label>
                        </div>
                    </div>

                    <%---- 3. ĐIỀU KHOẢN ----%>
                    <div style="font-size: 13px; color: #b5a995; display: flex; align-items: center; gap: 8px;">
                        <input type="checkbox" id="agreeTerms" required checked style="accent-color: #780000; width: 16px; height: 16px;">
                        <label for="agreeTerms">
                            Tôi xác nhận các thông tin trên là chính xác và đồng ý với 
                            <a href="#" style="color: #fcca46;">Điều khoản giao dịch</a> của rạp Cinetik.
                        </label>
                    </div>

                    <div style="margin-top: 24px; display: flex; flex-direction: column; gap: 8px;">
                        <button type="submit" class="btn-proceed" style="font-size: 15px; padding: 14px 16px;">
                            🔒 Xác Nhận & Thanh Toán Ngay
                        </button>
                        <a href="${pageContext.request.contextPath}/booking/combo?showtimeId=${param.showtimeId}&selectedSeats=${param.selectedSeats}&totalSeatPrice=${param.totalSeatPrice}" class="btn-back-step">
                            ← Quay lại chọn bắp nước
                        </a>
                    </div>

                </form>
            </section>

            <%---- CỘT PHẢI: TỔNG KẾT ĐƠN HÀNG CHI TIẾT ----%>
            <aside class="cinema-ticket-card">
                <div class="ticket-cutout-left"></div>
                <div class="ticket-cutout-right"></div>

                <div class="ticket-header-admit">
                    <span class="ticket-badge-pill">★ Cinetik Booking ★</span>
                    <h2 class="ticket-title-admit">ADMIT ONE</h2>
                    <div class="ticket-subtitle-theater">
                        ${not empty cinema.name ? cinema.name : 'Cinetik'}
                    </div>
                </div>
                <hr class="ticket-perforation">

                <%---- Thông tin phim ----%>
                <div class="ticket-info-group">
                    <span class="ticket-field-label">PHIM CHIẾU</span>
                    <div class="ticket-movie-title">${movie.title}</div>
                </div>

                <%---- 3. Bảng Suất chiếu, Ngày, Phòng chiếu (3 cột riêng biệt) ----%>
                <div class="ticket-meta-box">
                    <div class="ticket-meta-item">
                        <span class="ticket-field-label">SUẤT</span>
                        <div class="ticket-meta-val">
                            ${not empty formattedTime ? formattedTime : '09:15'}
                        </div>
                    </div>
                    <div class="ticket-meta-item">
                        <span class="ticket-field-label">NGÀY</span>
                        <div class="ticket-meta-val">
                            ${not empty formattedDate ? formattedDate : '16/10/2026'}
                        </div>
                    </div>
                    <div class="ticket-meta-item">
                        <span class="ticket-field-label">PHÒNG</span>
                        <div class="ticket-meta-val">
                            ${not empty roomNumber ? roomNumber : '05'}
                        </div>
                    </div>
                </div>

                <%---- 4. Ghế đã chọn dạng thẻ nổi bật ----%>
                <div class="ticket-info-group">
                    <span class="ticket-field-label">GHẾ ĐÃ CHỌN</span>
                    <div class="ticket-seats-list">
                        <c:choose>
                            <c:when test="${not empty param.selectedSeats}">
                                <c:forTokens items="${param.selectedSeats}" delims="," var="s">
                                    <span class="ticket-seat-tag">Ghế ${s}</span>
                                </c:forTokens>
                            </c:when>
                            <c:otherwise>
                                <span class="ticket-seat-tag">Ghế B5</span>
                                <span class="ticket-seat-tag">Ghế B6</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <%---- Chi tiết tạm tính ----%>
                <div class="ticket-pricing-list">
                    <div class="ticket-pricing-row">
                        <span>Tiền vé phim:</span>
                        <strong id="checkoutSeatPrice">${not empty param.totalSeatPrice ? param.totalSeatPrice : '110.000'}đ</strong>
                    </div>
                    <div class="ticket-pricing-row">
                        <span>Tiền bắp nước:</span>
                        <strong id="checkoutComboPrice">${not empty param.totalComboPrice ? param.totalComboPrice : '0'}đ</strong>
                    </div>
                </div>

                <%---- 5. Khung tổng cộng tiền thanh toán sang trọng ----%>
                <div class="ticket-total-box">
                    <span class="ticket-total-label">TỔNG CỘNG</span>
                    <span class="ticket-total-amount" id="checkoutFinalPrice">
                        ${not empty param.finalTotalPrice ? param.finalTotalPrice : '110.000'}đ
                    </span>
                </div>

                <%---- Nút xác nhận đặt vé ----%>
                <div>
                    <button type="submit" form="checkoutForm" class="btn-ticket-submit">
                        🔒 XÁC NHẬN & THANH TOÁN
                    </button>
                    <a href="${pageContext.request.contextPath}/booking/combo?showtimeId=${param.showtimeId}&selectedSeats=${param.selectedSeats}&totalSeatPrice=${param.totalSeatPrice}" class="btn-ticket-back">
                        ← Quay lại chọn bắp nước
                    </a>
                </div>

                <%---- Mã vạch Barcode & Mã số vé ----%>
                <div class="ticket-barcode-wrap">
                    <div class="ticket-barcode-stripes"></div>
                    <div class="ticket-barcode-serial">№ CNK-2026-88921-VIP</div>
                </div>
            </aside>

        </div>

    </div>
</main>

<script>
    const cardVnpay = document.getElementById('card-vnpay');
    const cardMomo = document.getElementById('card-momo');

    function selectPaymentMethod(method) {
        if (!cardVnpay || !cardMomo) return;
        if (method === 'VNPAY') {
            cardVnpay.classList.add('selected');
            cardVnpay.style.borderColor = '#fcca46';
            cardVnpay.style.background = '#25201b';
            cardVnpay.querySelector('input[type="radio"]').checked = true;

            cardMomo.classList.remove('selected');
            cardMomo.style.borderColor = '#3c3733';
            cardMomo.style.background = '#191617';
            cardMomo.querySelector('input[type="radio"]').checked = false;
        } else {
            cardMomo.classList.add('selected');
            cardMomo.style.borderColor = '#d82d8b';
            cardMomo.style.background = '#261620';
            cardMomo.querySelector('input[type="radio"]').checked = true;

            cardVnpay.classList.remove('selected');
            cardVnpay.style.borderColor = '#3c3733';
            cardVnpay.style.background = '#1c1917';
            cardVnpay.querySelector('input[type="radio"]').checked = false;
        }
    }

    if (cardVnpay) {
        cardVnpay.addEventListener('click', () => selectPaymentMethod('VNPAY'));
    }
    if (cardMomo) {
        cardMomo.addEventListener('click', () => selectPaymentMethod('MOMO'));
    }

    // Định dạng tiền tệ thống nhất với dấu chấm ngăn cách
    function formatVND(n) {
        if (!n || n === 0 || n === '0' || n === '0đ') return '0đ';
        let clean = n.toString().replace(/[^0-9]/g, '');
        if (!clean || clean === '0') return '0đ';
        return clean.replace(/\B(?=(\d{3})+(?!\d))/g, ".") + "đ";
    }

    const spEl = document.getElementById('checkoutSeatPrice');
    const cpEl = document.getElementById('checkoutComboPrice');
    const fpEl = document.getElementById('checkoutFinalPrice');
    if (spEl) spEl.textContent = formatVND(spEl.textContent);
    if (cpEl) cpEl.textContent = formatVND(cpEl.textContent);
    if (fpEl) fpEl.textContent = formatVND(fpEl.textContent);
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
