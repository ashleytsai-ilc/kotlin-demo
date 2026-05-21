package com.example.demo.poc.inspection.dto;

import java.time.Instant;

public record PocRevokedTokenInspectionResponse(
        String tokenId,
        String userId,
        String tokenType,
        Instant expiresAt,
        Instant revokedAt
) {
}
