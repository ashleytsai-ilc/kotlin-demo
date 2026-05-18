package com.example.demo.poc.inspection;

import com.example.demo.auth.revocation.RevokedToken;
import com.example.demo.auth.revocation.RevokedTokenRepository;
import com.example.demo.poc.inspection.dto.PocRevokedTokenInspectionResponse;
import com.example.demo.poc.inspection.dto.PocUserInspectionResponse;
import com.example.demo.user.account.UserAccount;
import com.example.demo.user.account.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PocInspectionService {

    private final UserRepository userRepository;
    private final RevokedTokenRepository revokedTokenRepository;

    public PocInspectionService(
            UserRepository userRepository,
            RevokedTokenRepository revokedTokenRepository
    ) {
        this.userRepository = userRepository;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Transactional(readOnly = true)
    public List<PocUserInspectionResponse> users() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PocRevokedTokenInspectionResponse> revokedTokens() {
        return revokedTokenRepository.findAll().stream()
                .map(this::toRevokedTokenResponse)
                .toList();
    }

    private PocUserInspectionResponse toUserResponse(UserAccount user) {
        return new PocUserInspectionResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getPasswordHash(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getDeletedAt(),
                user.getActiveUsernameKey(),
                user.getActiveNicknameKey()
        );
    }

    private PocRevokedTokenInspectionResponse toRevokedTokenResponse(RevokedToken revokedToken) {
        return new PocRevokedTokenInspectionResponse(
                revokedToken.getTokenId(),
                revokedToken.getUserId(),
                revokedToken.getTokenType(),
                revokedToken.getExpiresAt(),
                revokedToken.getRevokedAt()
        );
    }
}
