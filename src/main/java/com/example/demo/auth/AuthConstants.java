package com.example.demo.auth;

import org.springframework.security.oauth2.core.OAuth2AccessToken;

public final class AuthConstants {

    public static final String BEARER_AUTHENTICATION_SCHEME = OAuth2AccessToken.TokenType.BEARER.getValue();

    private AuthConstants() {
    }
}
