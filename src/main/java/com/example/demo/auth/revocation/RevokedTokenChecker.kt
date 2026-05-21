package com.example.demo.auth.revocation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RevokedTokenChecker {

    private final RevokedTokenRepository revokedTokenRepository;

    public RevokedTokenChecker(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Transactional(readOnly = true)
    public boolean isRevoked(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            return false;
        }
        return revokedTokenRepository.existsById(tokenId);
    }
}
