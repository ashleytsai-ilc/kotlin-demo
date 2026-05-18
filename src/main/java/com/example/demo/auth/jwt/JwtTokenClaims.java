package com.example.demo.auth.jwt;

import java.time.Instant;

public record JwtTokenClaims(
        String tokenId,
        String subject,
        JwtTokenType tokenType,
        Instant issuedAt,
        Instant expiresAt
) {
}
