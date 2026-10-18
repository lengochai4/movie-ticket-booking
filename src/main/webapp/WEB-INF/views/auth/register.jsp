<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký tài khoản | Cinema N3</title>
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <!-- Auth CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body class="auth-page">

    <div class="auth-container">
        <div class="auth-card">
            <div class="auth-header">
                <a href="${pageContext.request.contextPath}/" class="auth-brand" id="auth-logo-link">
                    CINEMA<span>N3</span>
                </a>
                <h1 class="auth-title">Tạo tài khoản mới</h1>
                <p class="auth-subtitle">Trở thành thành viên để tích luỹ điểm thưởng</p>
            </div>

            <!-- Thông báo lỗi -->
            <c:if test="${not empty errorMessage}">
                <div class="auth-alert auth-alert-danger" id="auth-register-error">
                    <span>⚠️</span>
                    <span><c:out value="${errorMessage}" /></span>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/auth/register" method="POST" class="auth-form" id="registerForm">
                <div class="auth-form-group">
                    <label for="fullName" class="auth-label">Họ và tên</label>
                    <div class="auth-input-wrapper">
                        <input type="text" id="fullName" name="fullName" class="auth-input" 
                               placeholder="Nguyễn Văn A" required autofocus>
                    </div>
                </div>

                <div class="auth-form-group">
                    <label for="email" class="auth-label">Địa chỉ Email</label>
                    <div class="auth-input-wrapper">
                        <input type="email" id="email" name="email" class="auth-input" 
                               placeholder="example@domain.com" required>
                    </div>
                </div>

                <div class="auth-form-group">
                    <label for="phone" class="auth-label">Số điện thoại</label>
                    <div class="auth-input-wrapper">
                        <input type="tel" id="phone" name="phone" class="auth-input" 
                               placeholder="0912345678" pattern="[0-9]{9,11}">
                    </div>
                </div>

                <div class="auth-form-group">
                    <label for="password" class="auth-label">Mật khẩu (tối thiểu 6 ký tự)</label>
                    <div class="auth-input-wrapper">
                        <input type="password" id="password" name="password" class="auth-input" 
                               placeholder="••••••••" minlength="6" required>
                    </div>
                </div>

                <button type="submit" class="auth-btn auth-btn-primary" id="btnRegisterSubmit">
                    Đăng ký ngay
                </button>
            </form>

            <div class="auth-footer">
                Đã có tài khoản? 
                <a href="${pageContext.request.contextPath}/auth/login" class="auth-link" id="linkToLogin">Đăng nhập ngay</a>
            </div>

            <div style="text-align: center; margin-top: 1rem;">
                <a href="${pageContext.request.contextPath}/" class="auth-link" style="color: #64748b; font-size: 0.85rem;">← Về trang chủ</a>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</body>
</html>
