<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Đăng Nhập - Cinetik" scope="request"/>
<c:set var="extraCss" value="auth.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="auth-container">
    <article class="auth-card">
        <header class="auth-header">
            <h1 class="auth-title">Đăng Nhập</h1>
            <p class="auth-subtitle">Chào mừng bạn quay lại với Cinetik Cinema</p>
        </header>

        <%-- Thông báo vừa đăng ký tài khoản thành công --%>
        <c:if test="${param.registered eq 'true'}">
            <aside class="auth-alert success" role="status">
                <span>✓ Đăng ký tài khoản thành công! Vui lòng đăng nhập để tiếp tục.</span>
            </aside>
        </c:if>

        <%-- Thông báo vừa kích hoạt email thành công --%>
        <c:if test="${param.verified eq 'true'}">
            <aside class="auth-alert success" role="status">
                <span>🎉 Kích hoạt tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ.</span>
            </aside>
        </c:if>

        <%-- Thông báo tài khoản bị khóa --%>
        <c:if test="${param.error eq 'account_locked'}">
            <aside class="auth-alert error" role="alert">
                <span>Tài khoản của bạn đã bị khóa hoặc hết phiên đăng nhập. Vui lòng liên hệ ban quản trị.</span>
            </aside>
        </c:if>

        <%-- Thông báo lỗi từ Servlet (sai mật khẩu, chưa kích hoạt...) --%>
        <c:if test="${not empty errorMessage}">
            <aside class="auth-alert error" role="alert">
                <span>${errorMessage}</span>
                <c:if test="${not empty unverifiedEmail}">
                    <p class="alert-action">
                        <a href="${pageContext.request.contextPath}/auth/resend-verification?email=${unverifiedEmail}" class="auth-link-highlight">
                            ✉️ Bấm vào đây để nhận lại email kích hoạt
                        </a>
                    </p>
                </c:if>
            </aside>
        </c:if>

        <%-- Form đăng nhập chuẩn HTML5 --%>
        <form action="${pageContext.request.contextPath}/auth/login" method="POST" class="auth-form" autocomplete="on">
            <c:if test="${not empty param.redirect}">
                <input type="hidden" name="redirect" value="<c:out value='${param.redirect}'/>">
            </c:if>

            <p class="form-group">
                <label for="username">Email hoặc Số điện thoại</label>
                <input type="text"
                       id="username"
                       name="username"
                       class="form-input"
                       placeholder="name@example.com hoặc 09xxxxxxxx"
                       value="<c:out value='${param.username}'/>"
                       autocomplete="username"
                       required
                       autofocus>
            </p>

            <p class="form-group">
                <label for="password">Mật khẩu</label>
                <input type="password"
                       id="password"
                       name="password"
                       class="form-input"
                       placeholder="Nhập mật khẩu của bạn"
                       autocomplete="current-password"
                       required>
            </p>

            <nav class="form-options" aria-label="Tùy chọn đăng nhập">
                <label class="remember-label">
                    <input type="checkbox" name="rememberMe" value="true">
                    <span>Ghi nhớ đăng nhập</span>
                </label>
                <a href="${pageContext.request.contextPath}/auth/forgot-password" class="auth-link">Quên mật khẩu?</a>
            </nav>

            <button type="submit" class="btn-auth-submit">Đăng Nhập</button>
        </form>

        <footer class="auth-footer">
            <span>Bạn chưa có tài khoản? </span>
            <a href="${pageContext.request.contextPath}/auth/register" class="auth-link-highlight">Đăng ký ngay</a>
        </footer>
    </article>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
