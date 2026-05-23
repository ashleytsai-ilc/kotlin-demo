## MODIFIED Requirements

### Requirement: Unique optional nickname
The system SHALL allow `nickname` to be omitted or null, but provided non-null nickname values MUST be non-blank, MUST NOT contain whitespace, MUST be unique among active users, and MUST NOT exceed 30 characters.

#### Scenario: Duplicate active nickname
- **GIVEN** an active user already has nickname `Ace`
- **WHEN** a client submits a registration request with nickname `Ace`
- **THEN** the system SHALL reject the request with `409 Conflict`
- **THEN** the system SHALL NOT create another active user with the same nickname

#### Scenario: Deleted nickname can be reused
- **GIVEN** a soft-deleted user already has nickname `Ace`
- **WHEN** a client submits a valid registration request with nickname `Ace`
- **THEN** the system SHALL create the user and return `201 Created`

#### Scenario: Multiple users without nickname
- **GIVEN** an existing user has no nickname
- **WHEN** another client submits a valid registration request with no nickname
- **THEN** the system SHALL create the user and return `201 Created`

#### Scenario: Null nickname is treated as omitted
- **GIVEN** a valid registration request has `nickname` set to null
- **WHEN** the client submits the request
- **THEN** the system SHALL create the user and return `201 Created`
- **THEN** the response SHALL NOT include `nickname`

#### Scenario: Blank nickname is rejected
- **GIVEN** a registration request has a blank `nickname`
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `400 Bad Request`
- **THEN** the error response SHALL include code `VALIDATION_ERROR`

#### Scenario: Nickname containing whitespace is rejected
- **GIVEN** a registration request has a `nickname` containing whitespace
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `400 Bad Request`
- **THEN** the error response SHALL include code `VALIDATION_ERROR`

### Requirement: Validate registration input
The system SHALL validate registration input before creating a user and SHALL NOT trim `username` or `nickname` values before validation. For accepted non-null `username` and `nickname` values, the accepted value SHALL equal the submitted value.

#### Scenario: Missing request body
- **GIVEN** a registration request has no body
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `400 Bad Request`
- **THEN** the error response SHALL include code `VALIDATION_ERROR`

#### Scenario: Missing username
- **GIVEN** a registration request has no `username`
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `400 Bad Request`

#### Scenario: Missing password
- **GIVEN** a registration request has no `password`
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `400 Bad Request`

#### Scenario: Username contains invalid characters
- **GIVEN** a registration request has a `username` containing characters other than letters, digits, or underscore
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `400 Bad Request`

#### Scenario: Username containing whitespace is rejected
- **GIVEN** a registration request has a `username` containing whitespace
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `400 Bad Request`
- **THEN** the error response SHALL include code `VALIDATION_ERROR`

#### Scenario: Username is too short
- **GIVEN** a registration request has a `username` shorter than 8 characters
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `400 Bad Request`

#### Scenario: Username is too long
- **GIVEN** a registration request has a `username` longer than 15 characters
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `400 Bad Request`

#### Scenario: Nickname is too long
- **GIVEN** a registration request has a `nickname` longer than 30 characters
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `400 Bad Request`

#### Scenario: Password is too short
- **GIVEN** a registration request has a `password` shorter than 8 characters
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `400 Bad Request`

#### Scenario: Password misses required character class
- **GIVEN** a registration request has a `password` missing an uppercase English letter, lowercase English letter, digit, or special symbol
- **WHEN** the client submits the request
- **THEN** the system SHALL reject the request with `400 Bad Request`
