package com.example.demo.auth.revocation

import com.example.demo.auth.jwt.JwtTokenClaims
import com.example.demo.auth.jwt.JwtTokenType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.Duration
import java.time.Instant

@SpringBootTest
internal class RevokedTokenRecorderTest @Autowired constructor(
    private val revokedTokenRecorder: RevokedTokenRecorder,
    private val revokedTokenChecker: RevokedTokenChecker,
    private val revokedTokenRepository: RevokedTokenRepository,
    transactionManager: PlatformTransactionManager
) {
    private val transactionTemplate = TransactionTemplate(transactionManager)

    @BeforeEach
    fun setUp() {
        revokedTokenRepository.deleteAll()
    }

    @Test
    fun recordsRevocationIdempotently() {
        val claims = claims(ACCESS_TOKEN_ID, JwtTokenType.ACCESS, Instant.now().plus(TOKEN_LIFETIME))

        revokedTokenRecorder.record(claims)
        revokedTokenRecorder.record(claims)

        assertThat(revokedTokenRepository.count()).isEqualTo(EXPECTED_SINGLE_REVOCATION_COUNT)
        assertThat(revokedTokenChecker.isRevoked(ACCESS_TOKEN_ID)).isTrue()
        assertThat(revokedTokenRepository.findById(ACCESS_TOKEN_ID))
            .hasValueSatisfying { revokedToken ->
                assertThat(revokedToken.userId).isEqualTo(USER_ID)
                assertThat(revokedToken.tokenType).isEqualTo(JwtTokenType.ACCESS.value())
                assertThat(revokedToken.expiresAt).isEqualTo(claims.expiresAt)
                assertThat(revokedToken.revokedAt).isNotNull()
            }
    }

    @Test
    fun expiredRevocationStillCountsAsRevoked() {
        val claims = claims(REFRESH_TOKEN_ID, JwtTokenType.REFRESH, Instant.now().minus(TOKEN_LIFETIME))

        revokedTokenRecorder.record(claims)

        assertThat(revokedTokenChecker.isRevoked(REFRESH_TOKEN_ID)).isTrue()
    }

    @Test
    fun duplicateRevocationDoesNotRollbackCallerTransaction() {
        val duplicateClaims = claims(ACCESS_TOKEN_ID, JwtTokenType.ACCESS, Instant.now().plus(TOKEN_LIFETIME))
        val otherClaims = claims(OUTER_TRANSACTION_TOKEN_ID, JwtTokenType.REFRESH, Instant.now().plus(TOKEN_LIFETIME))

        revokedTokenRecorder.record(duplicateClaims)

        transactionTemplate.executeWithoutResult {
            revokedTokenRecorder.record(duplicateClaims)
            revokedTokenRepository.saveAndFlush(revokedToken(otherClaims))
        }

        assertThat(revokedTokenRepository.count()).isEqualTo(EXPECTED_TWO_REVOCATION_COUNT)
        assertThat(revokedTokenChecker.isRevoked(ACCESS_TOKEN_ID)).isTrue()
        assertThat(revokedTokenChecker.isRevoked(OUTER_TRANSACTION_TOKEN_ID)).isTrue()
    }

    @Test
    fun consumeForRefreshRejectsDuplicateTokenId() {
        val claims = claims(REFRESH_TOKEN_ID, JwtTokenType.REFRESH, Instant.now().plus(TOKEN_LIFETIME))

        val firstConsumed =
            transactionTemplate.execute {
                revokedTokenRecorder.consumeForRefresh(claims)
            }
        val secondConsumed =
            transactionTemplate.execute {
                revokedTokenRecorder.consumeForRefresh(claims)
            }

        assertThat(firstConsumed).isTrue()
        assertThat(secondConsumed).isFalse()
        assertThat(revokedTokenRepository.count()).isEqualTo(EXPECTED_SINGLE_REVOCATION_COUNT)
        assertThat(revokedTokenChecker.isRevoked(REFRESH_TOKEN_ID)).isTrue()
    }

    private fun claims(tokenId: String, tokenType: JwtTokenType, expiresAt: Instant): JwtTokenClaims {
        return JwtTokenClaims(tokenId, USER_ID, tokenType, Instant.now(), expiresAt)
    }

    private fun revokedToken(claims: JwtTokenClaims): RevokedToken {
        return RevokedToken(
            claims.tokenId,
            claims.subject,
            claims.tokenType,
            claims.expiresAt,
            Instant.now()
        )
    }

    companion object {
        private const val ACCESS_TOKEN_ID = "11111111-1111-1111-1111-111111111111"
        private const val REFRESH_TOKEN_ID = "22222222-2222-2222-2222-222222222222"
        private const val OUTER_TRANSACTION_TOKEN_ID = "33333333-3333-3333-3333-333333333333"
        private const val USER_ID = "01KRD5JZA4DYH7E0XY3G2T7RP2"
        private val TOKEN_LIFETIME: Duration = Duration.ofHours(1)
        private const val EXPECTED_SINGLE_REVOCATION_COUNT = 1L
        private const val EXPECTED_TWO_REVOCATION_COUNT = 2L
    }
}
