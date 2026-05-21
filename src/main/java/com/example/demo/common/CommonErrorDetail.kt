package com.example.demo.common;

public enum CommonErrorDetail {
    INVALID_JSON_BODY(ErrorField.BODY, "Request body must be valid JSON."),
    VALID_BEARER_TOKEN_REQUIRED(ErrorField.AUTHORIZATION, "A valid bearer token is required.");

    private final ErrorField field;
    private final String message;

    CommonErrorDetail(ErrorField field, String message) {
        this.field = field;
        this.message = message;
    }

    public ErrorDetail toDetail() {
        return new ErrorDetail(field, message);
    }
}
