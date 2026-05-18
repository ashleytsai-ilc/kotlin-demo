## MODIFIED Requirements

### Requirement: Validate logout credentials
The system SHALL validate both the submitted access token and refresh token, and SHALL require their subject to identify an active user before completing logout.

#### Scenario: Missing access token
- **GIVEN** a logout request has no Authorization bearer token
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `UNAUTHORIZED`

#### Scenario: Invalid access token
- **GIVEN** a logout request has an invalid Authorization bearer token
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `UNAUTHORIZED`

#### Scenario: Access token must be an access token
- **GIVEN** a client has a valid refresh token
- **WHEN** the client submits it as the logout Authorization bearer token
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `UNAUTHORIZED`

#### Scenario: Token pair revoked by account deletion cannot logout
- **GIVEN** a user has deleted their own account successfully
- **AND** the submitted access token and submitted refresh token were revoked by account deletion
- **WHEN** the client submits logout with that token pair
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `UNAUTHORIZED`

#### Scenario: Missing refresh token
- **GIVEN** a logout request body does not include `refresh_token`
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

#### Scenario: Missing request body
- **GIVEN** a logout request has no body
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `400 Bad Request`
- **THEN** the error response SHALL include code `VALIDATION_ERROR`

#### Scenario: Blank refresh token
- **GIVEN** a logout request body has a blank `refresh_token`
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

#### Scenario: Invalid refresh token
- **GIVEN** a logout request body has an invalid refresh token
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

#### Scenario: Expired refresh token
- **GIVEN** a logout request body has an expired refresh token
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

#### Scenario: Tokens must belong to the same user
- **GIVEN** a logout request has a valid access token for one user
- **WHEN** the client submits a valid refresh token for a different user
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`
