package com.example.demo.user.auth.dto

@JvmRecord
data class LoginRequest(
    val username: String?,
    val password: String?,
)
