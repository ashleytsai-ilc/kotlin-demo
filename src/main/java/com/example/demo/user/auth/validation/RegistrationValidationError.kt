package com.example.demo.user.auth.validation;

import com.example.demo.common.ErrorDetail;
import com.example.demo.common.ErrorField;
import com.example.demo.user.account.UserAccountConstraints;

enum RegistrationValidationError {
    USERNAME_REQUIRED(ErrorField.USERNAME, "Username is required."),
    USERNAME_INVALID_CHARACTERS(ErrorField.USERNAME, "Username may contain only letters, digits, and underscore."),
    USERNAME_TOO_SHORT(ErrorField.USERNAME, RegistrationValidationRules.USERNAME_TOO_SHORT_MESSAGE),
    USERNAME_TOO_LONG(ErrorField.USERNAME, UserAccountConstraints.USERNAME_TOO_LONG_MESSAGE),
    NICKNAME_TOO_LONG(ErrorField.NICKNAME, UserAccountConstraints.NICKNAME_TOO_LONG_MESSAGE),
    PASSWORD_REQUIRED(ErrorField.PASSWORD, "Password is required."),
    PASSWORD_TOO_SHORT(ErrorField.PASSWORD, RegistrationValidationRules.PASSWORD_TOO_SHORT_MESSAGE),
    PASSWORD_WEAK(ErrorField.PASSWORD, "Password must include uppercase, lowercase, digit, and special symbol.");

    private final ErrorField field;
    private final String message;

    RegistrationValidationError(ErrorField field, String message) {
        this.field = field;
        this.message = message;
    }

    ErrorDetail toDetail() {
        return new ErrorDetail(field, message);
    }
}
