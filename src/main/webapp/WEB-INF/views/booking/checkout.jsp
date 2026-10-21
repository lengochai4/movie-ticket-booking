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

        <%---- THÔNG BÁO LỖI HOẶC TRẠNG THÁI GIAO DỊCH ----%>
        <c:choose>
            <c:when test="${param.error eq 'failed' or error eq 'failed'}">
                <div style="background: rgba(224, 49, 49, 0.18); border: 1px solid #ff6b6b; color: #ff8787; border-radius: 8px; padding: 14px 20px; margin-bottom: 24px; font-weight: 600; text-align: center; font-size: 15px;">
                    ⚠️ Thanh toán không thành công hoặc đơn giữ vé đã hết hạn (quá 10 phút). Vui lòng thử lại!
                </div>
            </c:when>
            <c:when test="${param.error eq 'vnpay_min_amount' or error eq 'vnpay_min_amount'}">
                <div style="background: rgba(224, 49, 49, 0.18); border: 1px solid #ff6b6b; color: #ff8787; border-radius: 8px; padding: 14px 20px; margin-bottom: 24px; font-weight: 600; text-align: center; font-size: 15px;">
                    ⚠️ Cổng thanh toán VNPay yêu cầu số tiền tối thiểu là 5.000 VNĐ. Nếu bạn dùng điểm thưởng, vui lòng dùng đủ điểm để về 0 VNĐ (nhận vé miễn phí) hoặc điều chỉnh điểm để số tiền còn lại từ 5.000 VNĐ trở lên!
                </div>
            </c:when>
            <c:when test="${param.error eq 'vnpay_checksum_failed' or error eq 'vnpay_checksum_failed'}">
                <div style="background: rgba(224, 49, 49, 0.18); border: 1px solid #ff6b6b; color: #ff8787; border-radius: 8px; padding: 14px 20px; margin-bottom: 24px; font-weight: 600; text-align: center; font-size: 15px;">
                    🔒 Chữ ký bảo mật VNPay không hợp lệ (HMAC Checksum Mismatch). Giao dịch bị từ chối để bảo vệ an toàn!
                </div>
            </c:when>
            <c:when test="${param.error eq 'amount_mismatch' or error eq 'amount_mismatch'}">
                <div style="background: rgba(224, 49, 49, 0.18); border: 1px solid #ff6b6b; color: #ff8787; border-radius: 8px; padding: 14px 20px; margin-bottom: 24px; font-weight: 600; text-align: center; font-size: 15px;">
                    ⚠️ Số tiền thanh toán thực tế không khớp với hóa đơn trong hệ thống. Vui lòng kiểm tra lại!
                </div>
            </c:when>
            <c:when test="${param.error eq 'vnpay_cancelled' or error eq 'vnpay_cancelled'}">
                <div style="background: rgba(252, 202, 70, 0.18); border: 1px solid #fcca46; color: #fcca46; border-radius: 8px; padding: 14px 20px; margin-bottom: 24px; font-weight: 600; text-align: center; font-size: 15px;">
                    ℹ️ Giao dịch đã được hủy trên cổng VNPay. Bạn có thể chọn lại phương thức thanh toán khác dưới đây.
                </div>
            </c:when>
            <c:when test="${param.error eq 'booking_expired' or error eq 'booking_expired'}">
                <div style="background: rgba(224, 49, 49, 0.18); border: 1px solid #ff6b6b; color: #ff8787; border-radius: 8px; padding: 14px 20px; margin-bottom: 24px; font-weight: 600; text-align: center; font-size: 15px;">
                    ⏰ Đơn đặt vé đã quá thời hạn giữ chỗ 10 phút. Vui lòng chọn lại ghế để tiếp tục!
                </div>
            </c:when>
        </c:choose>

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
                                       value="${not empty bookingDetails.customerName ? bookingDetails.customerName : (not empty sessionScope.loginUser.fullName ? sessionScope.loginUser.fullName : 'Lê Ngọc Hải')}" 
                                       required>
                            </div>

                            <div class="checkout-input-group">
                                <label for="contactPhone">Số điện thoại *</label>
                                <input type="tel" 
                                       id="contactPhone" 
                                       name="contactPhone" 
                                       class="checkout-input" 
                                       value="${not empty bookingDetails.phone ? bookingDetails.phone : (not empty sessionScope.loginUser.phone ? sessionScope.loginUser.phone : '0988123456')}" 
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
                                       value="${not empty bookingDetails.email ? bookingDetails.email : (not empty sessionScope.loginUser.email ? sessionScope.loginUser.email : 'haile442006@gmail.com')}" 
                                       placeholder="name@example.com"
                                       required>
                            </div>
                        </div>
                    </div>

                    <%---- 2. ĐIỂM TÍCH LŨY (LOYALTY POINTS) ----%>
                    <div class="checkout-section-block">
                        <h3>2. Sử Dụng Điểm Tích Lũy Thành Viên</h3>
                        <p style="font-size: 13px; color: #8c8275; margin: -10px 0 10px 0;">
                            Quy đổi: <strong>1 điểm = 10.000 VNĐ</strong>. Điểm tích lũy được giảm tối đa <strong>90%</strong> tổng giá trị đơn hàng.
                            <c:choose>
                                <c:when test="${not empty availablePoints and availablePoints > 0}">
                                    <br><span style="color: #fcca46; font-weight: 600;">
                                        ★ Bạn hiện có: <strong>${availablePoints}</strong> điểm. Tối đa áp dụng cho đơn này: <strong>${maxUsablePoints}</strong> điểm (Giảm tối đa <fmt:formatNumber value="${maxUsablePoints * 10000}" type="number"/>đ)
                                    </span>
                                </c:when>
                                <c:otherwise>
                                    <br><span style="color: #6c6356;">(Tài khoản hiện có 0 điểm tích lũy)</span>
                                </c:otherwise>
                            </c:choose>
                        </p>
                        <div style="display: flex; gap: 12px; align-items: center; max-width: 460px; flex-wrap: wrap;">
                            <div style="display: flex; gap: 8px; align-items: center; flex: 1; min-width: 180px;">
                                <input type="number" 
                                       id="pointsUsed" 
                                       name="pointsUsed" 
                                       class="checkout-input" 
                                       value="${not empty pointsUsed ? pointsUsed : 0}" 
                                       min="0"
                                       max="${not empty maxUsablePoints ? maxUsablePoints : 0}"
                                       placeholder="0 điểm">
                                <span style="font-size: 13.5px; color: #fcca46; font-weight: 700; white-space: nowrap;">Điểm</span>
                            </div>
                            <c:if test="${not empty maxUsablePoints and maxUsablePoints > 0}">
                                <button type="button" id="btnApplyMaxPoints" style="background: rgba(252, 202, 70, 0.15); border: 1px solid #fcca46; color: #fcca46; border-radius: 8px; padding: 10px 14px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s ease;">
                                    ⚡ Dùng tối đa (${maxUsablePoints} điểm ~ 90%)
                                </button>
                            </c:if>
                        </div>
                        <div id="vnpayMinWarning" style="display: none; margin-top: 10px; font-size: 12.5px; color: #ff922b; background: rgba(255, 146, 43, 0.12); padding: 8px 12px; border-radius: 6px; border: 1px solid rgba(255, 146, 43, 0.3);">
                            ⚠️ Điểm thưởng được áp dụng tối đa 90% giá trị đơn hàng (tối đa <strong>${maxUsablePoints} điểm</strong>) để đảm bảo số tiền thanh toán từ 5.000đ qua VNPay.
                        </div>
                    </div>

                    <%---- 3. PHƯƠNG THỨC THANH TOÁN ----%>
                    <div class="checkout-section-block">
                        <h3>3. Phương Thức Thanh Toán</h3>
                        
                        <div class="payment-methods-grid" style="display: grid; grid-template-columns: 1fr; gap: 14px;">
                            <%---- THẺ 100% ĐIỂM THƯỞNG (HIỆN KHI 0 ĐỒNG) ----%>
                            <div id="card-points-free" style="display: none; background: rgba(105, 219, 124, 0.12); border: 2px solid #69db7c; border-radius: 12px; padding: 16px 20px; align-items: center; gap: 16px;">
                                <div style="width: 48px; height: 48px; background: linear-gradient(135deg, #69db7c, #2f9e44); border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 24px; flex-shrink: 0; box-shadow: 0 3px 10px rgba(47, 158, 68, 0.35);">
                                    🎁
                                </div>
                                <div style="flex: 1; display: flex; flex-direction: column; gap: 4px; min-width: 0;">
                                    <div style="display: flex; align-items: center; gap: 8px;">
                                        <span style="font-size: 15px; font-weight: 700; color: #ffffff;">Thanh toán 100% bằng Điểm thưởng</span>
                                        <span style="background: rgba(105, 219, 124, 0.2); color: #69db7c; font-size: 11px; font-weight: 700; padding: 2px 8px; border-radius: 6px; border: 1px solid rgba(105, 219, 124, 0.4);">Miễn phí 0 VNĐ</span>
                                    </div>
                                    <span style="font-size: 12.5px; color: #d0ebff;">
                                        Hóa đơn được thanh toán trọn vẹn bằng điểm tích lũy. Hệ thống sẽ cấp vé điện tử ngay mà không cần chuyển tới VNPay.
                                    </span>
                                </div>
                            </div>

                            <%---- VNPAY (HIỆN KHI TIỀN > 0) ----%>
                            <label class="payment-method-card selected" id="card-vnpay" style="background: #25201b; border: 2px solid #fcca46; border-radius: 12px; padding: 16px 20px; display: flex; align-items: center; gap: 16px; cursor: pointer; transition: all 0.2s ease;">
                                <input type="radio" name="paymentType" value="VNPAY" checked style="accent-color: #fcca46; width: 20px; height: 20px; cursor: pointer; flex-shrink: 0;">
                                <div style="width: 48px; height: 48px; background: #ffffff; border-radius: 10px; display: flex; flex-direction: column; align-items: center; justify-content: center; flex-shrink: 0; box-shadow: 0 3px 8px rgba(0,0,0,0.35);">
                                    <span style="font-family: 'Arial Black', sans-serif; font-size: 16px; font-weight: 900; color: #005baa; line-height: 1; letter-spacing: 0.5px;">VN</span>
                                    <span style="font-family: 'Arial Black', sans-serif; font-size: 14px; font-weight: 900; color: #e11b22; line-height: 1; letter-spacing: 0.5px; margin-top: 2px;">PAY</span>
                                </div>
                                <div style="flex: 1; display: flex; flex-direction: column; gap: 4px; min-width: 0;">
                                    <div style="display: flex; align-items: center; gap: 8px;">
                                        <span style="font-size: 15px; font-weight: 700; color: #ffffff;">Cổng thanh toán điện tử VNPAY</span>
                                        <span style="background: rgba(252, 202, 70, 0.15); color: #fcca46; font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 6px; border: 1px solid rgba(252, 202, 70, 0.3);">Bảo mật & Tức thì</span>
                                    </div>
                                    <span style="font-size: 12.5px; color: #a89f91;">Hỗ trợ ứng dụng ngân hàng quét VNPAY-QR, Thẻ ATM nội địa / Internet Banking và Thẻ quốc tế Visa/Mastercard</span>
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
                        <button type="submit" id="btnSubmitLeft" class="btn-proceed" style="font-size: 15px; padding: 14px 16px;">
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
                    <span class="ticket-badge-pill">★ Cinetik Cinema ★</span>
                    <h2 class="ticket-title-admit">ADMIT ONE</h2>
                    <div class="ticket-subtitle-theater" style="font-weight: 700; color: #fcca46; font-size: 15px;">
                        ${not empty bookingDetails.movieTitle ? bookingDetails.movieTitle : 'Phim Chiếu Rạp'}
                    </div>
                    <div style="font-size: 12px; color: #a89f91; margin-top: 4px;">
                        ${not empty bookingDetails.cinemaName ? bookingDetails.cinemaName : 'Cinetik Theater'} - ${not empty bookingDetails.roomName ? bookingDetails.roomName : 'Phòng 1'}
                    </div>
                </div>
                <hr class="ticket-perforation">

                <div class="ticket-info-group">
                    <span class="ticket-field-label">MÃ ĐƠN HÀNG</span>
                    <div style="font-family: monospace; font-size: 15px; font-weight: 700; color: #fcca46;">
                        ${not empty bookingDetails.orderCode ? bookingDetails.orderCode : bookingId}
                    </div>
                </div>

                <div class="ticket-info-group">
                    <span class="ticket-field-label">GHẾ ĐÃ CHỌN</span>
                    <div style="font-weight: 700; color: #ffffff; font-size: 14px;">
                        ${not empty bookingDetails.seats ? bookingDetails.seats : 'Chưa có ghế'}
                    </div>
                </div>

                <c:if test="${not empty bookingDetails.combos}">
                    <div class="ticket-info-group">
                        <span class="ticket-field-label">BẮP NƯỚC ĐÃ CHỌN</span>
                        <div style="font-size: 13px; color: #fcca46;">
                            ${bookingDetails.combos}
                        </div>
                    </div>
                </c:if>

                <div class="ticket-info-group">
                    <span class="ticket-field-label">CHI TIẾT CHI PHÍ</span>
                    <div style="display: flex; justify-content: space-between; font-size: 13px; color: #d0c7b7; margin-top: 4px;">
                        <span>Tiền vé:</span>
                        <span><fmt:formatNumber value="${bookingDetails.ticketPrice}" type="number"/>đ</span>
                    </div>
                    <c:if test="${bookingDetails.comboPrice > 0}">
                        <div style="display: flex; justify-content: space-between; font-size: 13px; color: #d0c7b7; margin-top: 2px;">
                            <span>Bắp nước:</span>
                            <span><fmt:formatNumber value="${bookingDetails.comboPrice}" type="number"/>đ</span>
                        </div>
                    </c:if>
                    <div style="display: flex; justify-content: space-between; font-size: 13px; color: #69db7c; margin-top: 2px;">
                        <span>Giảm trừ điểm (tối đa 90%):</span>
                        <span id="discountDisplay">-<fmt:formatNumber value="${pointsUsed * 10000}" type="number"/>đ</span>
                    </div>
                </div>

                <div class="ticket-info-group">
                    <span class="ticket-field-label">TRẠNG THÁI HIỆN TẠI</span>
                    <div style="display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: 700; background: rgba(252, 202, 70, 0.15); color: #fcca46; border: 1px solid rgba(252, 202, 70, 0.4);">
                        🟡 ĐANG GIỮ CHỖ (PENDING)
                    </div>
                </div>

                <%---- Khung tổng cộng tiền thanh toán sang trọng ----%>
                <div class="ticket-total-box" style="margin-top: 16px;">
                    <span class="ticket-total-label">TỔNG TIỀN THANH TOÁN</span>
                    <span class="ticket-total-amount" id="checkoutFinalPrice">
                        <fmt:formatNumber value="${totalAmount}" type="number" groupingUsed="true"/>đ
                    </span>
                </div>

                <div style="margin-top: 14px;">
                    <button type="submit" form="checkoutForm" id="btnSubmitRight" class="btn-ticket-submit">
                        🔒 XÁC NHẬN & THANH TOÁN
                    </button>
                    <a href="${pageContext.request.contextPath}/booking/combos?bookingId=${bookingId}" class="btn-ticket-back">
                        ← Quay lại chọn bắp nước
                    </a>
                </div>

                <%---- Mã vạch Barcode & Mã số vé ----%>
                <div class="ticket-barcode-wrap">
                    <div class="ticket-barcode-stripes"></div>
                    <div class="ticket-barcode-serial">№ ${not empty bookingDetails.orderCode ? bookingDetails.orderCode : bookingId}</div>
                </div>
            </aside>

        </div>

    </div>
