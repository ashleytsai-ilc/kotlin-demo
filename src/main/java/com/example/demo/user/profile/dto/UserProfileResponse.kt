package com.example.demo.user.profile.dto

import java.time.Instant

class UserProfileResponse(
    val id: String,
    val username: String,
    nickname: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val nickname: String = nickname ?: EMPTY_NICKNAME

    companion object {
        private const val EMPTY_NICKNAME = ""
    }
}
