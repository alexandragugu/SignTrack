package com.example.demo.utils;

public enum UserDetails {
    USERNAME("username"),
    EMAIL("email"),
    FULLNAME ("fullname");

    private final String value;

    UserDetails(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
