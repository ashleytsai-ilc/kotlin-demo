# user-login Specification

## Purpose
TBD - created by syncing change add-user-login-api. Update Purpose after archive.

## Requirements
### Requirement: Login user
The system SHALL provide a RESTful API for logging in an existing active user with `username` and `password`.

#### Scenario: Successful login
- **GIVEN** an active user has username `alice_001`
- **WHEN** the client submits the correct `username` and `password`
- **THEN** the system SHALL authenticate the user and return `200 OK`

#### Scenario: Login uses username and password
- **GIVEN** a client submits a login request
- **WHEN** the system evaluates the request
- **THEN** the system SHALL use `username` and `password` as the login credentials

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

### Requirement: Reject invalid credentials
The system SHALL reject invalid login attempts with a standard authentication error that does not reveal whether the username exists or has been soft deleted.

#### Scenario: Unknown username
- **GIVEN** no active user exists for the submitted `username`
- **WHEN** the client submits a login request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_CREDENTIALS`

#### Scenario: Soft-deleted username
- **GIVEN** a soft-deleted user exists for the submitted `username`
- **WHEN** the client submits a login request with the correct historical password
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_CREDENTIALS`

#### Scenario: Incorrect password
- **GIVEN** an active user has username `alice_001`
- **WHEN** the client submits an incorrect `password`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_CREDENTIALS`

#### Scenario: Missing username
- **GIVEN** a login request body does not include `username`
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_CREDENTIALS`

#### Scenario: Missing request body
- **GIVEN** a login request has no body
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `400 Bad Request`
- **THEN** the error response SHALL include code `VALIDATION_ERROR`

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
The system MUST NOT add logout-all-devices, session list, server-side session management, or multi-device session management as part of this change.

#### Scenario: Logout all devices is out of scope
- **GIVEN** this change is implemented
- **WHEN** API endpoints and server state are reviewed
- **THEN** no logout-all-devices capability SHALL be added by this change

#### Scenario: Session management is out of scope
- **GIVEN** this change is implemented
- **WHEN** API endpoints and server state are reviewed
- **THEN** no session list, server-side session, or multi-device session management SHALL be added by this change
