package com.cinema.order;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Tạo URL ảnh QR cho vé điện tử. Không thêm thư viện mới vào pom.xml.
 * Trang ticket-detail.jsp đã dùng cùng dịch vụ tạo mã QR.
 */
public final class QrCodeUtil {

    private QrCodeUtil() {
    }

    public static String buildQrImageUrl(String payload) {
        String data = payload == null ? "" : payload.trim();
        String encoded = URLEncoder.encode(data, StandardCharsets.UTF_8);
        return "https://api.qrserver.com/v1/create-qr-code/?size=190x190&color=110e0c&data=" + encoded;
    }
}
