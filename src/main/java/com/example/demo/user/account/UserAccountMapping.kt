package com.example.demo.user.account;

final class UserAccountMapping {

    static final String TABLE_NAME = "app_users";
    static final String ACTIVE_USERNAME_KEY_UNIQUE_CONSTRAINT = "uk_app_users_active_username_key";
    static final String ACTIVE_NICKNAME_KEY_UNIQUE_CONSTRAINT = "uk_app_users_active_nickname_key";
    static final String USERNAME_COLUMN = "username";
    static final String NICKNAME_COLUMN = "nickname";
    static final String DELETED_AT_COLUMN = "deleted_at";
    static final String ACTIVE_USERNAME_KEY_COLUMN = "active_username_key";
    static final String ACTIVE_NICKNAME_KEY_COLUMN = "active_nickname_key";
    static final int ID_LENGTH = 26;
    static final int USERNAME_LENGTH = UserAccountConstraints.USERNAME_MAX_LENGTH;
    static final int NICKNAME_LENGTH = UserAccountConstraints.NICKNAME_MAX_LENGTH;

    private UserAccountMapping() {
    }
}
