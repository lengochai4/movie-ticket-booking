package com.cinema.util;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Tiện ích gửi email qua Gmail SMTP Server (Port 465 - SMTPS)
 * Dựa trên cấu trúc Murach MailUtilGmail.
 */
public class MailUtilGmail {

    public static final String DEFAULT_GMAIL_USER = "thienantranit@gmail.com";
    public static final String DEFAULT_GMAIL_PASS = "bwie szbw phaa crer";

    private static String getUsername() {
        String envUser = System.getenv("GMAIL_USER");
        if (envUser != null && !envUser.isBlank()) {
            return envUser;
        }
        return System.getProperty("gmail.user", DEFAULT_GMAIL_USER);
    }

    private static String getPassword() {
        String envPass = System.getenv("GMAIL_PASSWORD");
        if (envPass != null && !envPass.isBlank()) {
            return envPass;
        }
        return System.getProperty("gmail.password", DEFAULT_GMAIL_PASS);
    }

    /**
     * Gửi email qua Gmail SMTPS (chuẩn Murach EmailList)
     */
    public static void sendMail(
            String to,
            String from,
            String subject,
            String body,
            boolean bodyIsHTML) throws MessagingException, UnsupportedEncodingException {

        // 1. Cấu hình Mail Session với giao thức SMTPS (Port 465)
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtps.host", "smtp.gmail.com");
        props.put("mail.smtps.port", "465");
        props.put("mail.smtps.auth", "true");
        props.put("mail.smtps.quitwait", "false");
        props.put("mail.smtps.ssl.enable", "true");
        props.put("mail.smtps.ssl.protocols", "TLSv1.2 TLSv1.3");

        Session session = Session.getInstance(props);
        session.setDebug(false);

        // 2. Tạo nội dung MimeMessage
        Message message = new MimeMessage(session);
        message.setSubject(subject);
        if (bodyIsHTML) {
            message.setContent(body, "text/html; charset=UTF-8");
        } else {
            message.setText(body);
        }

        // 3. Thiết lập địa chỉ người gửi và người nhận
        String senderEmail = (from != null && !from.isBlank()) ? from : getUsername();
        Address fromAddress = new InternetAddress(senderEmail, "Cinetik Cinema", "UTF-8");
        Address toAddress = new InternetAddress(to);
        message.setFrom(fromAddress);
        message.setRecipient(Message.RecipientType.TO, toAddress);

        // 4. Kết nối và gửi email qua SMTPS Transport
        Transport transport = session.getTransport();
        try {
            transport.connect("smtp.gmail.com", getUsername(), getPassword());
            transport.sendMessage(message, message.getAllRecipients());
            System.out.println("[MailUtilGmail] Đã gửi thành công email tới: " + to);
        } finally {
            transport.close();
        }
    }

    /**
     * Overload rút gọn sử dụng email mặc định làm người gửi
     */
    public static void sendMail(
            String to,
            String subject,
            String body,
            boolean bodyIsHTML) throws MessagingException, UnsupportedEncodingException {
        sendMail(to, getUsername(), subject, body, bodyIsHTML);
    }

    /**
     * Tạo nội dung email HTML xác nhận tài khoản chuyên nghiệp
     */
    public static String buildVerificationEmail(String fullName, String verifyUrl) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset='UTF-8'></head>"
                + "<body style='margin:0;padding:0;background-color:#0d0d12;font-family:Arial,Helvetica,sans-serif;color:#e4e4e7;'>"
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#0d0d12;padding:40px 10px;'>"
                + "  <tr><td align='center'>"
                + "    <table width='600' cellpadding='0' cellspacing='0' style='max-width:600px;background:#18181b;border:1px solid #27272a;border-radius:12px;overflow:hidden;box-shadow:0 10px 30px rgba(0,0,0,0.5);'>"
                + "      <tr>"
                + "        <td style='padding:30px 40px;background:linear-gradient(135deg,#e11d48,#be123c);text-align:center;'>"
                + "          <h1 style='margin:0;color:#ffffff;font-size:26px;letter-spacing:1px;text-transform:uppercase;'>🎬 Cinetik Cinema</h1>"
                + "          <p style='margin:8px 0 0;color:#ffe4e6;font-size:14px;'>Hệ Thống Đặt Vé Xem Phim Hiện Đại</p>"
                + "        </td>"
                + "      </tr>"
                + "      <tr>"
                + "        <td style='padding:40px;'>"
                + "          <h2 style='margin:0 0 16px;color:#ffffff;font-size:20px;'>Xin chào " + escapeHtml(fullName) + ",</h2>"
                + "          <p style='margin:0 0 16px;color:#a1a1aa;line-height:1.6;font-size:15px;'>"
                + "            Cảm ơn bạn đã đăng ký tài khoản thành viên tại <strong>Cinetik Cinema</strong>."
                + "            Để hoàn tất quá trình đăng ký và kích hoạt tài khoản của bạn, vui lòng nhấn vào nút xác nhận bên dưới:"
                + "          </p>"
                + "          <div style='text-align:center;margin:32px 0;'>"
                + "            <a href='" + verifyUrl + "' style='display:inline-block;padding:14px 32px;background:linear-gradient(135deg,#e11d48,#be123c);color:#ffffff;text-decoration:none;font-weight:bold;font-size:16px;border-radius:8px;box-shadow:0 4px 14px rgba(225,29,72,0.4);letter-spacing:0.5px;'>XÁC NHẬN TÀI KHOẢN</a>"
                + "          </div>"
                + "          <p style='margin:0 0 12px;color:#a1a1aa;line-height:1.6;font-size:14px;'>"
                + "            Hoặc bạn có thể sao chép và dán liên kết sau vào trình duyệt:"
                + "          </p>"
                + "          <div style='background:#27272a;padding:12px;border-radius:6px;word-break:break-all;font-size:13px;color:#f43f5e;margin-bottom:20px;font-family:monospace;'>"
                + "            " + verifyUrl
                + "          </div>"
                + "          <p style='margin:0;color:#71717a;font-size:13px;line-height:1.5;'>"
                + "            ⏰ <em>Lưu ý: Liên kết này có thời hạn sử dụng trong vòng <strong>24 giờ</strong>. Nếu bạn không thực hiện đăng ký tài khoản này, vui lòng bỏ qua email.</em>"
                + "          </p>"
                + "        </td>"
                + "      </tr>"
                + "      <tr>"
                + "        <td style='padding:20px 40px;background:#121215;border-top:1px solid #27272a;text-align:center;color:#71717a;font-size:12px;'>"
                + "          © " + java.time.Year.now().getValue() + " Cinetik Cinema. Mọi quyền được bảo lưu."
                + "        </td>"
                + "      </tr>"
                + "    </table>"
                + "  </td></tr>"
                + "</table>"
                + "</body></html>";
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
