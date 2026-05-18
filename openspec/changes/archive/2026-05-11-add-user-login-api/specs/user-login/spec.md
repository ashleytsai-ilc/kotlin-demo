## ADDED Requirements

### Requirement: Login user
The system SHALL provide a RESTful API for logging in an existing user with `username` and `password`.

#### Scenario: Successful login
- **GIVEN** an existing user has username `alice_001`
- **WHEN** the client submits the correct `username` and `password`
- **THEN** the system SHALL authenticate the user and return `200 OK`

#### Scenario: Login uses username and password
- **GIVEN** a client submits a login request
- **WHEN** the system evaluates the request
- **THEN** the system SHALL use `username` and `password` as the login credentials

### Requirement: Login response includes token and profile
The system SHALL return an access token and user profile when login succeeds.

#### Scenario: Successful login response
- **GIVEN** a user logs in successfully
- **WHEN** the system returns the login response
- **THEN** the response SHALL include `access_token`
- **THEN** the response SHALL include `id`, `username`, `nickname`, `created_at`, and `updated_at`

#### Scenario: Login response excludes password
- **GIVEN** a user logs in successfully
- **WHEN** the system returns the login response
- **THEN** the response MUST NOT include `password` or password hash

### Requirement: Reject invalid credentials
The system SHALL reject invalid login attempts with a standard authentication error that does not reveal whether the username exists.

#### Scenario: Unknown username
- **GIVEN** no user exists for the submitted `username`
- **WHEN** the client submits a login request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_CREDENTIALS`

#### Scenario: Incorrect password
- **GIVEN** an existing user has username `alice_001`
- **WHEN** the client submits an incorrect `password`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_CREDENTIALS`

#### Scenario: Missing username
- **GIVEN** a login request body does not include `username`
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_CREDENTIALS`

#### Scenario: Missing password
- **GIVEN** a login request body does not include `password`
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_CREDENTIALS`

### Requirement: Keep login public
The system SHALL allow unauthenticated clients to call the login API.

#### Scenario: Login without bearer token
- **GIVEN** a client does not have a JWT
- **WHEN** the client submits valid login credentials
- **THEN** the system SHALL allow the login request to proceed

### Requirement: Defer session management
The system MUST NOT add refresh token, logout, server-side session, or multi-device session management as part of this change.

#### Scenario: Refresh token is out of scope
- **GIVEN** this change is implemented
- **WHEN** API endpoints and responses are reviewed
- **THEN** no refresh token capability SHALL be added by this change

#### Scenario: Logout and session management are out of scope
- **GIVEN** this change is implemented
- **WHEN** API endpoints and server state are reviewed
- **THEN** no logout endpoint, server-side session, or multi-device session management SHALL be added by this change
