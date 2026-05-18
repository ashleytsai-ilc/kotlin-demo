## 1. Scope Confirmation

- [x] 1.1 Confirm registration success returns a JWT.
- [x] 1.2 Confirm login API is deferred to a later proposal.
- [x] 1.3 Confirm validation rules for `username`, `nickname`, and `password`.
- [x] 1.4 Confirm standard error response shape uses `code`, `message`, and `details`.
- [x] 1.5 Confirm JWT expiration duration is 1 hour.
- [x] 1.6 Confirm registration response token field name is `access_token`.

## 2. Project Setup

- [x] 2.1 Add Maven dependencies for Spring WebMVC, Jackson, Spring Data JPA, Spring Security, Spring Security OAuth2 JOSE / Resource Server, H2, and `ulid-creator`.
- [x] 2.2 Configure H2 in-memory database and JPA settings in `application.yaml`.
- [x] 2.3 Configure JWT secret and token settings in application configuration.
- [x] 2.4 Run `./mvnw test` to verify the project still builds after dependency and configuration changes.

## 3. User Registration TDD

- [x] 3.1 Add a failing test for successful registration returning `201 Created` with `id`, `username`, `nickname`, `created_at`, and `updated_at`.
- [x] 3.2 Add a failing test that successful registration response includes a signed JWT in `access_token`.
- [x] 3.3 Add a failing test that registration response does not include plaintext password or password hash.
- [x] 3.4 Add a failing test that duplicate `username` returns `409 Conflict`.
- [x] 3.5 Add a failing test that duplicate non-empty `nickname` returns `409 Conflict`.
- [x] 3.6 Add a failing test that omitted or blank `nickname` is accepted.
- [x] 3.7 Add failing tests for missing `username`, missing `password`, invalid `username`, too-short `username`, too-long `username`, too-long `nickname`, and invalid `password` returning `400 Bad Request`.
- [x] 3.8 Add a failing test that validation and conflict errors include `code`, `message`, and `details`.
- [x] 3.9 Add a failing test that persisted password is hashed and does not equal the submitted password.
- [x] 3.10 Add a failing test that generated user IDs are ULID strings and preserve lexicographic creation order.
- [x] 3.11 Add a failing test that `createdAt` and `updatedAt` are set when a user is registered.

## 4. User Registration Implementation

- [x] 4.1 Implement user entity with ULID `id`, unique `username`, optional unique `nickname`, password hash field, `createdAt`, and `updatedAt`.
- [x] 4.2 Implement user repository with lookup by `username` and non-empty `nickname`.
- [x] 4.3 Implement DTOs for registration request and response body.
- [x] 4.4 Implement ULID generation component using `ulid-creator`.
- [x] 4.5 Implement password hashing using BCrypt.
- [x] 4.6 Implement registration service with username and nickname uniqueness checks and duplicate handling.
- [x] 4.7 Implement timestamp handling for user creation and future updates.
- [x] 4.8 Implement registration controller endpoint.
- [x] 4.9 Implement exception handling for validation and conflict responses using `code`, `message`, and `details`.
- [x] 4.10 Run registration-related tests and make them pass.

## 5. JWT TDD

- [x] 5.1 Add a failing test for issuing a signed JWT after successful registration.
- [x] 5.2 Add a failing test that issued JWT subject identifies the registered user.
- [x] 5.3 Add a failing test that issued JWT expires after 1 hour.
- [x] 5.4 Add a failing test that valid bearer token authenticates a protected API request.
- [x] 5.5 Add failing tests that missing or invalid bearer token returns `401 Unauthorized` for protected API requests.
- [x] 5.6 Add a failing test that registration endpoint remains public without a JWT.
- [x] 5.7 Add a failing test that this change does not add a login API.

## 6. JWT Implementation

- [x] 6.1 Implement JWT service for token creation and validation using Spring Security `JwtEncoder` / `JwtDecoder`.
- [x] 6.2 Implement Spring Security resource server configuration that permits registration without authentication.
- [x] 6.3 Implement bearer token validation for protected API requests using Spring Security resource server.
- [x] 6.4 Wire registration success response to include the signed JWT as `access_token`.
- [x] 6.5 Add a minimal protected test endpoint or test-only route if needed to verify JWT behavior.
- [x] 6.6 Run JWT-related tests and make them pass.

## 7. Final Verification

- [x] 7.1 Run `./mvnw test`.
- [x] 7.2 Review API responses to ensure password values are never exposed.
- [x] 7.3 Review OpenSpec requirements against implemented tests.
- [x] 7.4 Update task checkboxes as implementation work is completed.
