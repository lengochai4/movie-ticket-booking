package com.cinema.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    public void testHashAndVerifyPassword() {
        String rawPassword = "mySecretPassword123";
        String hash = PasswordUtil.hashPassword(rawPassword);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$10$"));
        assertTrue(PasswordUtil.verifyPassword(rawPassword, hash));
        assertFalse(PasswordUtil.verifyPassword("wrongPassword", hash));
    }

    @Test
    public void testEmptyPasswordVerification() {
        assertFalse(PasswordUtil.verifyPassword("", "$2a$10$dummyhash"));
        assertFalse(PasswordUtil.verifyPassword(null, "$2a$10$dummyhash"));
        assertFalse(PasswordUtil.verifyPassword("test", null));
    }
}
