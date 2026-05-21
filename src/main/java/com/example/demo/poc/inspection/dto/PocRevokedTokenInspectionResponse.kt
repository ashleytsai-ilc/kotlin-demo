package com.example.demo.poc.inspection.dto

import java.time.Instant

@JvmRecord
data class PocRevokedTokenInspectionResponse(
    val tokenId: String,
    val userId: String,
    val tokenType: String,
    val expiresAt: Instant,
    val revokedAt: Instant
)
