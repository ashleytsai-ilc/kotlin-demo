## 1. Test Coverage

- [x] 1.1 Add failing API tests for successful `DELETE /api/users/me` with valid bearer access token, correct `password`, and same-user `refresh_token`, expecting `204 No Content`.
- [x] 1.2 Add failing persistence assertions proving account deletion sets `deletedAt`, preserves original `username` / `nickname`, and clears active username/nickname uniqueness keys.
- [x] 1.3 Add failing API tests proving submitted access token and submitted refresh token are recorded as revoked after successful account deletion.
- [x] 1.4 Add failing API tests for missing, invalid, refresh-token-as-bearer, revoked, and soft-deleted-subject access token rejection on account deletion.
- [x] 1.5 Add failing API tests for missing request body returning `400 Bad Request` / `VALIDATION_ERROR`.
- [x] 1.6 Add failing API tests for missing, blank, and incorrect `password` returning `401 Unauthorized` / `INVALID_CREDENTIALS` without deleting the user.
- [x] 1.7 Add failing API tests for missing, blank, invalid, expired, revoked, access-token-as-refresh-token, and cross-user `refresh_token` in account deletion returning `401 Unauthorized` / `INVALID_CREDENTIALS`.
- [x] 1.8 Add failing API tests proving a soft-deleted user cannot login and receives `401 Unauthorized` / `INVALID_CREDENTIALS`.
- [x] 1.9 Add failing API tests proving refresh token for a soft-deleted user returns `401 Unauthorized` / `INVALID_REFRESH_TOKEN`.
- [x] 1.10 Add failing API tests proving the token pair revoked by account deletion cannot logout and returns `401 Unauthorized` / `UNAUTHORIZED`.
- [x] 1.11 Add failing API tests proving profile update with a soft-deleted subject returns `401 Unauthorized` / `UNAUTHORIZED`.
- [x] 1.12 Add failing API tests proving username and nickname from a soft-deleted account can be reused during registration.
- [x] 1.13 Add failing API tests proving duplicate username and duplicate nickname are still rejected when held by active users.
- [x] 1.14 Add failing API tests proving nickname from a soft-deleted account can be reused during profile update while duplicate active nickname is still rejected.

## 2. Data Model and Repository

- [x] 2.1 Add `deletedAt`, `activeUsernameKey`, and `activeNicknameKey` fields to `UserAccount` with `snake_case` column mappings.
- [x] 2.2 Move database unique constraints from `username` / `nickname` to `active_username_key` / `active_nickname_key`.
- [x] 2.3 Initialize active unique keys when constructing a new `UserAccount`.
- [x] 2.4 Update `UserAccount.updateNickname(...)` to keep `activeNicknameKey` synchronized with nickname changes and cleared nicknames.
- [x] 2.5 Add `UserAccount.softDelete(...)` to set `deletedAt` and clear both active unique keys without changing original `username` or `nickname`.
- [x] 2.6 Add repository methods for active-user lookup and active username/nickname existence checks.

## 3. Account Deletion API Implementation

- [x] 3.1 Add account deletion route constants for `DELETE /api/users/me` without changing existing profile route behavior.
- [x] 3.2 Add account deletion request DTO containing `password` and `refresh_token`.
- [x] 3.3 Add account deletion controller that requires a request body, reads the Authorization header, and returns `204 No Content` on success.
- [x] 3.4 Add account deletion service that uses authenticated access token claims, loads the active user first, then validates refresh token, verifies same-user token pair, verifies password, soft deletes the user, and revokes both submitted tokens.
- [x] 3.5 Reuse centralized error catalog behavior for `UNAUTHORIZED`, `INVALID_CREDENTIALS`, `INVALID_REFRESH_TOKEN`, and missing-body `VALIDATION_ERROR`; account deletion request-body credential failures use `INVALID_CREDENTIALS`; do not introduce throw-site magic strings.
- [x] 3.6 Ensure `DELETE /api/users/me` is not permit-all and is not ignored by the custom bearer token resolver.
- [x] 3.7 Ensure account deletion does not add logout-all-devices, session list, token family invalidation, restore account, admin delete, or hard delete behavior.

## 4. Existing Flow Alignment

- [x] 4.1 Update registration uniqueness checks to use active username/nickname semantics while preserving active-user conflict responses.
- [x] 4.2 Update login lookup to authenticate only active users and keep soft-deleted users indistinguishable from invalid credentials.
- [x] 4.3 Update refresh token flow to require the token subject to identify an active user.
- [x] 4.4 Update logout flow to require the token subject to identify an active user and return `401 Unauthorized` / `UNAUTHORIZED` for soft-deleted users.
- [x] 4.5 Update profile update flow to require the token subject to identify an active user.
- [x] 4.6 Update profile nickname uniqueness checks to use active nickname semantics while preserving active-user conflict responses.
- [x] 4.7 Confirm register, login, refresh, logout, and profile success response contracts do not include `deleted_at`.

## 5. Cross-Change Coordination

- [x] 5.1 Keep POC inspection `deleted_at`, `active_username_key`, and `active_nickname_key` response contract in `add-poc-data-inspection-api`, not in this change.
- [x] 5.2 Confirm `active_username_key` and `active_nickname_key` are exposed only by POC inspection and remain absent from register, login, refresh, logout, and profile responses.
- [x] 5.3 Coordinate implementation order so this change provides the `deleted_at`, `active_username_key`, and `active_nickname_key` schema/entity fields before POC inspection DTO/output tasks for those fields are completed.
- [x] 5.4 Coordinate sync/archive order so `add-user-account-deletion-api` schema support is synced or archived before any completed `add-poc-data-inspection-api` contract that requires those fields.

## 6. Verification

- [x] 6.1 Run the new focused account deletion API tests and confirm they fail before implementation where applicable.
- [x] 6.2 Run the account deletion, auth, profile, and POC inspection API tests after implementation and confirm they pass.
- [x] 6.3 Run `./mvnw test` with Java 17.
- [x] 6.4 Run `openspec validate add-user-account-deletion-api --strict`.
- [x] 6.5 Run `openspec validate --all --strict`.