</main>

<%---- MODAL XÁC NHẬN TRỪ ĐIỂM KHI ĐƠN HÀNG 0 ĐỒNG ----%>
<div id="confirmPointsModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.78); backdrop-filter: blur(5px); z-index: 9999; align-items: center; justify-content: center; padding: 20px;">
    <div style="background: #1e1915; border: 2px solid #fcca46; border-radius: 16px; max-width: 480px; width: 100%; padding: 28px; box-shadow: 0 12px 36px rgba(0,0,0,0.6); position: relative;">
        <div style="text-align: center; margin-bottom: 20px;">
            <div style="width: 64px; height: 64px; background: linear-gradient(135deg, #fcca46, #d48806); border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; font-size: 32px; box-shadow: 0 6px 16px rgba(252, 202, 70, 0.4); margin-bottom: 12px;">
                🎁
            </div>
            <h3 style="color: #ffffff; font-size: 20px; font-weight: 800; margin: 0 0 6px 0;">Xác Nhận Đổi Điểm Thưởng</h3>
            <p style="color: #a89f91; font-size: 13.5px; margin: 0; line-height: 1.5;">
                Hóa đơn của bạn được thanh toán 100% bằng điểm thưởng (<strong>0 VNĐ</strong>). Bạn không cần thực hiện thanh toán qua VNPay!
            </p>
        </div>

        <div style="background: #14110e; border: 1px solid #3d352e; border-radius: 12px; padding: 16px 20px; margin-bottom: 20px; display: flex; flex-direction: column; gap: 10px;">
            <div style="display: flex; justify-content: space-between; font-size: 13.5px; color: #b5a995;">
                <span>Tổng giá trị đơn hàng:</span>
                <span style="font-weight: 700; color: #ffffff;" id="modalRawTotal">0đ</span>
            </div>
            <div style="display: flex; justify-content: space-between; font-size: 13.5px; color: #b5a995;">
                <span>Số điểm sử dụng:</span>
                <span style="font-weight: 700; color: #fcca46;" id="modalPointsUsed">0 điểm</span>
            </div>
            <div style="display: flex; justify-content: space-between; font-size: 13.5px; color: #b5a995;">
                <span>Số tiền giảm trừ:</span>
                <span style="font-weight: 700; color: #69db7c;" id="modalDiscount">0đ</span>
            </div>
            <hr style="border: 0; border-top: 1px dashed #3d352e; margin: 4px 0;">
            <div style="display: flex; justify-content: space-between; font-size: 14.5px; align-items: center;">
                <span style="font-weight: 700; color: #ffffff;">Số tiền cần thanh toán:</span>
                <span style="font-weight: 900; color: #69db7c; font-size: 18px; background: rgba(105, 219, 124, 0.15); padding: 2px 10px; border-radius: 6px;">0 VNĐ (Miễn phí)</span>
            </div>
            <div style="display: flex; justify-content: space-between; font-size: 12.5px; color: #8c8275;">
                <span>Điểm tích lũy còn lại sau trừ:</span>
                <span style="font-weight: 600; color: #fcca46;" id="modalRemainingPoints">0 điểm</span>
            </div>
        </div>

        <div style="background: rgba(252, 202, 70, 0.08); border-left: 3px solid #fcca46; padding: 10px 14px; border-radius: 4px; font-size: 12px; color: #d0c7b7; margin-bottom: 22px; line-height: 1.5;">
            ★ Khi xác nhận, hệ thống sẽ tự động trừ số điểm trên tài khoản của bạn và lập tức chuyển đến trang vé điện tử thành công!
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1.3fr; gap: 12px;">
            <button type="button" id="btnCancelModal" style="background: #2b2520; color: #d0c7b7; border: 1px solid #4a4037; border-radius: 8px; padding: 12px; font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.2s ease;">
                Quay lại
            </button>
            <button type="button" id="btnConfirmDeduct" style="background: linear-gradient(135deg, #2b8a3e, #237032); color: #ffffff; border: 0; border-radius: 8px; padding: 12px; font-size: 14px; font-weight: 700; cursor: pointer; box-shadow: 0 4px 12px rgba(43,138,62,0.4); transition: all 0.2s ease;">
                ✓ Xác Nhận Trừ Điểm
            </button>
        </div>
    </div>
