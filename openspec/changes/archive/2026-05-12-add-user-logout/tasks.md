## 1. Logout API Tests

- [x] 1.1 Add a failing API test that `POST /api/users/logout` with a valid access token and matching refresh token returns `204 No Content` with an empty body.
- [x] 1.2 Add a failing API test that repeated logout with the same token pair remains idempotent and returns `204 No Content`.
- [x] 1.3 Add failing API tests that missing, invalid, or refresh-token-as-bearer Authorization values return `401 Unauthorized` with `UNAUTHORIZED`.
- [x] 1.4 Add failing API tests that missing body, missing `refresh_token`, blank, invalid, and expired refresh tokens return `401 Unauthorized` with `INVALID_REFRESH_TOKEN`.
- [x] 1.5 Add a failing API test that access token and refresh token from different users are rejected with `INVALID_REFRESH_TOKEN`.
- [x] 1.6 Add a failing API test that a revoked access token is rejected by protected APIs with `401 Unauthorized`.
- [x] 1.7 Add a failing API test that a revoked refresh token is rejected by the refresh endpoint with `INVALID_REFRESH_TOKEN`.
- [x] 1.8 Add failing checks that logout-all-devices, session list, server-side session management, and refresh token reuse detection are not added by this change.

## 2. JWT Token Identifier

- [x] 2.1 Add centralized constant support for the JWT token id claim without scattering magic strings.
- [x] 2.2 Update access and refresh token issuing so every token includes a unique token id.
- [x] 2.3 Add JwtService accessors or validation results needed to read subject, token id, token type, issued-at, and expires-at without duplicating decode logic.
- [x] 2.4 Update existing JWT tests to assert access and refresh tokens include token ids.

## 3. Token Revocation Persistence

- [x] 3.1 Add a revoked token entity with token id, user id, token type, expires-at, and revoked-at fields.
- [x] 3.2 Add centralized constants for revoked token table, column, and constraint names.
- [x] 3.3 Add a revoked token repository that can check active revocation by token id and upsert or ignore repeated revocations.
- [x] 3.4 Add a focused revocation service that records access and refresh token revocations idempotently.
- [x] 3.5 Add tests for idempotent token revocation persistence.

## 4. Logout Implementation

- [x] 4.1 Add logout route constants for `POST /api/users/logout`.
- [x] 4.2 Add logout request DTO that accepts `refresh_token`.
- [x] 4.3 Add `POST /api/users/logout` to `UserAuthController` and return `204 No Content` on success.
- [x] 4.4 Permit unauthenticated access to the logout route in `SecurityConfig` so the service can preserve idempotent repeated logout semantics.
- [x] 4.5 Implement logout service logic that validates the bearer access token, validates the refresh token, confirms both tokens belong to the same existing user, and records both revocations.
- [x] 4.6 Map invalid access-token logout failures to `401 Unauthorized` with `UNAUTHORIZED`.
- [x] 4.7 Map invalid refresh-token logout failures to `401 Unauthorized` with `INVALID_REFRESH_TOKEN`.

## 5. Revocation Enforcement

- [x] 5.1 Update resource server access-token validation to reject revoked access token ids while preserving existing signature, expiration, and token type checks.
- [x] 5.2 Update refresh token validation to reject revoked refresh token ids while preserving existing refresh token failure semantics.
- [x] 5.3 Ensure repeated logout can still return `204 No Content` even when the submitted tokens are already recorded as revoked.
- [x] 5.4 Ensure non-revoked refresh tokens remain usable until expiration unless another validation rule rejects them.

## 6. Verification

- [x] 6.1 Run focused logout API and token revocation tests.
- [x] 6.2 Run `./mvnw test`.
- [x] 6.3 Run `openspec validate add-user-logout --strict`.
- [x] 6.4 Review implementation against `user-logout`, `jwt-authentication`, `refresh-token`, and `user-login` delta specs.
- [x] 6.5 Update task checkboxes as implementation work is completed.
- [x] 6.6 Define and verify that refresh ignores Authorization bearer access token headers.
- [x] 6.7 Ensure duplicate revocation insert failures do not rollback the caller transaction.
