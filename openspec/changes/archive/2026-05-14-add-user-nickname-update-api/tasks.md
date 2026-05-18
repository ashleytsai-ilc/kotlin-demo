## 1. Test Coverage

- [x] 1.1 Add focused API tests for successful `PATCH /api/users/me` nickname update and response profile fields.
- [x] 1.2 Add API tests proving the update is scoped to the bearer access token user and does not update other users.
- [x] 1.3 Add API tests for missing, invalid, refresh-token, revoked-token, and token-subject-without-user Authorization bearer rejection.
- [x] 1.4 Add API tests proving unknown fields and immutable fields such as `username`, `password`, `id`, access token, and refresh token are ignored without validation error, cannot be changed, and are not returned by the profile update response.
- [x] 1.5 Add API tests for missing request body returning `400 Bad Request`, blank `nickname` clearing the nickname, null or omitted `nickname` returning validation error, and cleared responses including `nickname: ""`.
- [x] 1.6 Add API tests for nickname too long, duplicate nickname, same own nickname with unchanged `updated_at`, and multiple users without nickname.

## 2. Profile API Implementation

- [x] 2.1 Add `user.profile` route, controller, service, request DTO, response DTO, and validation package structure.
- [x] 2.2 Add `PATCH /api/users/me` endpoint that requires a valid bearer access token and reads the authenticated subject.
- [x] 2.3 Implement profile update service logic to load the current user, normalize nickname, validate nickname, enforce uniqueness, skip persistence for no-op updates, and save actual updates.
- [x] 2.4 Add a dedicated `UserAccount.updateNickname(...)` method and keep timestamp updates handled by entity lifecycle.
- [x] 2.5 Add or reuse centralized constants, enum values, and API error catalog entries needed for profile validation and errors.
- [x] 2.6 Ensure profile update response serialization always includes `nickname` and maps cleared nickname values to an empty string, without changing register or login response contracts.
- [x] 2.7 Convert DB constraint conflicts during save/flush into a generic conflict response consistent with the current POC strategy.

## 3. Security and Contract Checks

- [x] 3.1 Ensure `PATCH /api/users/me` is not configured as permit-all and continues to reject non-access JWT bearer tokens.
- [x] 3.2 Ensure profile update does not issue new tokens and does not change register, login, refresh, or logout behavior.
- [x] 3.3 Ensure no magic strings or magic numbers are introduced for routes, error codes, error messages, fields, or nickname length.

## 4. Verification

- [x] 4.1 Run the new profile API tests and confirm they fail before implementation where applicable.
- [x] 4.2 Run the new profile API tests after implementation and confirm they pass.
- [x] 4.3 Run `./mvnw test`.
- [x] 4.4 Run `openspec validate add-user-nickname-update-api --strict`.

## 5. Request Body Contract Alignment

- [x] 5.1 Move remaining missing request body handling from service/validator null checks to Spring MVC `@RequestBody` required behavior.
- [x] 5.2 Update auth/profile tests to expect unified `400 Bad Request` / `VALIDATION_ERROR` for missing request bodies.
- [x] 5.3 Update OpenSpec main specs and change artifacts to reflect the unified missing request body contract.
