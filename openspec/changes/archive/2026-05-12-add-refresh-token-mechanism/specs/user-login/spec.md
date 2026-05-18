## MODIFIED Requirements

### Requirement: Login response includes token and profile
The system SHALL return access and refresh tokens with the user profile when login succeeds.

#### Scenario: Successful login response
- **GIVEN** a user logs in successfully
- **WHEN** the system returns the login response
- **THEN** the response SHALL include `access_token`
- **THEN** the response SHALL include `refresh_token`
- **THEN** the response SHALL include `id`, `username`, `nickname`, `created_at`, and `updated_at`

#### Scenario: Login response excludes password
- **GIVEN** a user logs in successfully
- **WHEN** the system returns the login response
- **THEN** the response MUST NOT include `password` or password hash

### Requirement: Defer session management
The system MUST NOT add logout, refresh-token revocation, server-side session, or multi-device session management as part of this change.

#### Scenario: Logout and session management are out of scope
- **GIVEN** this change is implemented
- **WHEN** API endpoints and server state are reviewed
- **THEN** no logout endpoint, server-side session, or multi-device session management SHALL be added by this change

#### Scenario: Refresh token revocation is out of scope
- **GIVEN** this change is implemented
- **WHEN** API endpoints, persistence, and server state are reviewed
- **THEN** no refresh-token revocation capability SHALL be added by this change
