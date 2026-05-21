package com.example.demo.auth.revocation

import com.example.demo.auth.jwt.JwtTokenType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = RevokedTokenMapping.TABLE_NAME,
    uniqueConstraints = [
        UniqueConstraint(
            name = RevokedTokenMapping.TOKEN_ID_UNIQUE_CONSTRAINT,
            columnNames = [RevokedTokenMapping.TOKEN_ID_COLUMN],
        ),
    ],
)
class RevokedToken {
    @Id
    @Column(
        name = RevokedTokenMapping.TOKEN_ID_COLUMN,
        length = RevokedTokenMapping.TOKEN_ID_LENGTH,
        nullable = false,
        updatable = false,
    )
    var tokenId: String? = null
        private set

    @Column(
        name = RevokedTokenMapping.USER_ID_COLUMN,
        length = RevokedTokenMapping.USER_ID_LENGTH,
        nullable = false,
        updatable = false,
    )
    var userId: String? = null
        private set

    @Column(
        name = RevokedTokenMapping.TOKEN_TYPE_COLUMN,
        length = RevokedTokenMapping.TOKEN_TYPE_LENGTH,
        nullable = false,
        updatable = false,
    )
    var tokenType: String? = null
        private set

    @Column(name = RevokedTokenMapping.EXPIRES_AT_COLUMN, nullable = false, updatable = false)
    var expiresAt: Instant? = null
        private set

    @Column(name = RevokedTokenMapping.REVOKED_AT_COLUMN, nullable = false, updatable = false)
    var revokedAt: Instant? = null
        private set

    protected constructor()

    constructor(
        tokenId: String?,
        userId: String?,
        tokenType: JwtTokenType,
        expiresAt: Instant?,
        revokedAt: Instant?,
    ) {
        this.tokenId = tokenId
        this.userId = userId
        this.tokenType = tokenType.value()
        this.expiresAt = expiresAt
        this.revokedAt = revokedAt
    }
}
