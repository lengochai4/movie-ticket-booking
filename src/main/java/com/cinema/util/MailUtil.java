package com.cinema.util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Tiện ích gửi email qua Jakarta Mail (Angus Mail).
 * Cung cấp hàm send(to, subject, body) mà TicketMail (module của Như) và hệ thống cần.
 */
public class MailUtil {

    private static final String SMTP_HOST = System.getProperty("SMTP_HOST", System.getenv().getOrDefault("SMTP_HOST", "smtp.gmail.com"));
    private static final String SMTP_PORT = System.getProperty("SMTP_PORT", System.getenv().getOrDefault("SMTP_PORT", "587"));
    private static final String SMTP_USER = System.getProperty("SMTP_USER", System.getenv().getOrDefault("SMTP_USER", ""));
    private static final String SMTP_PASS = System.getProperty("SMTP_PASSWORD", System.getenv().getOrDefault("SMTP_PASSWORD", ""));

    public static void send(String to, String subject, String body) {
        if (to == null || to.trim().isEmpty() || !to.contains("@")) {
            System.err.println("[MailUtil] Địa chỉ email nhận không hợp lệ: " + to);
            return;
        }

        // Nếu chưa cấu hình tài khoản gửi thực tế, in log giả lập an toàn
        if (SMTP_USER.isBlank() || SMTP_PASS.isBlank()) {
            System.out.println("==================================================================");
            System.out.println("📧 [MOCK EMAIL] Gửi email thành công!");
            System.out.println("   Người nhận: " + to);
            System.out.println("   Tiêu đề: " + subject);
            System.out.println("   Nội dung: " + (body != null && body.length() > 150 ? body.substring(0, 150) + "..." : body));
            System.out.println("==================================================================");
            return;
        }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER, "Cinetik Cinema", StandardCharsets.UTF_8.name()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to.trim()));
            message.setSubject(subject, StandardCharsets.UTF_8.name());
            message.setContent(body, "text/html; charset=UTF-8");

            Transport.send(message);
            System.out.println("[MailUtil] Đã gửi email thực tế thành công tới: " + to);
        } catch (Exception e) {
            System.err.println("[MailUtil] Lỗi gửi email: " + e.getMessage());
        }
    }

    public static void sendMail(String to, String subject, String body) {
        send(to, subject, body);
    }

    public static void sendEmail(String to, String subject, String body) {
        send(to, subject, body);
    }
}
