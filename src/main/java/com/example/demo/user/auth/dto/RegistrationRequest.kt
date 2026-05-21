package com.example.demo.user.auth.dto

@JvmRecord
data class RegistrationRequest(
    val username: String?,
    val nickname: String?,
    val password: String?,
)
