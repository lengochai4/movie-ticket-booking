<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập | Cinema N3</title>
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
                <h1 class="auth-title">Chào mừng trở lại!</h1>
                <p class="auth-subtitle">Đăng nhập để đặt vé và nhận ưu đãi điểm thưởng</p>
            </div>

            <!-- Thông báo lỗi -->
            <c:if test="${not empty errorMessage}">
                <div class="auth-alert auth-alert-danger" id="auth-login-error">
                    <span>⚠️</span>
                    <span><c:out value="${errorMessage}" /></span>
                </div>
            </c:if>

            <!-- Thông báo thành công -->
            <c:if test="${not empty successMessage}">
                <div class="auth-alert auth-alert-success" id="auth-login-success">
                    <span>✅</span>
                    <span><c:out value="${successMessage}" /></span>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/auth/login" method="POST" class="auth-form" id="loginForm">
                <!-- Redirect back URL nếu có -->
                <c:if test="${not empty param.redirect}">
                    <input type="hidden" name="redirect" value="<c:out value='${param.redirect}' />">
                </c:if>

                <div class="auth-form-group">
                    <label for="email" class="auth-label">Địa chỉ Email</label>
                    <div class="auth-input-wrapper">
                        <input type="email" id="email" name="email" class="auth-input" 
                               placeholder="example@domain.com" required autofocus>
                    </div>
                </div>

                <div class="auth-form-group">
                    <label for="password" class="auth-label">
                        <span>Mật khẩu</span>
                    </label>
                    <div class="auth-input-wrapper">
                        <input type="password" id="password" name="password" class="auth-input" 
                               placeholder="••••••••" required>
                    </div>
                </div>

                <button type="submit" class="auth-btn auth-btn-primary" id="btnLoginSubmit">
                    Đăng nhập
                </button>
            </form>

            <div class="auth-footer">
                Chưa có tài khoản? 
                <a href="${pageContext.request.contextPath}/auth/register" class="auth-link" id="linkToRegister">Đăng ký ngay</a>
            </div>
            
            <div style="text-align: center; margin-top: 1rem;">
                <a href="${pageContext.request.contextPath}/" class="auth-link" style="color: #64748b; font-size: 0.85rem;">← Về trang chủ</a>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</body>
</html>
