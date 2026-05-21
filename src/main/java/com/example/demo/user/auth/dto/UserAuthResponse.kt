package com.example.demo.user.auth.dto;

import java.time.Instant;

public record UserAuthResponse(
        String id,
        String username,
        String nickname,
        Instant createdAt,
        Instant updatedAt,
        String accessToken,
        String refreshToken
) {
}
