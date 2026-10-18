<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Người dùng | Admin Cinema N3</title>
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <!-- Auth CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body style="background: #0b0c10; min-height: 100vh; margin: 0; color: #f1f5f9;">

    <div class="admin-user-page">
        <!-- Header -->
        <div class="admin-user-header">
            <div>
                <a href="${pageContext.request.contextPath}/admin/dashboard" style="color: #64748b; text-decoration: none; font-size: 0.9rem;">
                    ← Quay lại Dashboard
                </a>
                <h1 class="admin-user-title">
                    👥 Quản lý Người dùng & Phân quyền
                </h1>
            </div>

            <div style="display: flex; gap: 0.75rem; align-items: center;">
                <span style="font-size: 0.9rem; color: #94a3b8;">
                    Admin: <strong><c:out value="${sessionScope.loginUser.fullName}" /></strong>
                </span>
                <a href="${pageContext.request.contextPath}/auth/profile" class="auth-btn auth-btn-secondary auth-btn-sm">
                    Hồ sơ
                </a>
                <a href="${pageContext.request.contextPath}/auth/logout" class="auth-btn auth-btn-danger auth-btn-sm">
                    Đăng xuất
                </a>
            </div>
        </div>

        <!-- Alert notifications -->
        <c:if test="${not empty sessionScope.adminSuccess}">
            <div class="auth-alert auth-alert-success" id="adminSuccessAlert">
                <span>✅</span>
                <span><c:out value="${sessionScope.adminSuccess}" /></span>
            </div>
            <c:remove var="adminSuccess" scope="session" />
        </c:if>

        <c:if test="${not empty sessionScope.adminError}">
            <div class="auth-alert auth-alert-danger" id="adminErrorAlert">
                <span>⚠️</span>
                <span><c:out value="${sessionScope.adminError}" /></span>
            </div>
            <c:remove var="adminError" scope="session" />
        </c:if>

        <!-- User Table -->
        <div class="admin-user-table-wrap">
            <table class="admin-user-table">
                <thead>
                    <tr>
                        <th style="width: 60px;">ID</th>
                        <th>Họ và Tên</th>
                        <th>Email</th>
                        <th>Số điện thoại</th>
                        <th>Vai trò</th>
                        <th>Điểm thưởng</th>
                        <th>Trạng thái</th>
                        <th style="text-align: right;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="u" items="${users}">
                        <tr>
                            <td>#<c:out value="${u.userID}" /></td>
                            <td>
                                <strong><c:out value="${u.fullName}" /></strong>
                                <c:if test="${u.userID == sessionScope.loginUser.userID}">
                                    <span style="color: #f5c518; font-size: 0.75rem; margin-left: 0.35rem;">(Bạn)</span>
                                </c:if>
                            </td>
                            <td><c:out value="${u.email}" /></td>
                            <td><c:out value="${empty u.phone ? '---' : u.phone}" /></td>
                            <td>
                                <c:choose>
                                    <c:when test="${u.role == 1}">
                                        <span class="auth-badge auth-badge-admin">Admin</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="auth-badge auth-badge-user">User</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <span style="font-weight: 600; color: #f5c518;">
                                    <c:out value="${u.loyaltyPoints}" /> pts
                                </span>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${u.locked}">
                                        <span class="auth-badge auth-badge-locked">Đã khoá</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="auth-badge auth-badge-active">Hoạt động</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="text-align: right;">
                                <c:choose>
                                    <c:when test="${u.userID == sessionScope.loginUser.userID}">
                                        <span style="font-size: 0.8rem; color: #64748b; font-style: italic;">Không thể tự khoá</span>
                                    </c:when>
                                    <c:when test="${u.locked}">
                                        <form action="${pageContext.request.contextPath}/admin/users" method="POST" style="display: inline;">
                                            <input type="hidden" name="action" value="unlock">
                                            <input type="hidden" name="id" value="${u.userID}">
                                            <button type="submit" class="auth-btn auth-btn-success auth-btn-sm" 
                                                    onclick="return confirm('Mở khoá tài khoản này?');">
                                                🔓 Mở khoá
                                            </button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <form action="${pageContext.request.contextPath}/admin/users" method="POST" style="display: inline;">
                                            <input type="hidden" name="action" value="lock">
                                            <input type="hidden" name="id" value="${u.userID}">
                                            <button type="submit" class="auth-btn auth-btn-danger auth-btn-sm"
                                                    onclick="return confirm('Bạn có chắc chắn muốn khoá tài khoản này?');">
                                                🔒 Khoá
                                            </button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty users}">
                        <tr>
                            <td colspan="8" style="text-align: center; padding: 2rem; color: #64748b;">
                                Chưa có dữ liệu người dùng.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>

</body>
</html>
