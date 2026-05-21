package com.example.demo.auth.jwt

import java.time.Instant

@JvmRecord
data class JwtTokenClaims(
    val tokenId: String,
    val subject: String?,
    val tokenType: JwtTokenType,
    val issuedAt: Instant?,
    val expiresAt: Instant?,
)
