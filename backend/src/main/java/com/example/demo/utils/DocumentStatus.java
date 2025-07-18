package com.example.demo.utils;

public enum DocumentStatus {

    APPROVED(1),
    SIGNED(2),
    TO_SIGN(3),
    TO_APPROVE(4),
    TO_VIEW(5),
    VIEWED(6),
    DELCLIEND(7);

    private final int code;

    DocumentStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
