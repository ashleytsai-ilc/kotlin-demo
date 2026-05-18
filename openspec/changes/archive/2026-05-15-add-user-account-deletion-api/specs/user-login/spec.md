## MODIFIED Requirements

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
