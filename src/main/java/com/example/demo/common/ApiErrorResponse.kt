package com.example.demo.common

@JvmRecord
data class ApiErrorResponse(
    val code: String,
    val message: String,
    val details: Any?,
)
