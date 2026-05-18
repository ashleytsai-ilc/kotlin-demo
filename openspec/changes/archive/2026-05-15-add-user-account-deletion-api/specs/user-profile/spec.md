## MODIFIED Requirements

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
