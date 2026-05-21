package com.example.demo.common

@JvmRecord
internal data class ApiErrorDefinition(
    val code: String,
    val message: String,
)
