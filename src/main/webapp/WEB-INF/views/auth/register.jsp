<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Đăng Ký Tài Khoản - Cinetik" scope="request"/>
<c:set var="extraCss" value="auth.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="auth-container">
    <article class="auth-card register-card">
        <header class="auth-header">
            <h1 class="auth-title">Đăng Ký Thành Viên</h1>
            <p class="auth-subtitle">Tạo tài khoản để tích lũy điểm thưởng và nhận ưu đãi độc quyền tại Cinetik</p>
        </header>

        <%-- Thông báo lỗi từ RegisterServlet nếu có --%>
        <c:if test="${not empty errorMessage}">
            <aside class="auth-alert error" role="alert">
                <span>${errorMessage}</span>
            </aside>
        </c:if>

        <%-- Form đăng ký chuẩn HTML5 --%>
        <form action="${pageContext.request.contextPath}/auth/register" method="POST" class="auth-form" autocomplete="on">

            <p class="form-group">
                <label for="fullName">Họ và tên</label>
                <input type="text"
                       id="fullName"
                       name="fullName"
                       class="form-input"
                       placeholder="Ví dụ: Nguyễn Văn A"
                       value="<c:out value='${param.fullName}'/>"
                       autocomplete="name"
                       required
                       autofocus>
            </p>

            <p class="form-group">
                <label for="email">Địa chỉ Email</label>
                <input type="email"
                       id="email"
                       name="email"
                       class="form-input"
                       placeholder="name@example.com"
                       value="<c:out value='${param.email}'/>"
                       autocomplete="email"
                       required>
            </p>

            <p class="form-group">
                <label for="phone">Số điện thoại</label>
                <input type="tel"
                       id="phone"
                       name="phone"
                       class="form-input"
                       placeholder="0912345678"
                       value="<c:out value='${param.phone}'/>"
                       pattern="[0-9]{10}"
                       maxlength="10"
                       title="Vui lòng nhập đúng 10 chữ số điện thoại"
                       autocomplete="tel"
                       required>
            </p>

            <p class="form-group">
                <label for="password">Mật khẩu</label>
                <input type="password"
                       id="password"
                       name="password"
                       class="form-input"
                       placeholder="Tạo mật khẩu mạnh"
                       pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,}"
                       title="Mật khẩu phải tối thiểu 8 ký tự, gồm ít nhất 1 chữ thường, 1 chữ hoa, 1 số và 1 ký tự đặc biệt"
                       minlength="8"
                       autocomplete="new-password"
                       required>
                <small class="form-hint">Tối thiểu 8 ký tự: gồm chữ hoa, chữ thường, số và ký tự đặc biệt (@, #, $...)</small>
            </p>

            <p class="form-group">
                <label for="confirmPassword">Xác nhận mật khẩu</label>
                <input type="password"
                       id="confirmPassword"
                       name="confirmPassword"
                       class="form-input"
                       placeholder="Nhập lại chính xác mật khẩu"
                       minlength="8"
                       autocomplete="new-password"
                       required>
            </p>

            <p class="form-options">
                <label class="remember-label">
                    <input type="checkbox" name="agreeTerms" value="true" required>
                    <span>Tôi đồng ý với <a href="#" class="auth-link">Điều khoản & Chính sách</a> của Cinetik Cinema</span>
                </label>
            </p>

            <button type="submit" class="btn-auth-submit">Đăng Ký Tài Khoản</button>
        </form>

        <footer class="auth-footer">
            <span>Đã có tài khoản? </span>
            <a href="${pageContext.request.contextPath}/auth/login" class="auth-link-highlight">Đăng nhập ngay</a>
        </footer>
    </article>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>