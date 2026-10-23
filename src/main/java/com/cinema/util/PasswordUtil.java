package com.cinema.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.regex.Pattern;

public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern STRONG_PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,}$");

    private PasswordUtil() {
        // Utility class không cho khởi tạo
    }
    /**
     * Băm mật khẩu người dùng nhập vào để lưu vào cơ sở dữ liệu.
     * @param plainPassword Mật khẩu gốc chưa mã hóa
     * @return Chuỗi "salt:hash" hoặc null nếu input rỗng
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            return null;
        }
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltHex = HexFormat.of().formatHex(salt);

        String hashHex = computeSha256(saltHex + plainPassword);
        return saltHex + ":" + hashHex;
    }

    /**
     * Xác minh mật khẩu người dùng nhập có khớp với mật khẩu đã lưu trong database không.
     * @param plainPassword Mật khẩu người dùng nhập từ form
     * @param storedPassword Mật khẩu lưu trong DB (dạng salt:hash hoặc mật khẩu cũ/mẫu test)
     * @return true nếu khớp, ngược lại false
     */
    public static boolean verify(String plainPassword, String storedPassword) {
        if (plainPassword == null || storedPassword == null) {
            return false;
        }

        // Trường hợp 1: Mật khẩu chuẩn salt:hash
        if (storedPassword.contains(":")) {
            String[] parts = storedPassword.split(":", 2);
            String saltHex = parts[0];
            String expectedHash = parts[1];
            String actualHash = computeSha256(saltHex + plainPassword);
            return MessageDigest.isEqual(
                    actualHash.getBytes(StandardCharsets.UTF_8),
                    expectedHash.getBytes(StandardCharsets.UTF_8)
            );
        }

        // Trường hợp 2: Khớp trực tiếp văn bản thường (dành cho dữ liệu mẫu test nhanh)
        if (plainPassword.equals(storedPassword)) {
            return true;
        }

        // Trường hợp 3: Khớp mã băm SHA-256 không có muối
        String rawHash = computeSha256(plainPassword);
        return MessageDigest.isEqual(
                rawHash.getBytes(StandardCharsets.UTF_8),
                storedPassword.getBytes(StandardCharsets.UTF_8)
        );
    }


    public static boolean isStrong(String password) {
        return password != null && STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }

    private static String computeSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Lỗi thuật toán SHA-256 không khả dụng", e);
        }
    }
}
