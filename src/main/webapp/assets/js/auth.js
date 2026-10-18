/**
 * Cinema N3 - Auth & Review Client Script
 * Thuộc sở hữu của Ân (@an)
 */
document.addEventListener('DOMContentLoaded', function () {
    // 1. Password confirmation check on registration
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', function (e) {
            const password = document.getElementById('password');
            if (password && password.value.length < 6) {
                alert('Mật khẩu phải có tối thiểu 6 ký tự!');
                e.preventDefault();
            }
        });
    }

    // 2. Change password validation
    const changePasswordForm = document.getElementById('changePasswordForm');
    if (changePasswordForm) {
        changePasswordForm.addEventListener('submit', function (e) {
            const newPassword = document.getElementById('newPassword');
            const oldPassword = document.getElementById('oldPassword');
            if (newPassword && newPassword.value.length < 6) {
                alert('Mật khẩu mới phải có tối thiểu 6 ký tự!');
                e.preventDefault();
                return;
            }
            if (oldPassword && newPassword && oldPassword.value === newPassword.value) {
                alert('Mật khẩu mới không được trùng với mật khẩu cũ!');
                e.preventDefault();
            }
        });
    }

    // 3. Review form validation
    const submitReviewForm = document.getElementById('submitReviewForm');
    if (submitReviewForm) {
        submitReviewForm.addEventListener('submit', function (e) {
            const comment = submitReviewForm.querySelector('textarea[name="comment"]');
            if (comment && comment.value.trim().length < 3) {
                alert('Nội dung đánh giá cần ít nhất 3 ký tự!');
                e.preventDefault();
            }
        });
    }
});
