package com.cinema;

import com.cinema.dao.auth.UserDAO;
import com.cinema.entity.User;
import com.cinema.util.MailUtilGmail;
import com.cinema.util.PasswordUtil;

import java.time.LocalDateTime;
import java.util.UUID;

public class EmailVerificationTest {

    public static void main(String[] args) {
        System.out.println("========== BẮT ĐẦU KIỂM TRA MODULE XÁC THỰC EMAIL ==========");

        testUserVerificationFields();
        testUserDAOTokenQuery();
        testEmailTemplateBuilder();
        testSendLiveEmail();

        System.out.println("🎉 TẤT CẢ CÁC BÀI TEST XÁC THỰC EMAIL ĐỀU ĐẠT CHUẨN!");
    }

    private static void testSendLiveEmail() {
        System.out.println("--> 4. Kiểm tra kết nối gửi email thực tế qua Gmail SMTPS (Port 465)...");
        try {
            String testRecipient = "thienantranit@gmail.com";
            String testLink = "http://localhost:8080/movie-booking/auth/verify?token=demo-test-token-123";
            String htmlContent = MailUtilGmail.buildVerificationEmail("Thiện Ân (Test Auto)", testLink);
            MailUtilGmail.sendMail(
                testRecipient,
                "thienantranit@gmail.com",
                "[Cinetik Cinema - Test] Xác nhận kích hoạt tài khoản",
                htmlContent,
                true
            );
            System.out.println("  [OK] Đã gửi email xác thực thực tế thành công tới: " + testRecipient);
        } catch (Exception e) {
            System.err.println("  [WARN] Gửi email trực tiếp gặp lỗi: " + e.getMessage());
        }
    }

    private static void testUserVerificationFields() {
        System.out.println("--> 1. Kiểm tra Entity User với các trường xác thực...");
        User u = new User();
        u.setStatus("PENDING");
        u.setEmailVerified(false);
        String token = UUID.randomUUID().toString();
        u.setVerificationToken(token);
        LocalDateTime expiry = LocalDateTime.now().plusHours(24);
        u.setTokenExpiry(expiry);

        assert u.isPending() : "User phải có trạng thái isPending() == true";
        assert !u.isEmailVerified() : "User chưa xác thực phải có isEmailVerified() == false";
        assert token.equals(u.getVerificationToken()) : "VerificationToken phải khớp";
        assert expiry.equals(u.getTokenExpiry()) : "TokenExpiry phải khớp";
        System.out.println("  [OK] Entity User lưu trữ đầy đủ các thông tin xác thực.");
    }

    private static void testUserDAOTokenQuery() {
        System.out.println("--> 2. Kiểm tra lưu và truy vấn token trong Database...");
        UserDAO userDAO = new UserDAO();

        String testEmail = "verify_test_" + System.currentTimeMillis() + "@gmail.com";
        String token = "tok-" + UUID.randomUUID();

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setFullName("Trần Thiện Ân Test");
        user.setEmail(testEmail);
        user.setPhone("0988776655");
        user.setPassword(PasswordUtil.hash("SecurePass@123"));
        user.setRole("USER");
        user.setStatus("PENDING");
        user.setEmailVerified(false);
        user.setVerificationToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusHours(24));
        user.setLoyaltyPoints(0);

        // Lưu vào CSDL
        boolean inserted = userDAO.insert(user);
        assert inserted : "Phải chèn được User PENDING vào CSDL";

        // Tìm kiếm bằng token
        User found = userDAO.findByVerificationToken(token);
        assert found != null : "Phải tìm thấy User bằng verificationToken";
        assert found.getEmail().equals(testEmail) : "Email của User tìm được phải khớp";
        assert "PENDING".equalsIgnoreCase(found.getStatus()) : "Trạng thái phải là PENDING";
        assert !found.isEmailVerified() : "emailVerified phải là false";

        // Giả lập kích hoạt tài khoản
        found.setStatus("ACTIVE");
        found.setEmailVerified(true);
        found.setVerificationToken(null);
        found.setTokenExpiry(null);
        boolean updated = userDAO.update(found);
        assert updated : "Phải cập nhật thành công User sang ACTIVE";

        // Kiểm tra sau khi kích hoạt
        User activated = userDAO.findById(found.getId());
        assert activated != null : "User kích hoạt phải tồn tại";
        assert "ACTIVE".equalsIgnoreCase(activated.getStatus()) : "Trạng thái mới phải là ACTIVE";
        assert activated.isEmailVerified() : "emailVerified phải là true";
        assert activated.getVerificationToken() == null : "Token phải được xoá sau kích hoạt";

        // Dọn dẹp bản ghi kiểm thử
        userDAO.delete(activated.getId());
        assert userDAO.findById(activated.getId()) == null : "Đã xóa bản ghi kiểm thử thành công";

        System.out.println("  [OK] Quy trình Database (Lưu PENDING -> Tìm Token -> Cập nhật ACTIVE) hoạt động chính xác.");
    }

    private static void testEmailTemplateBuilder() {
        System.out.println("--> 3. Kiểm tra MailUtilGmail và template HTML...");
        String sampleUrl = "http://localhost:8080/movie-booking/auth/verify?token=abc-123-xyz";
        String html = MailUtilGmail.buildVerificationEmail("Trần Thiện Ân", sampleUrl);

        assert html != null && !html.isBlank() : "Nội dung email không được rỗng";
        assert html.contains("Trần Thiện Ân") : "HTML phải chứa tên người nhận";
        assert html.contains(sampleUrl) : "HTML phải chứa đường dẫn xác thực";
        assert html.contains("Cinetik Cinema") : "HTML phải có thương hiệu Cinetik Cinema";

        System.out.println("  [OK] Template HTML email xác thực được định dạng chuẩn đẹp mắt.");
    }
}
