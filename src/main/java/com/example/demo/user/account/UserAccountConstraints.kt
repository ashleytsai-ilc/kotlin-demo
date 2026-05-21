package com.example.demo.user.account;

public final class UserAccountConstraints {

    public static final int USERNAME_MAX_LENGTH = 15;
    public static final int NICKNAME_MAX_LENGTH = 30;

    public static final String USERNAME_TOO_LONG_MESSAGE =
            "Username must be at most " + USERNAME_MAX_LENGTH + " characters.";
    public static final String NICKNAME_TOO_LONG_MESSAGE =
            "Nickname must be at most " + NICKNAME_MAX_LENGTH + " characters.";

    private UserAccountConstraints() {
    }
}
