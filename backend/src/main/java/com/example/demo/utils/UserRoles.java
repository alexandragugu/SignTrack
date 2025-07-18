package com.example.demo.utils;

public enum UserRoles {
    ADMIN("Admin"),
    USER("User");

    private final String value;

    UserRoles(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
