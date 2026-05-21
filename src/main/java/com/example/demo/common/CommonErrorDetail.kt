package com.example.demo.common

enum class CommonErrorDetail(private val field: ErrorField, private val message: String) {
    INVALID_JSON_BODY(ErrorField.BODY, "Request body must be valid JSON."),
    VALID_BEARER_TOKEN_REQUIRED(ErrorField.AUTHORIZATION, "A valid bearer token is required.");

    fun toDetail(): ErrorDetail = ErrorDetail(field, message)
}
