package com.example.demo.auth.revocation;

import com.example.demo.auth.jwt.JwtTokenClaims;
import com.example.demo.auth.jwt.JwtTokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RevokedTokenRecorderTest {

    private static final String ACCESS_TOKEN_ID = "11111111-1111-1111-1111-111111111111";
    private static final String REFRESH_TOKEN_ID = "22222222-2222-2222-2222-222222222222";
    private static final String OUTER_TRANSACTION_TOKEN_ID = "33333333-3333-3333-3333-333333333333";
    private static final String USER_ID = "01KRD5JZA4DYH7E0XY3G2T7RP2";
    private static final Duration TOKEN_LIFETIME = Duration.ofHours(1);
    private static final long EXPECTED_SINGLE_REVOCATION_COUNT = 1L;
    private static final long EXPECTED_TWO_REVOCATION_COUNT = 2L;

    private final RevokedTokenRecorder revokedTokenRecorder;
    private final RevokedTokenChecker revokedTokenChecker;
    private final RevokedTokenRepository revokedTokenRepository;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    RevokedTokenRecorderTest(
            RevokedTokenRecorder revokedTokenRecorder,
            RevokedTokenChecker revokedTokenChecker,
            RevokedTokenRepository revokedTokenRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.revokedTokenRecorder = revokedTokenRecorder;
        this.revokedTokenChecker = revokedTokenChecker;
        this.revokedTokenRepository = revokedTokenRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @BeforeEach
    void setUp() {
        revokedTokenRepository.deleteAll();
    }

    @Test
    void recordsRevocationIdempotently() {
        JwtTokenClaims claims = claims(ACCESS_TOKEN_ID, JwtTokenType.ACCESS, Instant.now().plus(TOKEN_LIFETIME));

        revokedTokenRecorder.record(claims);
        revokedTokenRecorder.record(claims);

        assertThat(revokedTokenRepository.count()).isEqualTo(EXPECTED_SINGLE_REVOCATION_COUNT);
        assertThat(revokedTokenChecker.isRevoked(ACCESS_TOKEN_ID)).isTrue();
        assertThat(revokedTokenRepository.findById(ACCESS_TOKEN_ID))
                .hasValueSatisfying(revokedToken -> {
                    assertThat(revokedToken.getUserId()).isEqualTo(USER_ID);
                    assertThat(revokedToken.getTokenType()).isEqualTo(JwtTokenType.ACCESS.value());
                    assertThat(revokedToken.getExpiresAt()).isEqualTo(claims.expiresAt());
                    assertThat(revokedToken.getRevokedAt()).isNotNull();
                });
    }

    @Test
    void expiredRevocationStillCountsAsRevoked() {
        JwtTokenClaims claims = claims(REFRESH_TOKEN_ID, JwtTokenType.REFRESH, Instant.now().minus(TOKEN_LIFETIME));

        revokedTokenRecorder.record(claims);

        assertThat(revokedTokenChecker.isRevoked(REFRESH_TOKEN_ID)).isTrue();
    }

    @Test
    void duplicateRevocationDoesNotRollbackCallerTransaction() {
        JwtTokenClaims duplicateClaims = claims(ACCESS_TOKEN_ID, JwtTokenType.ACCESS, Instant.now().plus(TOKEN_LIFETIME));
        JwtTokenClaims otherClaims = claims(OUTER_TRANSACTION_TOKEN_ID, JwtTokenType.REFRESH, Instant.now().plus(TOKEN_LIFETIME));

        revokedTokenRecorder.record(duplicateClaims);

        transactionTemplate.executeWithoutResult(status -> {
            revokedTokenRecorder.record(duplicateClaims);
            revokedTokenRepository.saveAndFlush(revokedToken(otherClaims));
        });

        assertThat(revokedTokenRepository.count()).isEqualTo(EXPECTED_TWO_REVOCATION_COUNT);
        assertThat(revokedTokenChecker.isRevoked(ACCESS_TOKEN_ID)).isTrue();
        assertThat(revokedTokenChecker.isRevoked(OUTER_TRANSACTION_TOKEN_ID)).isTrue();
    }

    private JwtTokenClaims claims(String tokenId, JwtTokenType tokenType, Instant expiresAt) {
        return new JwtTokenClaims(tokenId, USER_ID, tokenType, Instant.now(), expiresAt);
    }

    private RevokedToken revokedToken(JwtTokenClaims claims) {
        return new RevokedToken(
                claims.tokenId(),
                claims.subject(),
                claims.tokenType(),
                claims.expiresAt(),
                Instant.now()
        );
    }
}
