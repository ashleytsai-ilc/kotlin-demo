package com.example.demo.common;

public enum ErrorField {
    AUTHORIZATION("authorization"),
    BODY("body"),
    CREDENTIALS("credentials"),
    NICKNAME("nickname"),
    PASSWORD("password"),
    REFRESH_TOKEN("refresh_token"),
    USER("user"),
    USERNAME("username");

    private final String value;

    ErrorField(String value) {
        this.value = value;
    }

    String value() {
        return value;
    }
}
