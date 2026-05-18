## 1. Refresh Token API Tests

- [x] 1.1 Add failing registration API assertions that success responses include `refresh_token`.
- [x] 1.2 Add failing login API assertions that success responses include `refresh_token`.
- [x] 1.3 Add a failing API test that `POST /api/users/tokens/refresh` with a valid refresh token returns `200 OK`, `access_token`, and `refresh_token`.
- [x] 1.4 Add a failing API test that refresh success response does not include profile fields.
- [x] 1.5 Add failing API tests that missing body, missing, blank, invalid, and expired refresh tokens return `401 Unauthorized` with `INVALID_REFRESH_TOKEN`.
- [x] 1.6 Add a failing API test that a valid access token submitted as `refresh_token` is rejected with `401 Unauthorized`.
- [x] 1.7 Add a failing API test that refresh token subject must identify an existing user.
- [x] 1.8 Add failing checks that logout, revocation, denylist, server-side session, and multi-device session management are not added by this change.

## 2. JWT Token Model

- [x] 2.1 Add refresh token expiration to `JwtProperties` and `application.yaml`.
- [x] 2.2 Add centralized constants or enum values for JWT token type claim names and values.
- [x] 2.3 Split JWT issuing into access-token and refresh-token methods that set token type claims.
- [x] 2.4 Add refresh-token validation that verifies signature, expiration, subject, and token type.
- [x] 2.5 Add helper accessors needed by tests for token type and configured expiration without duplicating magic strings or durations.

## 3. Auth Response Changes

- [x] 3.1 Update the shared auth response DTO so registration and login responses include `refresh_token`.
- [x] 3.2 Ensure registration and login still exclude plaintext password and password hash.
- [x] 3.3 Ensure registration and login continue returning the existing profile fields and `access_token`.

## 4. Refresh Endpoint Implementation

- [x] 4.1 Add refresh request DTO that accepts `refresh_token`.
- [x] 4.2 Add token-only refresh response DTO containing `access_token` and `refresh_token`.
- [x] 4.3 Add refresh route constants to `UserAuthRoutes`.
- [x] 4.4 Add `POST /api/users/tokens/refresh` to `UserAuthController`.
- [x] 4.5 Implement refresh logic in `UserAuthService` that validates the refresh token, confirms the user exists, and issues a new token pair.
- [x] 4.6 Map all refresh validation failures to `401 Unauthorized` with `INVALID_REFRESH_TOKEN`.

## 5. Security Configuration

- [x] 5.1 Permit unauthenticated `POST /api/users/tokens/refresh` requests in `SecurityConfig`.
- [x] 5.2 Ensure protected API bearer-token behavior is unchanged for valid, missing, and invalid access tokens.
- [x] 5.3 Verify `SecurityConfig` continues to depend on route constants rather than controller classes.
- [x] 5.4 Reject refresh tokens when they are submitted as bearer tokens to protected APIs.

## 6. Verification

- [x] 6.1 Run focused auth API and JWT-related tests.
- [x] 6.2 Run `./mvnw test`.
- [x] 6.3 Run `openspec validate add-refresh-token-mechanism --strict`.
- [x] 6.4 Review implementation against `refresh-token`, `user-registration`, `user-login`, and `jwt-authentication` delta specs.
- [x] 6.5 Update task checkboxes as implementation work is completed.
