package com.example.demo.user.auth.validation

@JvmRecord
data class ValidatedRegistration(
    val username: String?,
    val nickname: String?,
    val password: String?,
)
