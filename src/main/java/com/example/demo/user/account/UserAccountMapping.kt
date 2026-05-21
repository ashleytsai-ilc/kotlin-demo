package com.example.demo.user.account

internal object UserAccountMapping {
    const val TABLE_NAME: String = "app_users"
    const val ACTIVE_USERNAME_KEY_UNIQUE_CONSTRAINT: String = "uk_app_users_active_username_key"
    const val ACTIVE_NICKNAME_KEY_UNIQUE_CONSTRAINT: String = "uk_app_users_active_nickname_key"
    const val USERNAME_COLUMN: String = "username"
    const val NICKNAME_COLUMN: String = "nickname"
    const val DELETED_AT_COLUMN: String = "deleted_at"
    const val ACTIVE_USERNAME_KEY_COLUMN: String = "active_username_key"
    const val ACTIVE_NICKNAME_KEY_COLUMN: String = "active_nickname_key"
    const val ID_LENGTH: Int = 26
    const val USERNAME_LENGTH: Int = UserAccountConstraints.USERNAME_MAX_LENGTH
    const val NICKNAME_LENGTH: Int = UserAccountConstraints.NICKNAME_MAX_LENGTH
}