</div>

<script>
    const checkoutForm = document.getElementById('checkoutForm');
    const cardVnpay = document.getElementById('card-vnpay');
    const cardPointsFree = document.getElementById('card-points-free');
    const pointsInput = document.getElementById('pointsUsed');
    const btnApplyMaxPoints = document.getElementById('btnApplyMaxPoints');
    const btnSubmitLeft = document.getElementById('btnSubmitLeft');
    const btnSubmitRight = document.getElementById('btnSubmitRight');
    const finalPriceEl = document.getElementById('checkoutFinalPrice');
    const discountEl = document.getElementById('discountDisplay');
    const vnpayMinWarning = document.getElementById('vnpayMinWarning');
    const vnpayMinAmountText = document.getElementById('vnpayMinAmountText');

    // Modal elements
    const modal = document.getElementById('confirmPointsModal');
    const modalRawTotal = document.getElementById('modalRawTotal');
    const modalPointsUsed = document.getElementById('modalPointsUsed');
    const modalDiscount = document.getElementById('modalDiscount');
    const modalRemainingPoints = document.getElementById('modalRemainingPoints');
    const btnCancelModal = document.getElementById('btnCancelModal');
    const btnConfirmDeduct = document.getElementById('btnConfirmDeduct');

    const rawTotal = ${not empty rawTotal ? rawTotal : 0};
    const availablePoints = ${not empty availablePoints ? availablePoints : 0};
    const maxUsablePoints = ${not empty maxUsablePoints ? maxUsablePoints : (not empty availablePoints ? availablePoints : 0)};

    let isZeroAmountConfirmed = false;

    function selectPaymentMethod(method) {
        if (cardVnpay) {
            cardVnpay.classList.add('selected');
            cardVnpay.style.borderColor = '#fcca46';
            cardVnpay.style.background = '#25201b';
            const r = cardVnpay.querySelector('input[type="radio"]');
            if (r) r.checked = true;
        }
    }

    if (cardVnpay) cardVnpay.addEventListener('click', () => selectPaymentMethod('VNPAY'));

    function updatePricing() {
        let pts = parseInt(pointsInput ? pointsInput.value : 0, 10);
        if (isNaN(pts) || pts < 0) pts = 0;
        if (pts > maxUsablePoints) {
            pts = maxUsablePoints;
            if (pointsInput) pointsInput.value = pts;
            if (vnpayMinWarning) vnpayMinWarning.style.display = 'block';
        } else {
            if (vnpayMinWarning) vnpayMinWarning.style.display = 'none';
        }

        const discount = pts * 10000;
        let finalPrice = rawTotal - discount;
        if (finalPrice < 0) finalPrice = 0;

        if (discountEl) {
            discountEl.textContent = '-' + discount.toLocaleString('vi-VN') + 'đ';
        }
        if (finalPriceEl) {
            finalPriceEl.textContent = finalPrice.toLocaleString('vi-VN') + 'đ';
        }

        // Toggle giao diện khi finalPrice == 0
        if (finalPrice === 0 && rawTotal > 0 && pts > 0) {
            if (cardPointsFree) cardPointsFree.style.display = 'flex';
            if (cardVnpay) cardVnpay.style.display = 'none';

            if (btnSubmitLeft) {
                btnSubmitLeft.innerHTML = '✨ Xác Nhận Đổi Điểm & Nhận Vé (0đ)';
                btnSubmitLeft.style.background = 'linear-gradient(135deg, #2b8a3e, #237032)';
                btnSubmitLeft.style.boxShadow = '0 4px 14px rgba(43, 138, 62, 0.45)';
            }
            if (btnSubmitRight) {
                btnSubmitRight.innerHTML = '✨ ĐỔI ĐIỂM & NHẬN VÉ (0đ)';
                btnSubmitRight.style.background = 'linear-gradient(135deg, #2b8a3e, #237032)';
                btnSubmitRight.style.boxShadow = '0 4px 14px rgba(43, 138, 62, 0.45)';
            }
            if (vnpayMinWarning) vnpayMinWarning.style.display = 'none';
        } else {
            if (cardPointsFree) cardPointsFree.style.display = 'none';
            if (cardVnpay) cardVnpay.style.display = 'flex';

            if (btnSubmitLeft) {
                btnSubmitLeft.innerHTML = '🔒 Xác Nhận & Thanh Toán Ngay';
                btnSubmitLeft.style.background = '';
                btnSubmitLeft.style.boxShadow = '';
            }
            if (btnSubmitRight) {
                btnSubmitRight.innerHTML = '🔒 XÁC NHẬN & THANH TOÁN';
                btnSubmitRight.style.background = '';
                btnSubmitRight.style.boxShadow = '';
            }

            // Kiểm tra ràng buộc cận biên VNPay: 0 < finalPrice < 5000
            if (finalPrice > 0 && finalPrice < 5000) {
                if (vnpayMinWarning) {
                    vnpayMinWarning.style.display = 'block';
                    if (vnpayMinAmountText) vnpayMinAmountText.textContent = finalPrice.toLocaleString('vi-VN') + 'đ';
                }
            } else {
                if (vnpayMinWarning) vnpayMinWarning.style.display = 'none';
            }
        }
    }

    if (pointsInput) {
        pointsInput.addEventListener('input', updatePricing);
    }

    if (btnApplyMaxPoints) {
        btnApplyMaxPoints.addEventListener('click', function() {
            if (pointsInput) {
                pointsInput.value = maxUsablePoints;
                updatePricing();
            }
        });
    }

    // Modal control
    function openModal() {
        let pts = parseInt(pointsInput ? pointsInput.value : 0, 10);
        if (isNaN(pts)) pts = 0;
        const discount = pts * 10000;
        const remaining = Math.max(0, availablePoints - pts);

        if (modalRawTotal) modalRawTotal.textContent = rawTotal.toLocaleString('vi-VN') + 'đ';
        if (modalPointsUsed) modalPointsUsed.textContent = pts + ' điểm';
        if (modalDiscount) modalDiscount.textContent = '-' + discount.toLocaleString('vi-VN') + 'đ';
        if (modalRemainingPoints) modalRemainingPoints.textContent = remaining + ' điểm';

        if (modal) modal.style.display = 'flex';
    }

    function closeModal() {
        if (modal) modal.style.display = 'none';
    }

    if (btnCancelModal) {
        btnCancelModal.addEventListener('click', closeModal);
    }

    if (btnConfirmDeduct) {
        btnConfirmDeduct.addEventListener('click', function() {
            isZeroAmountConfirmed = true;
            closeModal();
            if (checkoutForm) {
                checkoutForm.submit();
            }
        });
    }

    // Intercept form submit
    if (checkoutForm) {
        checkoutForm.addEventListener('submit', function(e) {
            let pts = parseInt(pointsInput ? pointsInput.value : 0, 10);
            if (isNaN(pts)) pts = 0;
            const discount = pts * 10000;
            let finalPrice = rawTotal - discount;
            if (finalPrice < 0) finalPrice = 0;

            if (finalPrice === 0 && rawTotal > 0 && pts > 0) {
                if (!isZeroAmountConfirmed) {
                    e.preventDefault();
                    openModal();
                    return;
                }
            }

            if (finalPrice > 0 && finalPrice < 5000) {
                e.preventDefault();
                alert('Cổng thanh toán VNPay yêu cầu số tiền tối thiểu là 5.000 VNĐ. Hiện tại hóa đơn còn ' + finalPrice.toLocaleString('vi-VN') + 'đ. Vui lòng giảm bớt điểm dùng để thanh toán từ 5.000đ hoặc dùng đủ ' + maxUsablePoints + ' điểm để được miễn phí 0đ!');
                return;
            }
        });
    }

    // Khởi chạy ngay khi tải trang
    updatePricing();
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
