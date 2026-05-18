## 1. Contract Confirmation

- [x] 1.1 Confirm login uses only `username` and `password`.
- [x] 1.2 Confirm login success response uses the same top-level profile shape as registration plus `access_token`.
- [x] 1.3 Confirm missing credentials, unknown username, and wrong password all return `401 Unauthorized` with `INVALID_CREDENTIALS`.
- [x] 1.4 Confirm JWT settings reuse the existing signing secret and access token expiration.
- [x] 1.5 Confirm refresh token, logout, server-side session, and multi-device session management are out of scope.

## 2. Login API TDD

- [x] 2.1 Add a failing API test that `POST /api/users/login` succeeds with a valid `username` and `password`.
- [x] 2.2 Add a failing API test that login success returns `access_token`, `id`, `username`, `nickname`, `created_at`, and `updated_at`.
- [x] 2.3 Add a failing API test that login success response does not include plaintext password or password hash.
- [x] 2.4 Add failing API tests that unknown username and wrong password return `401 Unauthorized` with `INVALID_CREDENTIALS`.
- [x] 2.5 Add failing API tests that missing `username` or missing `password` return `401 Unauthorized` with `INVALID_CREDENTIALS`.
- [x] 2.6 Add a failing API test that login does not require an existing bearer token.
- [x] 2.7 Add failing checks that this change does not add refresh token, logout, or session-management endpoints.
- [x] 2.8 Add a failing test that the login-issued JWT subject identifies the logged-in user and uses the existing expiration setting.

## 3. Login Implementation

- [x] 3.1 Add login request DTO and shared auth response DTO that follow existing API response conventions.
- [x] 3.2 Add `INVALID_CREDENTIALS` to the shared API error catalog.
- [x] 3.3 Implement login logic in the shared user auth service that looks up users by username and verifies passwords with `PasswordEncoder.matches`.
- [x] 3.4 Reuse `JwtService.issueToken(user.getId())` for successful login.
- [x] 3.5 Add login controller endpoint at `POST /api/users/login` in the shared user auth controller.
- [x] 3.6 Update Spring Security configuration to permit unauthenticated login requests.
- [x] 3.7 Ensure login errors use the standard `code`, `message`, and `details` response shape.

## 4. Verification

- [x] 4.1 Run login-related tests and make them pass.
- [x] 4.2 Run `./mvnw test`.
- [x] 4.3 Review implemented behavior against `user-login` and `jwt-authentication` delta specs.
- [x] 4.4 Update task checkboxes as implementation work is completed.
