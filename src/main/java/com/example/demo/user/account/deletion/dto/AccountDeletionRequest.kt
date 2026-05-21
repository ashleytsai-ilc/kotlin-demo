package com.example.demo.user.account.deletion.dto

@JvmRecord
data class AccountDeletionRequest(
    val password: String?,
    val refreshToken: String?,
)
