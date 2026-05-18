package com.example.demo.user.auth.validation;

public final class RegistrationValidationRules {

    public static final int USERNAME_MIN_LENGTH = 8;
    public static final int PASSWORD_MIN_LENGTH = 8;

    static final String USERNAME_TOO_SHORT_MESSAGE =
            "Username must be at least " + USERNAME_MIN_LENGTH + " characters.";
    static final String PASSWORD_TOO_SHORT_MESSAGE =
            "Password must be at least " + PASSWORD_MIN_LENGTH + " characters.";

    private RegistrationValidationRules() {
    }
}
