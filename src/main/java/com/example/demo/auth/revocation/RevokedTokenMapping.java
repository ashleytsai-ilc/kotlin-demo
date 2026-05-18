package com.example.demo.auth.revocation;

public final class RevokedTokenMapping {

    public static final String TABLE_NAME = "revoked_tokens";
    public static final String TOKEN_ID_COLUMN = "token_id";
    public static final int TOKEN_ID_LENGTH = 36;
    public static final String USER_ID_COLUMN = "user_id";
    public static final int USER_ID_LENGTH = 26;
    public static final String TOKEN_TYPE_COLUMN = "token_type";
    public static final int TOKEN_TYPE_LENGTH = 16;
    public static final String EXPIRES_AT_COLUMN = "expires_at";
    public static final String REVOKED_AT_COLUMN = "revoked_at";
    public static final String TOKEN_ID_UNIQUE_CONSTRAINT = "uk_revoked_tokens_token_id";

    private RevokedTokenMapping() {
    }
}
