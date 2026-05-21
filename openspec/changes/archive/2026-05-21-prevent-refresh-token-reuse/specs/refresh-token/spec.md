## MODIFIED Requirements

### Requirement: Refresh access token
The system SHALL provide a RESTful API for renewing tokens with a valid refresh token, and SHALL consume the submitted refresh token after a successful refresh.

#### Scenario: Successful refresh
- **GIVEN** a client has a valid refresh token
- **WHEN** the client submits `POST /api/users/tokens/refresh` with `refresh_token`
- **THEN** the system SHALL return `200 OK`
- **THEN** the response SHALL include `access_token`
- **THEN** the response SHALL include `refresh_token`
- **THEN** the submitted refresh token SHALL be revoked before the response is completed

#### Scenario: Refresh response excludes profile
- **GIVEN** a client refreshes tokens successfully
- **WHEN** the system returns the refresh response
- **THEN** the response SHALL NOT include `id`, `username`, `nickname`, `created_at`, or `updated_at`

#### Scenario: Reusing consumed refresh token is rejected
- **GIVEN** a client has refreshed tokens successfully
- **WHEN** the client submits the same original refresh token again before it expires
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

### Requirement: Validate refresh token
The system SHALL validate refresh tokens and require their subject to identify an active user before issuing renewed tokens.

#### Scenario: Missing refresh token
- **GIVEN** a refresh request body does not include `refresh_token`
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

#### Scenario: Missing request body
- **GIVEN** a refresh request has no body
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `400 Bad Request`
- **THEN** the error response SHALL include code `VALIDATION_ERROR`

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

#### Scenario: Refresh token user must be active
- **GIVEN** a refresh token subject identifies a soft-deleted user
- **WHEN** the client submits the refresh request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

#### Scenario: Revoked refresh token
- **GIVEN** a refresh request body has a revoked refresh token
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

#### Scenario: Consumed refresh token
- **GIVEN** a refresh request body has a refresh token that was consumed by a prior successful refresh
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

### Requirement: Keep refresh token revocation scoped
The system SHALL limit refresh token revocation to explicitly submitted refresh tokens and SHALL NOT invalidate a token family by this change.

#### Scenario: No token family invalidation is added
- **GIVEN** this change is implemented
- **WHEN** refresh token revocation behavior is reviewed
- **THEN** no token family invalidation capability SHALL be added by this change

#### Scenario: Newly issued refresh token remains usable
- **GIVEN** a client refreshes tokens successfully
- **WHEN** the client submits the newly issued refresh token before it expires
- **THEN** the system SHALL evaluate the newly issued refresh token as valid unless another validation rule rejects it
