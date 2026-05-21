package com.example.demo.user.account

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = UserAccountMapping.TABLE_NAME,
    uniqueConstraints = [
        UniqueConstraint(
            name = UserAccountMapping.ACTIVE_USERNAME_KEY_UNIQUE_CONSTRAINT,
            columnNames = [UserAccountMapping.ACTIVE_USERNAME_KEY_COLUMN],
        ), UniqueConstraint(
            name = UserAccountMapping.ACTIVE_NICKNAME_KEY_UNIQUE_CONSTRAINT,
            columnNames = [UserAccountMapping.ACTIVE_NICKNAME_KEY_COLUMN],
        ),
    ],
)
class UserAccount {
    @Id
    @Column(length = UserAccountMapping.ID_LENGTH, nullable = false, updatable = false)
    var id: String? = null
        private set

    @Column(nullable = false, length = UserAccountMapping.USERNAME_LENGTH)
    var username: String? = null
        private set

    @Column(length = UserAccountMapping.NICKNAME_LENGTH)
    var nickname: String? = null
        private set

    @Column(nullable = false)
    var passwordHash: String? = null
        private set

    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null
        private set

    @Column(nullable = false)
    var updatedAt: Instant? = null
        private set

    @Column(name = UserAccountMapping.DELETED_AT_COLUMN)
    var deletedAt: Instant? = null
        private set

    @Column(name = UserAccountMapping.ACTIVE_USERNAME_KEY_COLUMN, length = UserAccountMapping.USERNAME_LENGTH)
    var activeUsernameKey: String? = null
        private set

    @Column(name = UserAccountMapping.ACTIVE_NICKNAME_KEY_COLUMN, length = UserAccountMapping.NICKNAME_LENGTH)
    var activeNicknameKey: String? = null
        private set

    protected constructor()

    constructor(id: String?, username: String?, nickname: String?, passwordHash: String?) {
        this.id = id
        this.username = username
        this.nickname = nickname
        this.passwordHash = passwordHash
        this.activeUsernameKey = username
        this.activeNicknameKey = nickname
    }

    @PrePersist
    fun prePersist() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }

    fun updateNickname(nickname: String?) {
        this.nickname = nickname
        this.activeNicknameKey = nickname
    }

    fun softDelete(deletedAt: Instant?) {
        this.deletedAt = deletedAt
        this.activeUsernameKey = null
        this.activeNicknameKey = null
    }
}
