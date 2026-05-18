## ADDED Requirements

### Requirement: Identify JWTs for revocation
The system SHALL assign a unique token identifier to each issued access and refresh JWT.

#### Scenario: Access token has token id
- **GIVEN** an access token is issued
- **WHEN** the token claims are decoded by the server
- **THEN** the token SHALL include a token id claim

#### Scenario: Refresh token has token id
- **GIVEN** a refresh token is issued
- **WHEN** the token claims are decoded by the server
- **THEN** the token SHALL include a token id claim

## MODIFIED Requirements

### Requirement: Validate bearer token
The system SHALL validate JWT bearer tokens for protected API requests.

#### Scenario: Valid bearer token
- **GIVEN** a client has a valid JWT
- **WHEN** the client calls a protected API with `Authorization: Bearer <token>`
- **THEN** the system SHALL accept the request as authenticated

#### Scenario: Missing bearer token
- **GIVEN** a protected API requires authentication
- **WHEN** the client calls the API without an `Authorization` header
- **THEN** the system SHALL reject the request with `401 Unauthorized`

#### Scenario: Invalid bearer token
- **GIVEN** a protected API requires authentication
- **WHEN** the client calls the API with an invalid JWT
- **THEN** the system SHALL reject the request with `401 Unauthorized`

#### Scenario: Revoked bearer token
- **GIVEN** a protected API requires authentication
- **WHEN** the client calls the API with a revoked access token
- **THEN** the system SHALL reject the request with `401 Unauthorized`
