<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Kết Quả Xác Thực - Cinetik" scope="request"/>
<c:set var="extraCss" value="auth.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="auth-container">
    <article class="auth-card notice-card text-center">

        <%-- Trường hợp 1: Kích hoạt THÀNH CÔNG --%>
        <c:if test="${status eq 'success'}">
            <figure class="verify-icon">🎉</figure>
            <h1 class="auth-title status-success">Kích Hoạt Thành Công!</h1>
            <p class="auth-subtitle">
                Chào mừng <strong class="highlight-text"><c:out value="${fullName}"/></strong> đến với <strong>Cinetik Cinema</strong>!
            </p>

            <aside class="auth-alert success" role="status">
                <span>${message}</span>
            </aside>

            <nav class="auth-actions" aria-label="Đăng nhập">
                <a href="${pageContext.request.contextPath}/auth/login?verified=true" class="btn-auth-submit btn-block">
                    🔑 Đăng Nhập Ngay
                </a>
            </nav>
        </c:if>

        <%-- Trường hợp 2: Token HẾT HẠN (quá 24 giờ) --%>
        <c:if test="${status eq 'expired'}">
            <figure class="verify-icon">⏳</figure>
            <h1 class="auth-title status-warning">Liên Kết Đã Hết Hạn</h1>
            <p class="auth-subtitle">
                Liên kết xác thực tài khoản có thời hạn 24 giờ và hiện tại đã hết hạn.
            </p>

            <aside class="auth-alert error" role="alert">
                <span>${message}</span>
            </aside>

            <nav class="auth-actions" aria-label="Gửi lại kích hoạt">
                <form action="${pageContext.request.contextPath}/auth/resend-verification" method="POST">
                    <input type="hidden" name="email" value="<c:out value='${email}'/>">
                    <button type="submit" class="btn-auth-submit btn-block">
                        🔄 Gửi Lại Email Kích Hoạt
                    </button>
                </form>
            </nav>

            <footer class="auth-footer">
                <a href="${pageContext.request.contextPath}/auth/login" class="auth-link">Quay lại trang Đăng nhập</a>
            </footer>
        </c:if>

        <%-- Trường hợp 3: Token KHÔNG HỢP LỆ hoặc LỖI --%>
        <c:if test="${status eq 'invalid' || status eq 'error'}">
            <figure class="verify-icon">❌</figure>
            <h1 class="auth-title status-error">Mã Xác Thực Không Hợp Lệ</h1>
            <p class="auth-subtitle">
                Mã xác thực không tồn tại, đã sử dụng hoặc tài khoản này đã được kích hoạt trước đó.
            </p>

            <aside class="auth-alert error" role="alert">
                <span>${message}</span>
            </aside>

            <nav class="btn-group" aria-label="Điều hướng">
                <a href="${pageContext.request.contextPath}/auth/login" class="btn-auth-submit btn-flex">
                    Đăng Nhập
                </a>
                <a href="${pageContext.request.contextPath}/auth/register" class="btn-secondary btn-flex">
                    Đăng Ký Mới
                </a>
            </nav>
        </c:if>

    </article>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

