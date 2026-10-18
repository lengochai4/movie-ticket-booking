package com.cinema.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * MailUtil - Tiện ích gửi email HTML qua SMTP.
 * Cấu hình SMTP được load từ mail.properties trên classpath.
 * Các thành viên trong nhóm gọi MailUtil.send(...) để gửi email.
 */
public class MailUtil {

    private static final String MAIL_PROPERTIES_FILE = "mail.properties";
    private static Properties mailConfig;

    static {
        mailConfig = new Properties();
        try (InputStream is = MailUtil.class.getClassLoader().getResourceAsStream(MAIL_PROPERTIES_FILE)) {
            if (is != null) {
                mailConfig.load(is);
            }
        } catch (IOException e) {
            System.err.println("[MailUtil] Không thể load " + MAIL_PROPERTIES_FILE + ": " + e.getMessage());
        }
    }

    /**
     * Gửi email HTML qua SMTP.
     *
     * @param to          Địa chỉ email người nhận
     * @param subject     Tiêu đề email
     * @param htmlContent Nội dung HTML của email
     * @return true nếu gửi thành công, false nếu thất bại
     */
    public static boolean send(String to, String subject, String htmlContent) {
        if (to == null || to.isEmpty() || subject == null || htmlContent == null) {
            System.err.println("[MailUtil] Tham số không hợp lệ: to=" + to);
            return false;
        }

        String smtpHost = mailConfig.getProperty("mail.smtp.host", "smtp.gmail.com");
        String smtpPort = mailConfig.getProperty("mail.smtp.port", "587");
        final String fromEmail = mailConfig.getProperty("mail.from.email", "");
        final String fromPassword = mailConfig.getProperty("mail.from.password", "");
        String fromName = mailConfig.getProperty("mail.from.name", "Cinema N3");

        if (fromEmail.isEmpty()) {
            System.err.println("[MailUtil] Chưa cấu hình mail.from.email trong " + MAIL_PROPERTIES_FILE);
            return false;
        }

        Properties sessionProps = new Properties();
        sessionProps.put("mail.smtp.auth", "true");
        sessionProps.put("mail.smtp.starttls.enable", "true");
        sessionProps.put("mail.smtp.host", smtpHost);
        sessionProps.put("mail.smtp.port", smtpPort);
        sessionProps.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(sessionProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, fromPassword);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, fromName, "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject, "UTF-8");
            message.setContent(htmlContent, "text/html; charset=UTF-8");
            Transport.send(message);
            System.out.println("[MailUtil] Email đã gửi thành công tới: " + to);
            return true;
        } catch (Exception e) {
            System.err.println("[MailUtil] Lỗi khi gửi email tới " + to + ": " + e.getMessage());
            return false;
        }
    }
}
