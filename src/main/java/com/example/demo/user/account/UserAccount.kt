package com.example.demo.user.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = UserAccountMapping.TABLE_NAME,
        uniqueConstraints = {
                @UniqueConstraint(
                        name = UserAccountMapping.ACTIVE_USERNAME_KEY_UNIQUE_CONSTRAINT,
                        columnNames = UserAccountMapping.ACTIVE_USERNAME_KEY_COLUMN
                ),
                @UniqueConstraint(
                        name = UserAccountMapping.ACTIVE_NICKNAME_KEY_UNIQUE_CONSTRAINT,
                        columnNames = UserAccountMapping.ACTIVE_NICKNAME_KEY_COLUMN
                )
        }
)
public class UserAccount {

    @Id
    @Column(length = UserAccountMapping.ID_LENGTH, nullable = false, updatable = false)
    private String id;

    @Column(nullable = false, length = UserAccountMapping.USERNAME_LENGTH)
    private String username;

    @Column(length = UserAccountMapping.NICKNAME_LENGTH)
    private String nickname;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(name = UserAccountMapping.DELETED_AT_COLUMN)
    private Instant deletedAt;

    @Column(name = UserAccountMapping.ACTIVE_USERNAME_KEY_COLUMN, length = UserAccountMapping.USERNAME_LENGTH)
    private String activeUsernameKey;

    @Column(name = UserAccountMapping.ACTIVE_NICKNAME_KEY_COLUMN, length = UserAccountMapping.NICKNAME_LENGTH)
    private String activeNicknameKey;

    protected UserAccount() {
    }

    public UserAccount(String id, String username, String nickname, String passwordHash) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.passwordHash = passwordHash;
        this.activeUsernameKey = username;
        this.activeNicknameKey = nickname;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
        this.activeNicknameKey = nickname;
    }

    public void softDelete(Instant deletedAt) {
        this.deletedAt = deletedAt;
        this.activeUsernameKey = null;
        this.activeNicknameKey = null;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public String getActiveUsernameKey() {
        return activeUsernameKey;
    }

    public String getActiveNicknameKey() {
        return activeNicknameKey;
    }
}
