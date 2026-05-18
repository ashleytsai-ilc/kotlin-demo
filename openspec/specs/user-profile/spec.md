# user-profile Specification

## Purpose
Define authenticated user profile update behavior for changing the current user's nickname.

## Requirements
### Requirement: Update current user profile
The system SHALL provide a RESTful API for an authenticated user to update their own profile nickname.

#### Scenario: Successful nickname update
- **GIVEN** a client has a valid access token for an existing user
- **WHEN** the client submits `PATCH /api/users/me` with a valid `nickname`
- **THEN** the system SHALL update that user's nickname
- **THEN** the system SHALL return `200 OK`

#### Scenario: Update is scoped to current user
- **GIVEN** two users exist
- **WHEN** one user submits `PATCH /api/users/me` with a valid `nickname`
- **THEN** the system SHALL update only the user identified by the access token
- **THEN** the system SHALL NOT update any other user's nickname

#### Scenario: Same nickname update is accepted
- **GIVEN** a user already has nickname `Ace`
- **WHEN** the user submits `PATCH /api/users/me` with nickname `Ace`
- **THEN** the system SHALL return `200 OK`
- **THEN** the user's nickname SHALL remain `Ace`
- **THEN** the user's `updated_at` SHALL remain unchanged

### Requirement: Require access token for profile update
The system SHALL require a valid bearer access token whose subject identifies an active user to update the current user profile.

#### Scenario: Missing access token
- **GIVEN** a profile update request has no Authorization bearer token
- **WHEN** the client submits `PATCH /api/users/me`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `UNAUTHORIZED`

#### Scenario: Invalid access token
- **GIVEN** a profile update request has an invalid Authorization bearer token
- **WHEN** the client submits `PATCH /api/users/me`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `UNAUTHORIZED`

#### Scenario: Refresh token cannot update profile
- **GIVEN** a client has a valid refresh token
- **WHEN** the client submits it as the Authorization bearer token for `PATCH /api/users/me`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `UNAUTHORIZED`

#### Scenario: Revoked access token cannot update profile
- **GIVEN** a client's access token has been revoked
- **WHEN** the client submits `PATCH /api/users/me` with that access token
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `UNAUTHORIZED`

#### Scenario: Token subject user no longer exists
- **GIVEN** a client has a valid access token whose subject no longer matches an existing user
- **WHEN** the client submits `PATCH /api/users/me` with that access token
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `UNAUTHORIZED`

#### Scenario: Token subject user is soft deleted
- **GIVEN** a client has a valid access token whose subject identifies a soft-deleted user
- **WHEN** the client submits `PATCH /api/users/me` with that access token
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `UNAUTHORIZED`

### Requirement: Only nickname is mutable
The system SHALL allow profile update requests to change only `nickname`; unknown fields and immutable fields MUST be ignored and MUST NOT cause a validation error.

#### Scenario: Username is not updated
- **GIVEN** a client has a valid access token for an existing user
- **WHEN** the client submits `PATCH /api/users/me` with `username` and `nickname`
- **THEN** the system SHALL update `nickname`
- **THEN** the system SHALL NOT update `username`
- **THEN** the system SHALL return `200 OK`

#### Scenario: Password is not updated
- **GIVEN** a client has a valid access token for an existing user
- **WHEN** the client submits `PATCH /api/users/me` with `password` and `nickname`
- **THEN** the system SHALL update `nickname`
- **THEN** the system SHALL NOT update the user's password hash
- **THEN** the system SHALL return `200 OK`

#### Scenario: User id is not updated
- **GIVEN** a client has a valid access token for an existing user
- **WHEN** the client submits `PATCH /api/users/me` with `id` and `nickname`
- **THEN** the system SHALL update `nickname`
- **THEN** the system SHALL NOT update the user's `id`
- **THEN** the system SHALL return `200 OK`

#### Scenario: Unknown fields are ignored
- **GIVEN** a client has a valid access token for an existing user
- **WHEN** the client submits `PATCH /api/users/me` with an unknown field and `nickname`
- **THEN** the system SHALL update `nickname`
- **THEN** the system SHALL ignore the unknown field
- **THEN** the system SHALL return `200 OK`

### Requirement: Validate nickname update
The system SHALL require profile update request bodies to include `nickname`, SHALL treat blank `nickname` values as no nickname, SHALL reject null or omitted `nickname`, and SHALL reject non-empty nickname values longer than 30 characters.

