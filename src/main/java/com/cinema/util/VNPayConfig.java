package com.cinema.util;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class VNPayConfig {

    public static final String VNP_PAY_URL = System.getProperty("VNP_PAY_URL", System.getenv().getOrDefault("VNP_PAY_URL", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"));
    public static final String VNP_TMN_CODE = System.getProperty("VNP_TMN_CODE", System.getenv().getOrDefault("VNP_TMN_CODE", "NWGUE67R"));
    public static final String VNP_HASH_SECRET = System.getProperty("VNP_HASH_SECRET", System.getenv().getOrDefault("VNP_HASH_SECRET", "ETJHJUGEBCXYVPFFBVMIFRCVNJCNIFMO"));

    /**
     * Mã hóa dữ liệu bằng thuật toán HMAC-SHA512 trả về chuỗi Hex viết thường.
     */
    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException("Key hoặc data không được null");
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Lấy địa chỉ IP của Client gửi request (ưu tiên header X-FORWARDED-FOR).
     */
    public static String getIpAddress(HttpServletRequest req) {
        String ipAddress;
        try {
            ipAddress = req.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null || ipAddress.trim().isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = req.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.trim().isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = req.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.trim().isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = req.getRemoteAddr();
            }
            if (ipAddress != null && ipAddress.contains(",")) {
                ipAddress = ipAddress.split(",")[0].trim();
            }
            if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
                ipAddress = "127.0.0.1";
            }
        } catch (Exception e) {
            ipAddress = "127.0.0.1";
        }
        return ipAddress;
    }

    /**
     * Sinh chuỗi số ngẫu nhiên có độ dài len.
     */
    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
