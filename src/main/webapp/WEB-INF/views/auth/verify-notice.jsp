<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Xác Nhận Email - Cinetik" scope="request"/>
<c:set var="extraCss" value="auth.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="auth-container">
    <article class="auth-card notice-card">
        <header class="auth-header text-center">
            <figure class="verify-icon">📧</figure>
            <h1 class="auth-title">Kiểm Tra Hộp Thư Của Bạn</h1>
            <p class="auth-subtitle">Hệ thống đã gửi liên kết kích hoạt tài khoản đến địa chỉ:</p>
        </header>

        <p class="verify-email-badge">
            <c:out value="${email != null ? email : param.email}"/>
        </p>

        <%-- Thông báo gửi lại email thành công --%>
        <c:if test="${resendSuccess eq true}">
            <aside class="auth-alert success" role="status">
                <span>✓ Đã gửi lại email kích hoạt thành công! Vui lòng kiểm tra hộp thư của bạn.</span>
            </aside>
        </c:if>

        <%-- Thông báo lỗi gửi email --%>
        <c:if test="${not empty emailError}">
            <aside class="auth-alert error" role="alert">
                <span>${emailError}</span>
            </aside>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <aside class="auth-alert error" role="alert">
                <span>${errorMessage}</span>
            </aside>
        </c:if>

        <ul class="verify-guidelines">
            <li>Mở email từ <strong>Cinetik Cinema</strong> và nhấp vào nút <strong>Xác nhận tài khoản</strong>.</li>
            <li>Liên kết có hiệu lực an toàn trong vòng <strong>24 giờ</strong>.</li>
            <li><em>Mẹo: Nếu không tìm thấy trong Hộp thư đến, vui lòng kiểm tra thư mục <strong>Spam (Thư rác)</strong> hoặc <strong>Quảng cáo</strong>.</em></li>
        </ul>

        <nav class="auth-actions" aria-label="Hành động xác thực">
            <a href="https://mail.google.com" target="_blank" rel="noopener noreferrer" class="btn-auth-submit btn-block">
                🚀 Mở Gmail Ngay
            </a>

            <form action="${pageContext.request.contextPath}/auth/resend-verification" method="POST">
                <input type="hidden" name="email" value="<c:out value='${email != null ? email : param.email}'/>">
                <button type="submit" class="btn-secondary btn-block">
                    🔄 Chưa nhận được email? Gửi lại
                </button>
            </form>
        </nav>

        <footer class="auth-footer">
            <span>Đã xác thực xong? </span>
            <a href="${pageContext.request.contextPath}/auth/login" class="auth-link-highlight">Đăng nhập vào hệ thống</a>
        </footer>
    </article>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

