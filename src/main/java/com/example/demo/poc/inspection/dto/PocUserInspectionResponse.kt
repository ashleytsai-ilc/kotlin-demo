package com.example.demo.poc.inspection.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

@JvmRecord
data class PocUserInspectionResponse(
    val id: String,
    val username: String,
    @get:JsonInclude(JsonInclude.Include.ALWAYS)
    val nickname: String?,
    val passwordHash: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    @get:JsonInclude(JsonInclude.Include.ALWAYS)
    val deletedAt: Instant?,
    @get:JsonInclude(JsonInclude.Include.ALWAYS)
    val activeUsernameKey: String?,
    @get:JsonInclude(JsonInclude.Include.ALWAYS)
    val activeNicknameKey: String?
)
