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

        // Nếu chưa cấu hình tài khoản gửi riêng qua biến môi trường, sử dụng MailUtilGmail (Port 465 SMTPS)
        if (SMTP_USER.isBlank() || SMTP_PASS.isBlank()) {
            try {
                MailUtilGmail.sendMail(to, subject, body, true);
                return;
            } catch (Exception e) {
                System.err.println("[MailUtil] Không thể gửi email qua MailUtilGmail: " + e.getMessage());
                return;
            }
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
