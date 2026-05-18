package com.example.demo.auth.revocation;

import com.example.demo.auth.jwt.JwtTokenType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = RevokedTokenMapping.TABLE_NAME,
        uniqueConstraints = @UniqueConstraint(
                name = RevokedTokenMapping.TOKEN_ID_UNIQUE_CONSTRAINT,
                columnNames = RevokedTokenMapping.TOKEN_ID_COLUMN
        )
)
public class RevokedToken {

    @Id
    @Column(name = RevokedTokenMapping.TOKEN_ID_COLUMN, length = RevokedTokenMapping.TOKEN_ID_LENGTH, nullable = false, updatable = false)
    private String tokenId;

    @Column(name = RevokedTokenMapping.USER_ID_COLUMN, length = RevokedTokenMapping.USER_ID_LENGTH, nullable = false, updatable = false)
    private String userId;

    @Column(name = RevokedTokenMapping.TOKEN_TYPE_COLUMN, length = RevokedTokenMapping.TOKEN_TYPE_LENGTH, nullable = false, updatable = false)
    private String tokenType;

    @Column(name = RevokedTokenMapping.EXPIRES_AT_COLUMN, nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = RevokedTokenMapping.REVOKED_AT_COLUMN, nullable = false, updatable = false)
    private Instant revokedAt;

    protected RevokedToken() {
    }

    public RevokedToken(String tokenId, String userId, JwtTokenType tokenType, Instant expiresAt, Instant revokedAt) {
        this.tokenId = tokenId;
        this.userId = userId;
        this.tokenType = tokenType.value();
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getUserId() {
        return userId;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }
}
