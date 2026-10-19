package com.cinema.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * Tiện ích mã hóa và xác thực mật khẩu an toàn.
 */
public class PasswordUtil {

    private static final Pattern STRONG_PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,}$");
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Băm mật khẩu theo thuật toán SHA-256.
     * Định dạng trả về: saltHex:hashHex
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null) {
            return null;
        }
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltHex = bytesToHex(salt);
        String hashHex = computeSha256(saltHex + plainPassword);
        return saltHex + ":" + hashHex;
    }

    /**
     * Kiểm tra mật khẩu người dùng nhập có khớp với chuỗi đã lưu trong CSDL hay không.
     * Hỗ trợ định dạng salt:hash lẫn kiểm tra mật khẩu mẫu cũ.
     */
    public static boolean verify(String plainPassword, String storedPassword) {
        if (plainPassword == null || storedPassword == null) {
            return false;
        }
        // Trường hợp mật khẩu có salt:hash
        if (storedPassword.contains(":")) {
            String[] parts = storedPassword.split(":", 2);
            String saltHex = parts[0];
            String expectedHashHex = parts[1];
            String actualHashHex = computeSha256(saltHex + plainPassword);
            return MessageDigest.isEqual(
                    actualHashHex.getBytes(StandardCharsets.UTF_8),
                    expectedHashHex.getBytes(StandardCharsets.UTF_8)
            );
        }

        // Trường hợp mật khẩu mẫu văn bản thuần để kiểm thử nhanh
        if (plainPassword.equals(storedPassword)) {
            return true;
        }

        // Trường hợp SHA-256 thô
        String rawSha256 = computeSha256(plainPassword);
        return MessageDigest.isEqual(
                rawSha256.getBytes(StandardCharsets.UTF_8),
                storedPassword.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Kiểm tra tiêu chuẩn bảo mật: tối thiểu 8 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt.
     */
    public static boolean isStrong(String password) {
        if (password == null) {
            return false;
        }
        return STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }

    private static String computeSha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tính toán SHA-256: " + e.getMessage(), e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
