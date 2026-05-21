package com.example.demo.user.profile.validation;

import com.example.demo.common.ErrorDetail;
import com.example.demo.common.ErrorField;
import com.example.demo.user.account.UserAccountConstraints;

enum UserProfileValidationError {
    NICKNAME_REQUIRED(ErrorField.NICKNAME, "Nickname is required."),
    NICKNAME_TOO_LONG(ErrorField.NICKNAME, UserAccountConstraints.NICKNAME_TOO_LONG_MESSAGE);

    private final ErrorField field;
    private final String message;

    UserProfileValidationError(ErrorField field, String message) {
        this.field = field;
        this.message = message;
    }

    ErrorDetail toDetail() {
        return new ErrorDetail(field, message);
    }
}
