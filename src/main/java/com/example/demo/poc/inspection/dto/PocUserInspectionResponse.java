package com.example.demo.poc.inspection.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

public record PocUserInspectionResponse(
        String id,
        String username,
        @JsonInclude(JsonInclude.Include.ALWAYS)
        String nickname,
        String passwordHash,
        Instant createdAt,
        Instant updatedAt,
        @JsonInclude(JsonInclude.Include.ALWAYS)
        Instant deletedAt,
        @JsonInclude(JsonInclude.Include.ALWAYS)
        String activeUsernameKey,
        @JsonInclude(JsonInclude.Include.ALWAYS)
        String activeNicknameKey
) {
}
