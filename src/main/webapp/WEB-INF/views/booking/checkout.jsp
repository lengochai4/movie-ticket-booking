<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Xác Nhận & Thanh Toán - Cinetik" scope="request"/>
<c:set var="extraCss" value="booking.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="booking-page-container">
    <div class="booking-main-layout">

        <%---- 1. FILM REEL TICKET BANNER ----%>
        <jsp:include page="/WEB-INF/views/booking/movie-banner.jsp" />

        <%---- 2. BƯỚC TIẾN TRÌNH ----%>
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

        <%---- THÔNG BÁO LỖI HOẶC THÀNH CÔNG ----%>
        <c:if test="${param.error eq 'failed' or error eq 'failed'}">
            <div style="background: rgba(224, 49, 49, 0.18); border: 1px solid #ff6b6b; color: #ff8787; border-radius: 8px; padding: 14px 20px; margin-bottom: 24px; font-weight: 600; text-align: center; font-size: 15px;">
                ⚠️ Thanh toán không thành công hoặc đơn giữ vé đã hết hạn (quá 10 phút). Vui lòng thử lại hoặc chọn lại suất chiếu!
            </div>
        </c:if>

        <%---- 3. NỘI DUNG CHÍNH: FORM THÔNG TIN + CỔNG THANH TOÁN ----%>
        <div class="booking-content-grid">

            <%---- CỘT TRÁI: FORM LIÊN HỆ & PHƯƠNG THỨC THANH TOÁN ----%>
            <section class="booking-left-panel">
                
                <form action="${pageContext.request.contextPath}/booking/pay" method="POST" id="checkoutForm">
                    <input type="hidden" name="bookingId" value="${bookingId}">

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
                                       value="${not empty sessionScope.loginUser.fullName ? sessionScope.loginUser.fullName : 'Lê Ngọc Hải'}" 
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
                                       value="${not empty sessionScope.loginUser.email ? sessionScope.loginUser.email : 'haile442006@gmail.com'}" 
                                       placeholder="name@example.com"
                                       required>
                            </div>
                        </div>
                    </div>

                    <%---- 2. ĐIỂM TÍCH LŨY (LOYALTY POINTS) ----%>
                    <div class="checkout-section-block">
                        <h3>2. Sử Dụng Điểm Tích Lũy Thành Viên</h3>
                        <p style="font-size: 13px; color: #8c8275; margin: -10px 0 10px 0;">
                            Quy đổi: 1 điểm = 1.000 VNĐ. Điểm sẽ được khấu trừ trực tiếp vào tổng tiền thanh toán.
                        </p>
                        <div style="display: flex; gap: 12px; align-items: center; max-width: 320px;">
                            <input type="number" 
                                   id="pointsUsed" 
                                   name="pointsUsed" 
                                   class="checkout-input" 
                                   value="${not empty pointsUsed ? pointsUsed : 0}" 
                                   min="0"
                                   placeholder="0 điểm">
                            <span style="font-size: 13.5px; color: #fcca46; white-space: nowrap;">Điểm</span>
                        </div>
                    </div>

                    <%---- 3. PHƯƠNG THỨC THANH TOÁN (VNPAY, MOMO, CASH) ----%>
                    <div class="checkout-section-block">
                        <h3>3. Chọn Phương Thức Thanh Toán</h3>
                        
                        <div class="payment-methods-grid" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 14px;">
                            <%---- VNPAY ----%>
                            <label class="payment-method-card selected" id="card-vnpay" style="background: #25201b; border: 2px solid #fcca46; border-radius: 12px; padding: 14px 16px; display: flex; align-items: center; gap: 12px; cursor: pointer; transition: all 0.2s ease;">
                                <input type="radio" name="paymentType" value="VNPAY" checked style="accent-color: #fcca46; width: 18px; height: 18px; cursor: pointer; flex-shrink: 0;">
                                <div style="width: 44px; height: 44px; background: #ffffff; border-radius: 10px; display: flex; flex-direction: column; align-items: center; justify-content: center; flex-shrink: 0; box-shadow: 0 3px 8px rgba(0,0,0,0.35);">
                                    <span style="font-family: 'Arial Black', sans-serif; font-size: 15px; font-weight: 900; color: #005baa; line-height: 1; letter-spacing: 0.5px;">VN</span>
                                    <span style="font-family: 'Arial Black', sans-serif; font-size: 13px; font-weight: 900; color: #e11b22; line-height: 1; letter-spacing: 0.5px; margin-top: 2px;">PAY</span>
                                </div>
                                <div style="flex: 1; display: flex; flex-direction: column; gap: 2px; min-width: 0;">
                                    <span style="font-size: 14.5px; font-weight: 700; color: #ffffff;">VNPAY QR</span>
                                    <span style="font-size: 12px; color: #a89f91;">Quét mã QR & ATM</span>
                                </div>
                            </label>

                            <%---- MOMO ----%>
                            <label class="payment-method-card" id="card-momo" style="background: #191617; border: 2px solid #3c3733; border-radius: 12px; padding: 14px 16px; display: flex; align-items: center; gap: 12px; cursor: pointer; transition: all 0.2s ease;">
                                <input type="radio" name="paymentType" value="MOMO" style="accent-color: #d82d8b; width: 18px; height: 18px; cursor: pointer; flex-shrink: 0;">
                                <div style="width: 44px; height: 44px; background: linear-gradient(135deg, #d82d8b, #a50064); border-radius: 10px; display: flex; flex-direction: column; align-items: center; justify-content: center; flex-shrink: 0; box-shadow: 0 3px 8px rgba(165,0,100,0.35);">
                                    <span style="font-family: sans-serif; font-size: 15px; font-weight: 900; color: #ffffff;">mo</span>
                                    <span style="font-family: sans-serif; font-size: 15px; font-weight: 900; color: #ffffff;">mo</span>
                                </div>
                                <div style="flex: 1; display: flex; flex-direction: column; gap: 2px; min-width: 0;">
                                    <span style="font-size: 14.5px; font-weight: 700; color: #ffffff;">Ví MoMo</span>
                                    <span style="font-size: 12px; color: #a89f91;">Thanh toán tức thì</span>
                                </div>
                            </label>

                            <%---- CASH ----%>
                            <label class="payment-method-card" id="card-cash" style="background: #191617; border: 2px solid #3c3733; border-radius: 12px; padding: 14px 16px; display: flex; align-items: center; gap: 12px; cursor: pointer; transition: all 0.2s ease;">
                                <input type="radio" name="paymentType" value="CASH" style="accent-color: #69db7c; width: 18px; height: 18px; cursor: pointer; flex-shrink: 0;">
                                <div style="width: 44px; height: 44px; background: linear-gradient(135deg, #2b8a3e, #1b5e20); border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; box-shadow: 0 3px 8px rgba(0,0,0,0.35); font-size: 20px;">
                                    💵
                                </div>
                                <div style="flex: 1; display: flex; flex-direction: column; gap: 2px; min-width: 0;">
                                    <span style="font-size: 14.5px; font-weight: 700; color: #ffffff;">Tiền mặt</span>
                                    <span style="font-size: 12px; color: #a89f91;">Thanh toán tại quầy</span>
                                </div>
                            </label>
                        </div>
                    </div>

                    <%---- 4. ĐIỀU KHOẢN ----%>
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
                        <a href="${pageContext.request.contextPath}/booking/combos?bookingId=${bookingId}" class="btn-back-step">
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
                        ${not empty cinema.name ? cinema.name : 'Cinetik Theater'}
                    </div>
                </div>
                <hr class="ticket-perforation">

                <div class="ticket-info-group">
                    <span class="ticket-field-label">MÃ ĐƠN HÀNG</span>
                    <div style="font-family: monospace; font-size: 15px; font-weight: 700; color: #fcca46;">
                        ${bookingId}
                    </div>
                </div>

                <div class="ticket-info-group">
                    <span class="ticket-field-label">TRẠNG THÁI HIỆN TẠI</span>
                    <div style="display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: 700; background: rgba(252, 202, 70, 0.15); color: #fcca46; border: 1px solid rgba(252, 202, 70, 0.4);">
                        🟡 ĐANG GIỮ CHỖ (PENDING)
                    </div>
                </div>

                <%---- Khung tổng cộng tiền thanh toán sang trọng ----%>
                <div class="ticket-total-box" style="margin-top: 20px;">
                    <span class="ticket-total-label">TỔNG TIỀN THANH TOÁN</span>
                    <span class="ticket-total-amount" id="checkoutFinalPrice">
                        <fmt:formatNumber value="${totalAmount}" type="number" groupingUsed="true"/>đ
                    </span>
                </div>

                <div style="margin-top: 14px;">
                    <button type="submit" form="checkoutForm" class="btn-ticket-submit">
                        🔒 XÁC NHẬN & THANH TOÁN
                    </button>
                    <a href="${pageContext.request.contextPath}/booking/combos?bookingId=${bookingId}" class="btn-ticket-back">
                        ← Quay lại chọn bắp nước
                    </a>
                </div>

                <%---- Mã vạch Barcode & Mã số vé ----%>
                <div class="ticket-barcode-wrap">
                    <div class="ticket-barcode-stripes"></div>
                    <div class="ticket-barcode-serial">№ CNK-${bookingId}</div>
                </div>
            </aside>

        </div>

    </div>
