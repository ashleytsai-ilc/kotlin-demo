package com.example.demo.auth.jwt;

import com.example.demo.auth.revocation.RevokedTokenChecker;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final RevokedTokenChecker revokedTokenChecker;

    public JwtService(
            JwtProperties properties,
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            RevokedTokenChecker revokedTokenChecker
    ) {
        this.properties = properties;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.revokedTokenChecker = revokedTokenChecker;
    }

    public String issueAccessToken(String subject) {
        return issueToken(subject, JwtTokenType.ACCESS, properties.accessTokenExpiration());
    }

    public String issueRefreshToken(String subject) {
        return issueToken(subject, JwtTokenType.REFRESH, properties.refreshTokenExpiration());
    }

    private String issueToken(String subject, JwtTokenType tokenType, Duration expiration) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plus(expiration))
                .id(UUID.randomUUID().toString())
                .claim(JwtTokenType.CLAIM, tokenType.value())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public String validateRefreshToken(String token) {
        JwtTokenClaims claims = validateRefreshTokenClaims(token);
        return claims.subject();
    }

    public JwtTokenClaims validateRefreshTokenClaims(String token) {
        JwtTokenClaims claims = validateToken(token, JwtTokenType.REFRESH);
        if (revokedTokenChecker.isRevoked(claims.tokenId())) {
            throw new JwtException("JWT has been revoked.");
        }
        return claims;
    }

    public String subject(String token) {
        return decode(token).getSubject();
    }

    public String tokenType(String token) {
        return tokenType(decode(token));
    }

    public String tokenId(String token) {
        return decode(token).getId();
    }

    public Instant issuedAt(String token) {
        return decode(token).getIssuedAt();
    }

    public Instant expiresAt(String token) {
        return decode(token).getExpiresAt();
    }

    private Jwt decode(String token) {
        return jwtDecoder.decode(token);
    }

    public JwtTokenClaims validateToken(String token, JwtTokenType expectedType) {
        Jwt jwt = decode(token);
        if (!expectedType.value().equals(tokenType(jwt))) {
            throw new JwtException("JWT token type is invalid.");
        }
        String tokenId = jwt.getId();
        if (tokenId == null || tokenId.isBlank()) {
            throw new JwtException("JWT token id is missing.");
        }
        return new JwtTokenClaims(
                tokenId,
                jwt.getSubject(),
                expectedType,
                jwt.getIssuedAt(),
                jwt.getExpiresAt()
        );
    }

    private String tokenType(Jwt jwt) {
        return jwt.getClaimAsString(JwtTokenType.CLAIM);
    }
}
