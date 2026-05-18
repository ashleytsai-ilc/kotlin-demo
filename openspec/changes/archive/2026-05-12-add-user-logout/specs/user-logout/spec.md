## ADDED Requirements

### Requirement: Logout current user session
The system SHALL provide a RESTful API for logging out the current user session.

#### Scenario: Successful logout
- **GIVEN** a client has a valid access token and its matching refresh token
- **WHEN** the client submits `POST /api/users/logout` with `Authorization: Bearer <access_token>` and `refresh_token`
- **THEN** the system SHALL revoke the submitted access token
- **THEN** the system SHALL revoke the submitted refresh token
- **THEN** the system SHALL return `204 No Content`
- **THEN** the response SHALL NOT include a body

#### Scenario: Repeated logout is idempotent
- **GIVEN** a client has already logged out with an access token and refresh token pair
- **WHEN** the client submits the same logout request again before the tokens expire
- **THEN** the system SHALL return `204 No Content`
- **THEN** the response SHALL NOT include a body

### Requirement: Validate logout credentials
The system SHALL validate both the submitted access token and refresh token before completing logout.

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

#### Scenario: Missing refresh token
- **GIVEN** a logout request body does not include `refresh_token`
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

#### Scenario: Missing request body
- **GIVEN** a logout request has no body
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

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

### Requirement: Keep logout scoped to current session
The system SHALL limit logout to the submitted access token and refresh token pair.

#### Scenario: Logout all devices is out of scope
- **GIVEN** this change is implemented
- **WHEN** API endpoints and server state are reviewed
- **THEN** no logout-all-devices capability SHALL be added by this change

#### Scenario: Session management remains out of scope
- **GIVEN** this change is implemented
- **WHEN** API endpoints and server state are reviewed
- **THEN** no session list or server-side session management API SHALL be added by this change

#### Scenario: Refresh token reuse detection remains out of scope
- **GIVEN** this change is implemented
- **WHEN** token revocation behavior is reviewed
- **THEN** no refresh token reuse detection or token family invalidation capability SHALL be added by this change
