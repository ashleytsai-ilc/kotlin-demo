package com.example.demo.auth.revocation

object RevokedTokenMapping {
    const val TABLE_NAME: String = "revoked_tokens"
    const val TOKEN_ID_COLUMN: String = "token_id"
    const val TOKEN_ID_LENGTH: Int = 36
    const val USER_ID_COLUMN: String = "user_id"
    const val USER_ID_LENGTH: Int = 26
    const val TOKEN_TYPE_COLUMN: String = "token_type"
    const val TOKEN_TYPE_LENGTH: Int = 16
    const val EXPIRES_AT_COLUMN: String = "expires_at"
    const val REVOKED_AT_COLUMN: String = "revoked_at"
    const val TOKEN_ID_UNIQUE_CONSTRAINT: String = "uk_revoked_tokens_token_id"
}
