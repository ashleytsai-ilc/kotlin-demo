package com.example.demo.user.profile.dto;

import java.time.Instant;

public record UserProfileResponse(
        String id,
        String username,
        String nickname,
        Instant createdAt,
        Instant updatedAt
) {
    private static final String EMPTY_NICKNAME = "";

    public UserProfileResponse {
        if (nickname == null) {
            nickname = EMPTY_NICKNAME;
        }
    }
}
