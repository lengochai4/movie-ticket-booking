<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hồ sơ cá nhân | Cinema N3</title>
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <!-- Auth CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body class="auth-page" style="align-items: flex-start; padding-top: 3rem;">

    <div class="auth-container-wide">
        <!-- Top Navigation Bar -->
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
            <a href="${pageContext.request.contextPath}/" class="auth-brand">
                CINEMA<span>N3</span>
            </a>
            <div style="display: flex; gap: 1rem; align-items: center;">
                <c:if test="${sessionScope.loginUser.role == 1}">
                    <a href="${pageContext.request.contextPath}/admin/dashboard" class="auth-btn auth-btn-secondary auth-btn-sm">
                        ⚙️ Trang Quản Trị
                    </a>
                </c:if>
                <a href="${pageContext.request.contextPath}/orders/history" class="auth-btn auth-btn-secondary auth-btn-sm">
                    🎟️ Vé Của Tôi
                </a>
                <a href="${pageContext.request.contextPath}/auth/logout" class="auth-btn auth-btn-danger auth-btn-sm" id="btnLogout">
                    Đăng xuất
                </a>
            </div>
        </div>

        <!-- Alert messages -->
        <c:if test="${not empty errorMessage}">
            <div class="auth-alert auth-alert-danger" id="profileError">
                <span>⚠️</span>
                <span><c:out value="${errorMessage}" /></span>
            </div>
        </c:if>

        <c:if test="${not empty successMessage}">
            <div class="auth-alert auth-alert-success" id="profileSuccess">
                <span>✅</span>
                <span><c:out value="${successMessage}" /></span>
            </div>
        </c:if>

        <div class="auth-card" style="padding: 2.5rem;">
            <div class="auth-profile-layout">
                <!-- Sidebar info -->
                <div class="auth-profile-card">
                    <div class="auth-avatar-circle">
                        <c:out value="${sessionScope.loginUser.fullName.substring(0, 1).toUpperCase()}" default="U" />
                    </div>
                    <h2 style="margin: 0.5rem 0 0.25rem 0; font-size: 1.25rem; font-weight: 700;">
                        <c:out value="${sessionScope.loginUser.fullName}" />
                    </h2>
                    <p style="margin: 0 0 1rem 0; font-size: 0.85rem; color: var(--auth-text-muted);">
                        <c:out value="${sessionScope.loginUser.email}" />
                    </p>

                    <div>
                        <c:choose>
                            <c:when test="${sessionScope.loginUser.role == 1}">
                                <span class="auth-badge auth-badge-admin">Quản trị viên (Admin)</span>
                            </c:when>
                            <c:otherwise>
                                <span class="auth-badge auth-badge-user">Thành viên VIP</span>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="auth-points-box">
                        <div style="font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.5px;">Điểm thưởng tích luỹ</div>
                        <div class="auth-points-val" id="userLoyaltyPoints">
                            <c:out value="${sessionScope.loginUser.loyaltyPoints}" default="0" /> pts
                        </div>
                    </div>
                </div>

                <!-- Forms: Profile & Change Password -->
                <div style="display: flex; flex-direction: column; gap: 2.5rem;">
                    <!-- Update Profile Form -->
                    <div>
                        <h3 style="margin-top: 0; font-size: 1.25rem; font-weight: 700; border-bottom: 1px solid rgba(255,255,255,0.08); padding-bottom: 0.75rem;">
                            Thông tin cá nhân
                        </h3>
                        <form action="${pageContext.request.contextPath}/auth/profile" method="POST" class="auth-form" id="updateProfileForm">
                            <div class="auth-form-group">
                                <label for="emailReadonly" class="auth-label">Email (không thể thay đổi)</label>
                                <input type="email" id="emailReadonly" class="auth-input" 
                                       value="<c:out value='${sessionScope.loginUser.email}' />" readonly 
                                       style="opacity: 0.7; cursor: not-allowed;">
                            </div>

                            <div class="auth-form-group">
                                <label for="fullName" class="auth-label">Họ và tên</label>
                                <input type="text" id="fullName" name="fullName" class="auth-input" 
                                       value="<c:out value='${sessionScope.loginUser.fullName}' />" required>
                            </div>

                            <div class="auth-form-group">
                                <label for="phone" class="auth-label">Số điện thoại</label>
                                <input type="tel" id="phone" name="phone" class="auth-input" 
                                       value="<c:out value='${sessionScope.loginUser.phone}' />" placeholder="0912345678">
                            </div>

                            <div style="text-align: right;">
                                <button type="submit" class="auth-btn auth-btn-primary" id="btnUpdateProfile">
                                    Lưu thay đổi
                                </button>
                            </div>
                        </form>
                    </div>

                    <!-- Change Password Form -->
                    <div>
                        <h3 style="font-size: 1.25rem; font-weight: 700; border-bottom: 1px solid rgba(255,255,255,0.08); padding-bottom: 0.75rem;">
                            Đổi mật khẩu
                        </h3>
                        <form action="${pageContext.request.contextPath}/auth/change-password" method="POST" class="auth-form" id="changePasswordForm">
                            <div class="auth-form-group">
                                <label for="oldPassword" class="auth-label">Mật khẩu hiện tại</label>
                                <input type="password" id="oldPassword" name="oldPassword" class="auth-input" 
                                       placeholder="••••••••" required>
                            </div>

                            <div class="auth-form-group">
                                <label for="newPassword" class="auth-label">Mật khẩu mới (tối thiểu 6 ký tự)</label>
                                <input type="password" id="newPassword" name="newPassword" class="auth-input" 
                                       placeholder="••••••••" minlength="6" required>
                            </div>

                            <div style="text-align: right;">
                                <button type="submit" class="auth-btn auth-btn-secondary" id="btnChangePassword">
                                    Cập nhật mật khẩu
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</body>
</html>
