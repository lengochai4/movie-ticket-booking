<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Xác Nhận Email - Cinetik" scope="request"/>
<c:set var="extraCss" value="auth.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="auth-container">
    <div class="auth-card" style="max-width: 520px; text-align: center;">
        <div style="font-size: 54px; margin-bottom: 12px;">📧</div>
        <h2 class="auth-title" style="margin-bottom: 8px;">Kiểm Tra Hộp Thư Của Bạn</h2>
        
        <p class="auth-subtitle" style="margin-bottom: 20px;">
            Hệ thống đã gửi liên kết kích hoạt tài khoản đến địa chỉ:
        </p>

        <div style="background: rgba(225, 29, 72, 0.1); border: 1px solid rgba(225, 29, 72, 0.3); border-radius: 8px; padding: 12px 16px; margin-bottom: 24px; font-weight: 600; color: #f43f5e; font-size: 16px; word-break: break-all;">
            <c:out value="${email != null ? email : param.email}" />
        </div>

        <c:if test="${resendSuccess eq true}">
            <div class="auth-alert success" style="margin-bottom: 20px; text-align: left;">
                <span> Đã gửi lại email kích hoạt thành công! Vui lòng kiểm tra hộp thư của bạn.</span>
            </div>
        </c:if>

        <c:if test="${not empty emailError}">
            <div class="auth-alert error" style="margin-bottom: 20px; text-align: left;">
                <span>${emailError}</span>
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="auth-alert error" style="margin-bottom: 20px; text-align: left;">
                <span>${errorMessage}</span>
            </div>
        </c:if>

        <p style="color: #a1a1aa; font-size: 14px; line-height: 1.6; margin-bottom: 28px; text-align: left;">
            • Vui lòng mở email từ <strong>Cinetik Cinema</strong> và nhấp vào nút <strong>Xác nhận tài khoản</strong>.<br>
            • Liên kết có hiệu lực trong vòng <strong>24 giờ</strong>.<br>
            • <em>Lưu ý: Nếu không tìm thấy thư trong Hộp thư đến, vui lòng kiểm tra thêm mục <strong>Spam (Thư rác)</strong> hoặc <strong>Quảng cáo</strong>.</em>
        </p>

        <div style="display: flex; flex-direction: column; gap: 12px; margin-bottom: 24px;">
            <a href="https://mail.google.com" target="_blank" rel="noopener noreferrer" class="btn-auth-submit" style="display: block; text-decoration: none; text-align: center;">
                 Mở Gmail Ngay
            </a>

            <form action="${pageContext.request.contextPath}/auth/resend-verification" method="POST" style="margin: 0;">
                <input type="hidden" name="email" value="<c:out value='${email != null ? email : param.email}'/>">
                <button type="submit" style="background: transparent; border: 1px solid #3f3f46; color: #d4d4d8; padding: 10px 16px; border-radius: 6px; width: 100%; cursor: pointer; font-size: 14px; transition: all 0.2s ease;">
                    🔄 Chưa nhận được email? Gửi lại
                </button>
            </form>
        </div>

        <div class="auth-footer" style="margin-top: 10px;">
            <span>Đã xác thực xong?</span>
            <a href="${pageContext.request.contextPath}/auth/login" class="auth-link-highlight">Đăng nhập vào hệ thống</a>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
