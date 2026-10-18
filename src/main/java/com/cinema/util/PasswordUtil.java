package com.cinema.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordUtil - Tiện ích băm và xác minh mật khẩu bằng BCrypt.
 * Cột lưu trữ trong DB là passwordHash (VARCHAR 255).
 */
public class PasswordUtil {

    private static final int BCRYPT_ROUNDS = 10;

    /**
     * Băm mật khẩu thuần văn bản bằng BCrypt.
     *
     * @param plain Mật khẩu gốc chưa băm
     * @return Chuỗi băm BCrypt (bắt đầu bằng $2a$)
     */
    public static String hashPassword(String plain) {
        if (plain == null || plain.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }
        return BCrypt.hashpw(plain, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Xác minh mật khẩu thuần văn bản khớp với chuỗi băm đã lưu.
     *
     * @param plain Mật khẩu gốc người dùng nhập
     * @param hash  Chuỗi băm lấy từ cột passwordHash trong DB
     * @return true nếu khớp, false nếu không
     */
    public static boolean verifyPassword(String plain, String hash) {
        if (plain == null || hash == null || plain.isEmpty() || hash.isEmpty()) {
            return false;
        }
        return BCrypt.checkpw(plain, hash);
    }
}
