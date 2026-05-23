package com.example.demo.user.auth.validation

import com.example.demo.common.ErrorDetail
import com.example.demo.common.ErrorField
import com.example.demo.user.account.UserAccountConstraints

internal enum class RegistrationValidationError(private val field: ErrorField, private val message: String) {
    USERNAME_REQUIRED(ErrorField.USERNAME, "Username is required."),
    USERNAME_INVALID(
        ErrorField.USERNAME,
        "Username must be ${RegistrationValidationRules.USERNAME_MIN_LENGTH} to ${UserAccountConstraints.USERNAME_MAX_LENGTH} characters and contain only letters, digits, and underscore.",
    ),
    NICKNAME_INVALID(
        ErrorField.NICKNAME,
        "Nickname must not be blank, contain whitespace, or exceed ${UserAccountConstraints.NICKNAME_MAX_LENGTH} characters.",
    ),
    PASSWORD_REQUIRED(ErrorField.PASSWORD, "Password is required."),
    PASSWORD_TOO_SHORT(
        ErrorField.PASSWORD,
        "Password must be at least ${RegistrationValidationRules.PASSWORD_MIN_LENGTH} characters.",
    ),
    PASSWORD_WEAK(ErrorField.PASSWORD, "Password must include uppercase, lowercase, digit, and special symbol.");

    fun toDetail(): ErrorDetail = ErrorDetail(field, message)
}
