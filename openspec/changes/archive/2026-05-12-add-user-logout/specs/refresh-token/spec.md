## MODIFIED Requirements

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

#### Scenario: Revoked refresh token
- **GIVEN** a refresh request body has a revoked refresh token
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

## ADDED Requirements

### Requirement: Keep refresh token revocation scoped
The system SHALL limit refresh token revocation to explicitly submitted tokens.

#### Scenario: No token family invalidation is added
- **GIVEN** this change is implemented
- **WHEN** refresh token revocation behavior is reviewed
- **THEN** no token family invalidation capability SHALL be added by this change

#### Scenario: Old non-revoked refresh token remains usable until expiration
- **GIVEN** a refresh token is not revoked
- **WHEN** the same refresh token is submitted again before it expires
- **THEN** the system SHALL evaluate it as a valid refresh token unless another validation rule rejects it

### Requirement: Keep refresh independent from access token authentication
The system SHALL use only the submitted `refresh_token` as the credential for the refresh endpoint.

#### Scenario: Authorization header does not block refresh
- **GIVEN** a client has a valid refresh token
- **AND** the refresh request includes an expired bearer access token in the Authorization header
- **WHEN** the client submits `POST /api/users/tokens/refresh`
- **THEN** the system SHALL ignore the Authorization header for refresh authentication
- **THEN** the system SHALL return `200 OK`
- **THEN** the response SHALL include `access_token`
- **THEN** the response SHALL include `refresh_token`

## REMOVED Requirements

### Requirement: Keep refresh token stateless for this change
The system SHALL NOT add refresh token revocation, token denylist, server-side session management, or multi-device session management as part of this change.

**Reason**: Logout requires refresh token revocation, so refresh tokens can no longer remain fully stateless.
**Migration**: Use revoked-token persistence to reject explicitly revoked refresh tokens while keeping logout-all-devices, session management, and token family invalidation out of scope.
