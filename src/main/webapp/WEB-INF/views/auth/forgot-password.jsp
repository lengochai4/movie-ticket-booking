<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Quên Mật Khẩu - Cinetik" scope="request"/>
<c:set var="extraCss" value="auth.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="auth-container">
    <div class="auth-card">
        <h2 class="auth-title">Khôi Phục Mật Khẩu</h2>
        
        <div class="step-indicator">
            <span class="step-badge ${step ne 'verify' ? 'active' : ''}">1. Nhập thông tin</span>
            <span style="color: #6e6459; font-size: 12px;">&rarr;</span>
            <span class="step-badge ${step eq 'verify' ? 'active' : ''}">2. Xác thực OTP & Mật khẩu</span>
        </div>

        <%---- Thông báo thành công ----%>
        <c:if test="${not empty successMessage}">
            <div class="auth-alert success">
                <span>${successMessage}</span>
            </div>
        </c:if>

        <%---- Thông báo lỗi ----%>
        <c:if test="${not empty errorMessage}">
            <div class="auth-alert error">
                <span>${errorMessage}</span>
            </div>
        </c:if>

        <c:choose>
            <%-- ==================== BƯỚC 1: NHẬP EMAIL HOẶC SỐ ĐIỆN THOẠI ==================== --%>
            <c:when test="${step ne 'verify'}">
                <p class="auth-subtitle">Vui lòng cung cấp email hoặc số điện thoại đã đăng ký để nhận mã xác thực OTP.</p>

                <form action="${pageContext.request.contextPath}/auth/forgot-password" method="POST" class="auth-form">
                    <input type="hidden" name="action" value="send-otp">

                    <div class="form-group">
                        <label for="identifier">Email hoặc Số điện thoại</label>
                        <input type="text"
                               id="identifier"
                               name="identifier"
                               class="form-input"
                               placeholder="Ví dụ: example@cinetik.com hoặc 0901234567"
                               value="${not empty param.identifier ? param.identifier : identifier}"
                               required
                               autofocus>
                        <small class="form-hint">Mã OTP sẽ được gửi về hộp thư điện tử đã liên kết với tài khoản.</small>
                    </div>

                    <button type="submit" class="btn-auth-submit">Gửi Mã Xác Thực (OTP)</button>
                </form>
            </c:when>

            <%-- ==================== BƯỚC 2: NHẬP MÃ OTP VÀ MẬT KHẨU MỚI ==================== --%>
            <c:otherwise>
                <p class="auth-subtitle">
                    Mã xác thực đã được gửi tới <strong style="color: #fcca46;">${maskedEmail}</strong>. Mã có hiệu lực trong 10 phút.
                </p>

                <form action="${pageContext.request.contextPath}/auth/forgot-password" method="POST" class="auth-form">
                    <input type="hidden" name="action" value="reset-password">

                    <div class="form-group">
                        <label for="otp">Mã xác thực OTP (6 chữ số)</label>
                        <input type="text"
                               id="otp"
                               name="otp"
                               class="form-input otp-input"
                               placeholder="------"
                               maxlength="6"
                               pattern="[0-9]{6}"
                               inputmode="numeric"
                               autocomplete="one-time-code"
                               required
                               autofocus>
                    </div>

                    <div class="form-group">
                        <label for="newPassword">Mật khẩu mới</label>
                        <input type="password"
                               id="newPassword"
                               name="newPassword"
                               class="form-input"
                               placeholder="Nhập mật khẩu mới"
                               required>
                        <small class="form-hint">Tối thiểu 8 ký tự, gồm cả chữ hoa, chữ thường, chữ số và ký tự đặc biệt.</small>
                    </div>

                    <div class="form-group">
                        <label for="confirmPassword">Xác nhận mật khẩu mới</label>
                        <input type="password"
                               id="confirmPassword"
                               name="confirmPassword"
                               class="form-input"
                               placeholder="Nhập lại mật khẩu mới"
                               required>
                    </div>

                    <div class="auth-subactions">
                        <a href="${pageContext.request.contextPath}/auth/forgot-password?restart=true" class="auth-link">
                            &larr; Đổi email khác
                        </a>
                        <button type="submit"
                                form="resendForm"
                                class="btn-link-action">
                            Gửi lại mã OTP
                        </button>
                    </div>

                    <button type="submit" class="btn-auth-submit">Xác Nhận & Đổi Mật Khẩu</button>
                </form>

                <%-- Form phụ để xử lý nút Gửi lại mã OTP mà không xung đột input --%>
                <form id="resendForm" action="${pageContext.request.contextPath}/auth/forgot-password" method="POST" style="display: none;">
                    <input type="hidden" name="action" value="resend-otp">
                </form>
            </c:otherwise>
        </c:choose>

        <%-- Chân Card: Quay về trang Đăng nhập --%>
        <div class="auth-footer">
            <span>Đã nhớ lại mật khẩu? </span>
            <a href="${pageContext.request.contextPath}/auth/login" class="auth-link-highlight">Đăng nhập ngay</a>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
