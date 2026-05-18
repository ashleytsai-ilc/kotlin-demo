## MODIFIED Requirements

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
