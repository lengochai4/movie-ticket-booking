<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Đăng Ký Tài Khoản - Cinetik" scope="request"/>
<c:set var="extraCss" value="auth.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="auth-container">
    <div class="auth-card register-card">
        <h2 class="auth-title">Đăng Ký Thành Viên</h2>
        <p class="auth-subtitle">Tạo tài khoản để tích điểm và nhận ưu đãi vé xem phim</p>

        <%--- Thông báo lỗi từ Servlet nếu đăng ký thất bại ----%>
        <c:if test="${not empty errorMessage}">
            <div class="auth-alert error">
                <span>${errorMessage}</span>
            </div>
        </c:if>
        <%---- Form gửi dữ liệu sang AuthServlet bằng phương thức POST ----%>
        <form action="${pageContext.request.contextPath}/auth/register" method="POST" class="auth-form">

        <%---- Họ và tên ----%>
        <div class="form-group">
            <label for="fullName">Họ và tên</label>
            <input type="text"
                   id="fullName"
                   name="fullName"
                   class="form-input"
                   placeholder="Ví dụ: Nguyễn Văn A"
                   value="${param.fullName}"
                   required
                   autofocus>
        </div>

        <%---- Email ----%>
        <div class="form-group">
            <label for="email">Email</label>
            <input type="email"
                   id="email"
                   name="email"
                   class="form-input"
                   placeholder="name@example.com"
                   value="${param.email}"
                   required>
        </div>

        <%---- Số điện thoại ----%>
        <div class="form-group">
            <label for="phone">Số điện thoại</label>
            <input type="tel"
                   id="phone"
                   name="phone"
                   class="form-input"
                   placeholder="0912 345 678"
                   pattern="[0-9]{10}"
                   title="Vui lòng nhập số điện thoại gồm 10 chữ số"
                   required>
        </div>

        <%---- Mật khẩu ----%>
        <div class="form-group">
            <label for="password">Mật khẩu</label>
            <input type="password"
                   id="password"
                   name="password"
                   class="form-input"
                   placeholder="Nhập mật khẩu"
                   pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,}"
                   title="Mật khẩu phải dài ít nhất 8 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 chữ số và 1 ký tự đặc biệt (ví dụ: @, #, $, %, !...)"
                   minlength="8"
                   required>
            <small class="form-hint"> Tối thiểu 8 ký tự: gồm chữ hoa, chữ thường, số và ký tự đặc biệt (@, #, $...)</small>
        </div>

        <%---- Xác thực mật khẩu ----%>
        <div class="form-group">
            <label for="confirmPassword">Xác nhận mật khẩu</label>
            <input type="password"
                   id="confirmPassword"
                   name="confirmPassword"
                   class="form-input"
                   placeholder="Nhập lại mật khẩu ở trên"
                   minlength="8"
                   required>
        </div>

        <%---- Checkbox đồng ý điều khoản ----%>
        <div class="form-options">
            <label class="remember-label">
                <input type="checkbox" name="agreeTerms" value="True" required>
                <span>Tôi đồng ý với <a href="#" class="auth-link">Điều khoản & Chính sách</a> của rạp</span>
            </label>
        </div>

        <%-- Nút Submit --%>
            <button type="submit" class="btn-auth-submit">Đăng Ký Tài Khoản</button>
        </form>

        <%---- Chuyển sang trang đăng nhập ----%>
        <div class="auth-footer">
            <span>Đã có tài khoản?</span>
            <a href="${pageContext.request.contextPath}/auth/login" class="auth-link-highlight">Đăng nhập ngay</a>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>