</main>

<script>
    const cardVnpay = document.getElementById('card-vnpay');
    const cardMomo = document.getElementById('card-momo');
    const cardCash = document.getElementById('card-cash');

    function selectPaymentMethod(method) {
        [cardVnpay, cardMomo, cardCash].forEach(c => {
            if (c) {
                c.classList.remove('selected');
                c.style.borderColor = '#3c3733';
                c.style.background = '#191617';
                const r = c.querySelector('input[type="radio"]');
                if (r) r.checked = false;
            }
        });

        if (method === 'VNPAY' && cardVnpay) {
            cardVnpay.classList.add('selected');
            cardVnpay.style.borderColor = '#fcca46';
            cardVnpay.style.background = '#25201b';
            cardVnpay.querySelector('input[type="radio"]').checked = true;
        } else if (method === 'MOMO' && cardMomo) {
            cardMomo.classList.add('selected');
            cardMomo.style.borderColor = '#d82d8b';
            cardMomo.style.background = '#261620';
            cardMomo.querySelector('input[type="radio"]').checked = true;
        } else if (method === 'CASH' && cardCash) {
            cardCash.classList.add('selected');
            cardCash.style.borderColor = '#69db7c';
            cardCash.style.background = '#162319';
            cardCash.querySelector('input[type="radio"]').checked = true;
        }
    }

    if (cardVnpay) cardVnpay.addEventListener('click', () => selectPaymentMethod('VNPAY'));
    if (cardMomo) cardMomo.addEventListener('click', () => selectPaymentMethod('MOMO'));
    if (cardCash) cardCash.addEventListener('click', () => selectPaymentMethod('CASH'));
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
