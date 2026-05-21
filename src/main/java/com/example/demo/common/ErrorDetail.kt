package com.example.demo.common

@JvmRecord
data class ErrorDetail(
    val field: String,
    val message: String,
) {
    constructor(field: ErrorField, message: String) : this(field.value(), message)
}
