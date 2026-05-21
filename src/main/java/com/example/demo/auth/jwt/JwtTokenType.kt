package com.example.demo.auth.jwt

import org.springframework.security.oauth2.jwt.JwtClaimNames

enum class JwtTokenType(private val value: String) {
    ACCESS("access"),
    REFRESH("refresh");

    fun value(): String = value

    companion object {
        const val CLAIM: String = "token_type"
        const val ID_CLAIM: String = JwtClaimNames.JTI
    }
}
