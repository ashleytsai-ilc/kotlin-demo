## ADDED Requirements

### Requirement: Refresh access token
The system SHALL provide a RESTful API for renewing tokens with a valid refresh token.

#### Scenario: Successful refresh
- **GIVEN** a client has a valid refresh token
- **WHEN** the client submits `POST /api/users/tokens/refresh` with `refresh_token`
- **THEN** the system SHALL return `200 OK`
- **THEN** the response SHALL include `access_token`
- **THEN** the response SHALL include `refresh_token`

#### Scenario: Refresh response excludes profile
- **GIVEN** a client refreshes tokens successfully
- **WHEN** the system returns the refresh response
- **THEN** the response SHALL NOT include `id`, `username`, `nickname`, `created_at`, or `updated_at`

### Requirement: Validate refresh token
The system SHALL validate refresh tokens before issuing renewed tokens.

#### Scenario: Missing refresh token
- **GIVEN** a refresh request body does not include `refresh_token`
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

#### Scenario: Missing request body
- **GIVEN** a refresh request has no body
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

#### Scenario: Blank refresh token
- **GIVEN** a refresh request body has a blank `refresh_token`
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

#### Scenario: Invalid refresh token
- **GIVEN** a refresh request body has an invalid JWT
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

#### Scenario: Expired refresh token
- **GIVEN** a refresh request body has an expired refresh token
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

#### Scenario: Access token cannot refresh
- **GIVEN** a client has a valid access token
- **WHEN** the client submits it as `refresh_token`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

#### Scenario: Refresh token user must exist
- **GIVEN** a refresh token subject does not identify an existing user
- **WHEN** the client submits the refresh request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

### Requirement: Keep refresh token stateless for this change
The system SHALL NOT add refresh token revocation, token denylist, server-side session management, or multi-device session management as part of this change.

#### Scenario: No token revocation is added
- **GIVEN** this change is implemented
- **WHEN** API endpoints, persistence, and server state are reviewed
- **THEN** no refresh token revocation capability SHALL be added by this change

#### Scenario: Old refresh token remains usable until expiration
- **GIVEN** a refresh token is used successfully
- **WHEN** the same refresh token is submitted again before it expires
- **THEN** the system SHALL evaluate it as a valid refresh token unless another validation rule rejects it

#### Scenario: Logout remains out of scope
- **GIVEN** this change is implemented
- **WHEN** API endpoints and server state are reviewed
- **THEN** no logout endpoint SHALL be added by this change
