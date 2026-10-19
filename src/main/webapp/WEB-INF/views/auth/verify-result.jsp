<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Kết Quả Xác Thực - Cinetik" scope="request"/>
<c:set var="extraCss" value="auth.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="auth-container">
    <div class="auth-card" style="max-width: 520px; text-align: center;">

        <%-- Trường hợp 1: Kích hoạt THÀNH CÔNG --%>
        <c:if test="${status eq 'success'}">
            <div style="font-size: 56px; margin-bottom: 12px;">🎉</div>
            <h2 class="auth-title" style="color: #4ade80; margin-bottom: 8px;">Kích Hoạt Thành Công!</h2>
            <p class="auth-subtitle" style="margin-bottom: 20px;">
                Chào mừng <strong style="color: #ffffff;"><c:out value="${fullName}"/></strong> đến với <strong>Cinetik Cinema</strong>!
            </p>

            <div class="auth-alert success" style="margin-bottom: 24px; text-align: left;">
                <span>${message}</span>
            </div>

            <a href="${pageContext.request.contextPath}/auth/login?verified=true" class="btn-auth-submit" style="display: block; text-decoration: none; text-align: center;">
                🔑 Đăng Nhập Ngay
            </a>
        </c:if>

        <%-- Trường hợp 2: Token HẾT HẠN --%>
        <c:if test="${status eq 'expired'}">
            <div style="font-size: 56px; margin-bottom: 12px;">⏳</div>
            <h2 class="auth-title" style="color: #fbbf24; margin-bottom: 8px;">Liên Kết Đã Hết Hạn</h2>
            <p class="auth-subtitle" style="margin-bottom: 20px;">
                Liên kết xác thực tài khoản có thời hạn 24 giờ và đã hết hạn.
            </p>

            <div class="auth-alert error" style="margin-bottom: 24px; text-align: left;">
                <span>${message}</span>
            </div>

            <form action="${pageContext.request.contextPath}/auth/resend-verification" method="POST" style="margin-bottom: 20px;">
                <input type="hidden" name="email" value="<c:out value='${email}'/>">
                <button type="submit" class="btn-auth-submit" style="width: 100%;">
                    🔄 Gửi Lại Email Kích Hoạt
                </button>
            </form>

            <div class="auth-footer">
                <a href="${pageContext.request.contextPath}/auth/login" class="auth-link">Quay lại trang Đăng nhập</a>
            </div>
        </c:if>

        <%-- Trường hợp 3: Token KHÔNG HỢP LỆ hoặc ĐÃ DÙNG --%>
        <c:if test="${status eq 'invalid' || status eq 'error'}">
            <div style="font-size: 56px; margin-bottom: 12px;">❌</div>
            <h2 class="auth-title" style="color: #f87171; margin-bottom: 8px;">Mã Xác Thực Không Hợp Lệ</h2>
            <p class="auth-subtitle" style="margin-bottom: 20px;">
                Mã xác thực không tồn tại hoặc tài khoản này đã được kích hoạt trước đó.
            </p>

            <div class="auth-alert error" style="margin-bottom: 24px; text-align: left;">
                <span>${message}</span>
            </div>

            <div style="display: flex; gap: 12px; justify-content: center;">
                <a href="${pageContext.request.contextPath}/auth/login" class="btn-auth-submit" style="flex: 1; text-decoration: none; text-align: center;">
                    Đăng Nhập
                </a>
                <a href="${pageContext.request.contextPath}/auth/register" style="flex: 1; display: inline-block; padding: 12px; background: transparent; border: 1px solid #3f3f46; color: #d4d4d8; text-decoration: none; border-radius: 8px; text-align: center; font-weight: 500;">
                    Đăng Ký Mới
                </a>
            </div>
        </c:if>

    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
