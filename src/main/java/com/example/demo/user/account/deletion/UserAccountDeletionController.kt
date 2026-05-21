package com.example.demo.user.account.deletion;

import com.example.demo.auth.jwt.JwtTokenClaims;
import com.example.demo.auth.jwt.JwtTokenType;
import com.example.demo.common.ApiExceptions;
import com.example.demo.user.account.deletion.dto.AccountDeletionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserAccountDeletionRoutes.BASE_PATH)
public class UserAccountDeletionController {

    private final UserAccountDeletionService userAccountDeletionService;

    public UserAccountDeletionController(UserAccountDeletionService userAccountDeletionService) {
        this.userAccountDeletionService = userAccountDeletionService;
    }

    @DeleteMapping(UserAccountDeletionRoutes.ME_ROUTE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCurrentUser(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody AccountDeletionRequest request
    ) {
        userAccountDeletionService.deleteCurrentUser(toClaims(jwt), request);
    }

    private JwtTokenClaims toClaims(Jwt jwt) {
        if (jwt == null || jwt.getId() == null || jwt.getId().isBlank()) {
            throw ApiExceptions.unauthorized();
        }
        return new JwtTokenClaims(
                jwt.getId(),
                jwt.getSubject(),
                JwtTokenType.ACCESS,
                jwt.getIssuedAt(),
                jwt.getExpiresAt()
        );
    }
}
