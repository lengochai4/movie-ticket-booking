package com.cinema.model.enums;

public enum Role {
    USER(0),
    ADMIN(1);

    private final int value;

    Role(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Role fromInt(int value) {
        for (Role role : Role.values()) {
            if (role.value == value) {
                return role;
            }
        }
        return USER;
    }
}
