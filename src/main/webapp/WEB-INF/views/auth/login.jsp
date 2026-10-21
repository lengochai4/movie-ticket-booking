<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Đăng Nhập - Cinetik" scope="request"/>
<c:set var="extraCss" value="auth.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="auth-container">
    <div class="auth-card">
        <h2 class="auth-title">Đăng Nhập</h2>
        <p class="auth-subtitle">Chào mừng bạn quay lại Cinetik</p>

        <%---- Thông báo khi vừa đăng ký thành công chuyển sang ----%>
        <c:if test="${param.registered eq 'true'}">
            <div class="auth-alert success">
                <span>Đăng ký tài khoản thành công! Vui lòng đăng nhập</span>
            </div>
        </c:if>

        <%---- Thông báo khi kích hoạt email thành công ----%>
        <c:if test="${param.verified eq 'true'}">
            <div class="auth-alert success">
                <span>🎉 Kích hoạt tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ.</span>
            </div>
        </c:if>

        <%---- Thông báo khi tài khoản bị khóa từ Filter ----%>
        <c:if test="${param.error eq 'account_locked'}">
            <div class="auth-alert error">
                <span>Tài khoản của bạn đã bị khóa hoặc hết phiên đăng nhập. Vui lòng liên hệ ban quản trị.</span>
            </div>
        </c:if>

        <%--- Thông báo lỗi từ Servlet nếu đăng nhập thất bại ----%>
        <c:if test="${not empty errorMessage}">
            <div class="auth-alert error">
                <span>${errorMessage}</span>
                <c:if test="${not empty unverifiedEmail}">
                    <div style="margin-top: 8px;">
                        <a href="${pageContext.request.contextPath}/auth/resend-verification?email=${unverifiedEmail}" class="auth-link-highlight" style="font-size: 13px; text-decoration: underline;">
                            ✉️ Bấm vào đây để nhận lại email kích hoạt
                        </a>
                    </div>
                </c:if>
            </div>
        </c:if>

        <%---- Form gửi dữ liệu sang AuthServlet bằng phương thức POST ----%>
        <form action="${pageContext.request.contextPath}/auth/login" method="POST" class="auth-form">
            <c:if test="${not empty param.redirect}">
                <input type="hidden" name="redirect" value="<c:out value='${param.redirect}'/>">
            </c:if>

            <%---- Ô nhập email, tên đăng nhập ----%>
            <div class="form-group">
                <label for="username">Email hoặc Tên đăng nhập</label>
                <input type ="text"
                id="username"
                name="username"
                class="form-input"
                placeholder="Nhập email hoặc tên tài khoản"
                value="${param.username}"
                required
                autofocus>
            </div>

            <%---- Ô nhập mật khẩu ----%>
            <div class="form-group">
                <label for="password">Mật khẩu</label>
                <input type="password"
                    id="password"
                    name="password"
                    class="form-input"
                    placeholder="Nhập mật khẩu"
                    required>
            </div>

            <%---- Ghi nhớ & Quên mật khẩu ----%>
            <div class="form-options"
                <label class="remember-label"</label>
                    <input type="checkbox" name="rememberMe" value="True">
                    <span>Ghi nhớ đăng nhập</span>
                </label>
                <a href="${pageContext.request.contextPath}/auth/forgot-password" class="auth-link">Quên mật khẩu?</a>
            </div>

            <%-- Nút Submit --%>
            <button type="submit" class="btn-auth-submit">Đăng Nhập</button>
        </form>

        <%-- Chuyển sang trang Đăng ký --%>
        <div class="auth-footer">
            <span>Bạn chưa có tài khoản? </span>
            <a href="${pageContext.request.contextPath}/auth/register" class="auth-link-highlight">Đăng ký ngay</a>
        </div>
    </div>
</main>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />




