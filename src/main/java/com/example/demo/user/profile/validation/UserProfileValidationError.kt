package com.example.demo.user.profile.validation

import com.example.demo.common.ErrorDetail
import com.example.demo.common.ErrorField
import com.example.demo.user.account.UserAccountConstraints

internal enum class UserProfileValidationError(private val field: ErrorField, private val message: String) {
    NICKNAME_REQUIRED(ErrorField.NICKNAME, "Nickname is required."),
    NICKNAME_TOO_LONG(ErrorField.NICKNAME, UserAccountConstraints.NICKNAME_TOO_LONG_MESSAGE);

    fun toDetail(): ErrorDetail = ErrorDetail(field, message)
}
