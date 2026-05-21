package com.example.demo.auth.revocation

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface RevokedTokenRepository : JpaRepository<RevokedToken, String> {
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        value = RevokedTokenRepositorySql.INSERT_IF_ABSENT,
        nativeQuery = true,
    )
    fun insertIfAbsent(
        @Param("tokenId")
        tokenId: String,
        @Param("userId")
        userId: String,
        @Param("tokenType")
        tokenType: String,
        @Param("expiresAt")
        expiresAt: Instant,
        @Param("revokedAt")
        revokedAt: Instant,
    ): Int
}

private object RevokedTokenRepositorySql {
    const val INSERT_IF_ABSENT: String =
        "INSERT INTO " + RevokedTokenMapping.TABLE_NAME + " " +
            "(" +
            RevokedTokenMapping.TOKEN_ID_COLUMN + ", " +
            RevokedTokenMapping.USER_ID_COLUMN + ", " +
            RevokedTokenMapping.TOKEN_TYPE_COLUMN + ", " +
            RevokedTokenMapping.EXPIRES_AT_COLUMN + ", " +
            RevokedTokenMapping.REVOKED_AT_COLUMN +
            ") " +
            "SELECT :tokenId, :userId, :tokenType, :expiresAt, :revokedAt " +
            "WHERE NOT EXISTS (" +
            "SELECT 1 FROM " + RevokedTokenMapping.TABLE_NAME + " " +
            "WHERE " + RevokedTokenMapping.TOKEN_ID_COLUMN + " = :tokenId" +
            ")"
}
