## 1. API Tests

- [x] 1.1 Add failing API tests that `GET /api/poc/users` works without an Authorization header and returns all persisted user account rows.
- [x] 1.2 Add failing API tests that user inspection items include `id`, `username`, `nickname`, `password_hash`, `created_at`, and `updated_at`.
- [x] 1.3 Add a failing API test that `GET /api/poc/users` returns an empty collection when no user accounts exist.
- [x] 1.4 Add failing API tests that `GET /api/poc/revoked-tokens` works without an Authorization header and returns all persisted revoked token rows.
- [x] 1.5 Add failing API tests that revoked token inspection items include `token_id`, `user_id`, `token_type`, `expires_at`, and `revoked_at`.
- [x] 1.6 Add a failing API test that `GET /api/poc/revoked-tokens` returns an empty collection when no revoked tokens exist.
- [x] 1.7 Add checks that POC inspection does not change existing register, login, refresh, or logout response contracts.
- [x] 1.8 Add failing API tests that user inspection items include `deleted_at`, including `deleted_at: null` for active users and a timestamp for soft-deleted users.
- [x] 1.9 Add failing API tests that user inspection items include `active_username_key` and `active_nickname_key`, including active values for active users with a nickname, `active_nickname_key: null` for active users without a nickname, and null values for soft-deleted users.

## 2. Routes and Security

- [x] 2.1 Add centralized route constants for `GET /api/poc/users` and `GET /api/poc/revoked-tokens`.
- [x] 2.2 Update `SecurityConfig` to permit unauthenticated GET access to the POC inspection routes.

## 3. POC Inspection Implementation

- [x] 3.1 Add a `com.example.demo.poc.inspection` package for POC inspection controller, service, route constants, and DTOs.
- [x] 3.2 Add user inspection response DTOs that expose persisted user account fields with `snake_case` JSON keys.
- [x] 3.3 Add revoked token inspection response DTOs that expose persisted revoked token fields with `snake_case` JSON keys.
- [x] 3.4 Add a read-only inspection service that loads all user accounts and revoked tokens from their repositories.
- [x] 3.5 Add a POC inspection controller with `GET /api/poc/users` and `GET /api/poc/revoked-tokens`.
- [x] 3.6 Ensure no create, update, delete, filter, pagination, sorting, or export endpoint is added.
- [x] 3.7 Update user inspection response DTOs to expose `deleted_at` with null serialization for active users.
- [x] 3.8 Update user inspection response DTOs to expose `active_username_key` and `active_nickname_key` with null serialization.

## 4. Verification

- [x] 4.1 Run focused POC inspection API tests.
- [x] 4.2 Run `./mvnw test`.
- [x] 4.3 Run `openspec validate add-poc-data-inspection-api --strict`.
- [x] 4.4 Review implementation against `poc-data-inspection` specs and update task checkboxes.
- [x] 4.5 Ensure POC inspection APIs ignore Authorization headers.
- [x] 4.6 Run focused POC inspection API tests after adding `deleted_at`, `active_username_key`, and `active_nickname_key`.
- [x] 4.7 Run `openspec validate add-poc-data-inspection-api --strict`.