#### Scenario: Missing request body
- **GIVEN** a client has a valid access token for an existing user
- **WHEN** the client submits `PATCH /api/users/me` without a request body
- **THEN** the system SHALL reject the request with `400 Bad Request`
- **THEN** the error response SHALL include code `VALIDATION_ERROR`

#### Scenario: Blank nickname clears nickname
- **GIVEN** a user has an existing nickname
- **WHEN** the user submits `PATCH /api/users/me` with a blank `nickname`
- **THEN** the system SHALL clear the user's nickname
- **THEN** the system SHALL return `200 OK`

#### Scenario: Null nickname is rejected
- **GIVEN** a user has an existing nickname
- **WHEN** the user submits `PATCH /api/users/me` with `nickname` set to null
- **THEN** the system SHALL reject the request with `400 Bad Request`
- **THEN** the error response SHALL include code `VALIDATION_ERROR`
- **THEN** the user's nickname SHALL remain unchanged

#### Scenario: Omitted nickname is rejected
- **GIVEN** a user has an existing nickname
- **WHEN** the user submits `PATCH /api/users/me` with a request body that omits `nickname`
- **THEN** the system SHALL reject the request with `400 Bad Request`
- **THEN** the error response SHALL include code `VALIDATION_ERROR`
- **THEN** the user's nickname SHALL remain unchanged

#### Scenario: Nickname is too long
- **GIVEN** a client has a valid access token for an existing user
- **WHEN** the client submits `PATCH /api/users/me` with a `nickname` longer than 30 characters
- **THEN** the system SHALL reject the request with `400 Bad Request`
- **THEN** the error response SHALL include code `VALIDATION_ERROR`

### Requirement: Enforce unique nickname on update
The system SHALL require non-empty updated nickname values to be unique among active users.

#### Scenario: Duplicate active nickname
- **GIVEN** another active user already has nickname `Ace`
- **WHEN** a client submits `PATCH /api/users/me` with nickname `Ace`
- **THEN** the system SHALL reject the request with `409 Conflict`
- **THEN** the error response SHALL include code `NICKNAME_ALREADY_EXISTS`
- **THEN** the system SHALL NOT update the user's nickname

#### Scenario: Deleted nickname can be reused on profile update
- **GIVEN** a soft-deleted user already has nickname `Ace`
- **WHEN** an active user submits `PATCH /api/users/me` with nickname `Ace`
- **THEN** the system SHALL update the active user's nickname
- **THEN** the system SHALL return `200 OK`

#### Scenario: Multiple active users without nickname after update
- **GIVEN** another active user has no nickname
- **WHEN** a client submits `PATCH /api/users/me` with a blank `nickname`
- **THEN** the system SHALL clear the user's nickname
- **THEN** the system SHALL return `200 OK`

### Requirement: Return updated user profile
The system SHALL return the updated user profile when profile update succeeds, and the response MUST include the `nickname` field. When the user's nickname has been cleared, the response `nickname` value MUST be an empty string.

#### Scenario: Successful update response includes profile fields
- **GIVEN** a user updates their nickname successfully
- **WHEN** the system returns the profile update response
- **THEN** the response SHALL include `id`
- **THEN** the response SHALL include `username`
- **THEN** the response SHALL include `nickname`
- **THEN** the response SHALL include `created_at`
- **THEN** the response SHALL include `updated_at`

#### Scenario: Cleared nickname response includes empty nickname
- **GIVEN** a user clears their nickname successfully
- **WHEN** the system returns the profile update response
- **THEN** the response SHALL include `nickname`
- **THEN** the `nickname` value SHALL be an empty string

#### Scenario: Successful update response excludes sensitive fields
- **GIVEN** a user updates their nickname successfully
- **WHEN** the system returns the profile update response
- **THEN** the response MUST NOT include `password` or password hash
- **THEN** the response MUST NOT include `access_token`
- **THEN** the response MUST NOT include `refresh_token`

### Requirement: Keep existing auth success and token contracts unchanged
The system SHALL add profile update behavior without changing existing authentication success responses, token issuance, or token validation contracts.

#### Scenario: Existing auth success and token contracts remain unchanged
- **GIVEN** this change is implemented
- **WHEN** register, login, refresh, and logout APIs are reviewed
- **THEN** their success responses, token issuance, and token validation contracts SHALL remain unchanged
