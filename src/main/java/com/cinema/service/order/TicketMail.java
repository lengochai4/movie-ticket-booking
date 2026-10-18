package com.cinema.service.order;

import com.cinema.dto.OrderView;

import java.lang.reflect.Method;

/**
 * Gửi mail vé nếu Ân đã có com.cinema.util.MailUtil.
 * Không tạo class đó. Chưa có class hoặc chưa đúng chữ ký thì bỏ qua, đơn vé vẫn lưu.
 *
 * Chữ ký Ân nên cung cấp:
 * public static void send(String to, String subject, String body)
 */
public final class TicketMail {

    private static final String MAIL_UTIL = "com.cinema.util.MailUtil";

    private TicketMail() {
    }

    public static void sendTicket(OrderView order) {
        if (order == null) {
            return;
        }
        send(order.getEmail(), "Vé điện tử Cinetik " + safe(order.getOrderCode()), ticketBody(order));
    }

    public static void sendCancelled(OrderView order) {
        if (order == null) {
            return;
        }
        String subject = "Đã hủy vé Cinetik " + safe(order.getOrderCode());
        String body = "<p>Đơn vé <strong>" + escape(order.getOrderCode()) + "</strong> đã được hủy.</p>"
                + "<p>Phim: " + escape(order.getMovieTitle()) + "</p>"
                + "<p>Suất chiếu: " + escape(order.getStartTime()) + " " + escape(order.getShowDate()) + "</p>"
                + "<p>Điểm thưởng đã dùng cho đơn này được hoàn lại.</p>";
        send(order.getEmail(), subject, body);
    }

    public static void send(String to, String subject, String htmlBody) {
        if (to == null || to.isBlank() || !to.contains("@")) {
            return;
        }
        try {
            Class<?> mailUtil = Class.forName(MAIL_UTIL);
            Method method = findSend(mailUtil);
            if (method == null) {
                System.err.println("[TicketMail] MailUtil chưa có send(String, String, String). Bỏ qua email.");
                return;
            }
            method.invoke(null, to.trim(), subject, htmlBody);
        } catch (ClassNotFoundException e) {
            System.err.println("[TicketMail] Chưa có MailUtil. Bỏ qua email.");
        } catch (Exception e) {
            System.err.println("[TicketMail] Không gửi được email: " + e.getMessage());
        }
    }

    private static Method findSend(Class<?> mailUtil) {
        String[] names = {"send", "sendMail", "sendEmail"};
        for (String name : names) {
            try {
                Method method = mailUtil.getMethod(name, String.class, String.class, String.class);
                if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                    return method;
                }
            } catch (NoSuchMethodException ignored) {
                // thử tên khác
            }
        }
        return null;
    }

    private static String ticketBody(OrderView order) {
        return "<h2>Vé điện tử Cinetik</h2>"
                + "<p>Mã vé: <strong>" + escape(order.getOrderCode()) + "</strong></p>"
                + "<p>Phim: " + escape(order.getMovieTitle()) + "</p>"
                + "<p>Rạp: " + escape(order.getCinemaName()) + "</p>"
                + "<p>Địa chỉ: " + escape(order.getCinemaAddress()) + "</p>"
                + "<p>Phòng: " + escape(order.getRoomName()) + "</p>"
                + "<p>Suất chiếu: " + escape(order.getStartTime()) + " ngày " + escape(order.getShowDate()) + "</p>"
                + "<p>Ghế: " + escape(order.getSeats()) + "</p>"
                + "<p>Tổng tiền: " + (order.getTotalPrice() == null ? "0" : order.getTotalPrice()) + "đ</p>"
                + "<p>Mã QR soát vé:<br><img src=\"" + escape(order.getQrImageUrl()) + "\" alt=\"QR\"></p>";
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
