package com.example.demo.auth.jwt;

import org.springframework.security.oauth2.jwt.JwtClaimNames;

public enum JwtTokenType {
    ACCESS("access"),
    REFRESH("refresh");

    public static final String CLAIM = "token_type";
    public static final String ID_CLAIM = JwtClaimNames.JTI;

    private final String value;

    JwtTokenType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
