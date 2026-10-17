<%@ page contentType="text/html; charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<c:set var="pageTitle" value="Hồ Sơ Cá Nhân - Cinetik" scope="request"/>
<c:set var="extraCss" value="profile.css" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="profile-container">
    <div class="profile-layout">

        <%---- Cột trái: Sidebar thông tin ----%>
        <aside class="profile-sidebar">
            <div class="user-card-summary">
                <div class="avatar-wrapper">
                    <%---- Avatar mặc định lấy chữ cái đầu của tên hoặc icon ----%>
                    <div class="avatar-placeholder">
                        ${not empty sessionScope.loginUser.fullName ? sessionScope.loginUser.fullName.substring(0,1).toUpperCase() : 'U'}
                    </div>
                </div>
                <h3 class="user-name">${sessionScope.loginUser.fullName}</h3>
                <p class="user-email">${sessionScope.loginUser.email}</p>
                <span class="user-badge">Thành Viên Cinetik</span>
            </div>

            <nav class="profile-menu">
                <button type="button" class="menu-item active" onclick="switchTab('info-tab', this)">
                    <span class="menu-icon">👤</span> Thông tin tài khoản
                </button>
                <button type="button" class="menu-item" onclick="switchTab('password-tab', this)">
                    <span class="menu-icon">🔒</span> Đổi mật khẩu
                </button>
                <a href="${pageContext.request.contextPath}/booking/history" class="menu-item">
                    <span class="menu-icon">🎟️</span> Lịch sử đặt vé
                </a>
                <c:if test="${sessionScope.loginUser.role == 'ADMIN'}">
                    <a href="${pageContext.request.contextPath}/admin/users" class="menu-item" style="color: #fcca46; border: 1px dashed rgba(252,202,70,0.4);">
                        <span class="menu-icon">👑</span> Quản lý người dùng (Admin)
                    </a>
                </c:if>
                <a href="${pageContext.request.contextPath}/auth/logout" class="menu-item logout-link">
                    <span class="menu-icon">🚪</span> Đăng xuất
                </a>
            </nav>
        </aside>

        <%---- Cột phải: Nội dung chính ----%>
        <section class="profile-content">
            <%---- Thông báo lỗi / Thành công ----%>
            <c:if test="${not empty errorMessage}">
                <div class="profile-alert error">
                    <span>⚠️ ${errorMessage}</span>
                </div>
            </c:if>
            <c:if test="${not empty successMessage}">
                <div class="profile-alert success">
                    <span>✅ ${successMessage}</span>
                </div>
            </c:if>

            <%---- TAB 1: Thông tin tài khoản ----%>
            <div id="info-tab" class="tab-pane active">
                <div class="tab-header">
                    <h2>Thông Tin Cá Nhân</h2>
                    <p>Quản lý và cập nhật thông tin liên hệ của bạn để nhận vé và ưu đãi</p>
                </div>

                <form action="${pageContext.request.contextPath}/auth/profile/update" method="POST" class="profile-form">
                    <div class="form-row">
                        <div class="form-group">
                            <label for="fullName">Họ và tên</label>
                            <input type="text"
                                   id="fullName"
                                   name="fullName"
                                   class="form-input"
                                   value="${sessionScope.loginUser.fullName}"
                                   required>
                        </div>

                        <div class="form-group">
                            <label for="phone">Số điện thoại</label>
                            <input type="tel"
                                   id="phone"
                                   name="phone"
                                   class="form-input"
                                   value="${sessionScope.loginUser.phone}"
                                   placeholder="0912 345 678"
                                   pattern="[0-9]{10}"
                                   title="Số điện thoại gồm 10 chữ số">
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="email">Địa chỉ email</label>
                        <input type="email"
                               id="email"
                               name="email"
                               class="form-input readonly-input"
                               value="${sessionScope.loginUser.email}"
                               readonly
                               title="Email dùng để đăng nhập, không thể thay đổi">
                        <small class="field-hint">Email định danh tài khoản, không thể tự ý thay đổi.</small>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn-primary">Lưu Thay Đổi</button>
                    </div>
                </form>
            </div>

            <%---- TAB 2: Đổi mật khẩu ----%>
            <div id="password-tab" class="tab-pane">
                <div class="tab-header">
                    <h2>Đổi mật khẩu</h2>
                    <p> Để bảo vệ tài khoản, không chia sẻ mật khẩu của bạn cho người khác</p>
                </div>

                <form action="${pageContext.request.contextPath}/auth/profile/change-password" method="POST" class="profile-form" onsubmit="return validatePasswordForm();">
                    <div class="form-group">
                        <label for="currentPassword">Mật khẩu hiện tại</label>
                        <input type="password"
                               id="currentPassword"
                               name="currentPassword"
                               class="form-input"
                               placeholder="Nhập mật khẩu bạn đang sử dụng"
                               required>
                    </div>

                    <div class="form-group">
                        <label for="newPassword">Mật khẩu mới</label>
                        <input type="password"
                               id="newPassword"
                               name="newPassword"
                               class="form-input"
                               placeholder="Tối thiểu 8 ký tự, có chữ hoa, số & ký tự đặc biệt"
                               required>

                        <%---- Danh sách tiêu chí Regex trực quan ----%>
                        <div class="pwd-rules-box">
                            <div class="rule-item" id="rule-length">
                                <span class="rule-icon">○</span> Tối thiểu 8 ký tự
                            </div>
                            <div class="rule-item" id="rule-letter">
                                <span class="rule-icon">○</span> Bao gồm cả chữ hoa (A-Z) và chữ thường (a-z)
                            </div>
                            <div class="rule-item" id="rule-digit">
                                <span class="rule-icon">○</span> Ít nhất 1 chữ số (0-9)
                            </div>
                            <div class="rule-item" id="rule-special">
                                <span class="rule-icon">○</span> Ít nhất 1 ký tự đặc biệt (!@#$%^&*...)
                            </div>
                        </div>
                        <span id="pwdRegexMsg" class="error-inline" style="display: none;">⚠️ Mật khẩu mới chưa đáp ứng đủ các tiêu chuẩn bảo mật trên!</span>
                    </div>

                    <div class="form-group">
                        <label for="confirmNewPassword">Xác nhận mật khẩu mới</label>
                        <input type="password"
                               id="confirmNewPassword"
                               name="confirmNewPassword"
                               class="form-input"
                               placeholder="Nhập lại mật khẩu mới"
                               required>
                        <span id="pwdMismatchMsg" class="error-inline" style="display: none;">⚠️ Mật khẩu xác nhận không trùng khớp!</span>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn-primary">Cập Nhật Mật Khẩu Mới</button>
                    </div>
                </form>
            </div>

        </section>
    </div>
</main>

<script>
    // Hàm chuyển tab qua lại
    function switchTab(tabId, element) {
        document.querySelectorAll('.tab-pane').forEach(el => el.classList.remove('active'));
        document.querySelectorAll('.profile-menu .menu-item').forEach(el => el.classList.remove('active'));

        document.getElementById(tabId).classList.add('active');
        element.classList.add('active');
    }

    const newPwdInput = document.getElementById('newPassword');
    const confirmPwdInput = document.getElementById('confirmNewPassword');
    const regexMsg = document.getElementById('pwdRegexMsg');
    const mismatchMsg = document.getElementById('pwdMismatchMsg');

    const ruleLength = document.getElementById('rule-length');
    const ruleLetter = document.getElementById('rule-letter');
    const ruleDigit = document.getElementById('rule-digit');
    const ruleSpecial = document.getElementById('rule-special');

    // Kiểm tra định dạng Regex theo thời gian thực khi người dùng gõ
    function updateRule(element, isValid) {
        if (isValid) {
            element.classList.add('valid');
            element.querySelector('.rule-icon').textContent = '✓';
        } else {
            element.classList.remove('valid');
            element.querySelector('.rule-icon').textContent = '○';
        }
    }

    newPwdInput.addEventListener('input', function() {
        const val = this.value;

        const hasLength = val.length >= 8;
        const hasUpperAndLower = /[A-Z]/.test(val) && /[a-z]/.test(val);
        const hasDigit = /\d/.test(val);
        const hasSpecial = /[@$!%*?&#^()_+\-=\[\]{};':"\\|,.<>\/?]/.test(val);

        updateRule(ruleLength, hasLength);
        updateRule(ruleLetter, hasUpperAndLower);
        updateRule(ruleDigit, hasDigit);
        updateRule(ruleSpecial, hasSpecial);

        // Ẩn thông báo lỗi regex khi người dùng đã thỏa mãn tất cả
        if (hasLength && hasUpperAndLower && hasDigit && hasSpecial) {
            regexMsg.style.display = 'none';
        }

        // Kiểm tra khớp mật khẩu xác nhận nếu đã nhập
        if (confirmPwdInput.value.length > 0) {
            if (val === confirmPwdInput.value) {
                mismatchMsg.style.display = 'none';
            }
        }
    });

    confirmPwdInput.addEventListener('input', function() {
        if (this.value === newPwdInput.value) {
            mismatchMsg.style.display = 'none';
        }
    });

    // Kiểm tra toàn diện khi submit Form
    function validatePasswordForm() {
        const newPwd = newPwdInput.value;
        const confirmPwd = confirmPwdInput.value;

        // Regex chuẩn: >=8 ký tự, có chữ hoa, chữ thường, số, ký tự đặc biệt
        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#^()_+\-=\[\]{};':"\\|,.<>\/?]).{8,}$/;

        if (!passwordRegex.test(newPwd)) {
            regexMsg.style.display = 'block';
            newPwdInput.focus();
            return false;
        }
        regexMsg.style.display = 'none';

        if (newPwd !== confirmPwd) {
            mismatchMsg.style.display = 'block';
            confirmPwdInput.focus();
            return false;
        }
        mismatchMsg.style.display = 'none';

        return true;
    }
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>