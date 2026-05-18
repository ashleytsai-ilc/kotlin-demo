# user-registration Specification

## Purpose
TBD - created by archiving change add-user-registration-api. Update Purpose after archive.
## Requirements
### Requirement: Register user
The system SHALL provide a RESTful API for registering a user with `username`, optional `nickname`, and `password`.

#### Scenario: Successful registration
- **GIVEN** no existing user has the requested `username`
- **WHEN** a client submits a valid registration request
- **THEN** the system SHALL create the user and return `201 Created`

#### Scenario: Response includes user fields
- **GIVEN** a user is registered successfully
- **WHEN** the system returns the registration response
- **THEN** the response SHALL include `id`, `username`, `nickname`, `created_at`, and `updated_at`

#### Scenario: Registration response includes tokens
- **GIVEN** a user is registered successfully
- **WHEN** the system returns the registration response
- **THEN** the response SHALL include `access_token`
- **THEN** the response SHALL include `refresh_token`

#### Scenario: Register without nickname
- **GIVEN** a valid registration request has no `nickname`
- **WHEN** the client submits the request
- **THEN** the system SHALL create the user and return `201 Created`

#### Scenario: Response excludes password
- **GIVEN** a user is registered successfully
- **WHEN** the system returns the registration response
- **THEN** the response MUST NOT include `password` or password hash

### Requirement: Unique username
The system SHALL require `username` to be unique among active users.

#### Scenario: Duplicate active username
- **GIVEN** an active user already has username `alice`
- **WHEN** a client submits a registration request with username `alice`
- **THEN** the system SHALL reject the request with `409 Conflict`
- **THEN** the system SHALL NOT create another active user with the same username

#### Scenario: Deleted username can be reused
- **GIVEN** a soft-deleted user already has username `alice`
- **WHEN** a client submits a valid registration request with username `alice`
- **THEN** the system SHALL create the user and return `201 Created`

### Requirement: Unique optional nickname
The system SHALL allow `nickname` to be omitted or blank, but provided nickname values MUST be unique among active users and MUST NOT exceed 30 characters.

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

#### Scenario: Blank nickname is treated as omitted
- **GIVEN** a registration request has a blank `nickname`
- **WHEN** the client submits the request
- **THEN** the system SHALL create the user and return `201 Created`
- **THEN** the response SHALL NOT include `nickname`

### Requirement: Validate registration input
The system SHALL validate registration input before creating a user.

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

### Requirement: Store password securely
The system SHALL store only a password hash and MUST NOT persist plaintext passwords.

#### Scenario: Password is hashed
- **GIVEN** a client registers with password `secret123`
- **WHEN** the user record is saved
- **THEN** the persisted password value SHALL be a secure hash
- **THEN** the persisted password value MUST NOT equal `secret123`

### Requirement: Use ordered non-sequential user IDs
The system SHALL assign user IDs that are not database auto-increment values and preserve natural creation-time ordering.

#### Scenario: User ID is generated
- **GIVEN** a valid registration request
- **WHEN** the system creates the user
- **THEN** the system SHALL assign a ULID string as the user `id`

#### Scenario: User IDs preserve ordering
- **GIVEN** two users are registered at different times
- **WHEN** their IDs are compared lexicographically
- **THEN** the earlier user's ULID SHALL sort before the later user's ULID

### Requirement: Persist users in H2
The system SHALL persist registered users in an H2 in-memory database during application runtime.

#### Scenario: Registered user is persisted
- **GIVEN** a user is registered successfully
- **WHEN** the repository looks up that user by username
- **THEN** the system SHALL return the persisted user record

### Requirement: Track user timestamps
The system SHALL track creation and update timestamps for user records.

#### Scenario: Timestamps are set on registration
- **GIVEN** a valid registration request
- **WHEN** the system creates the user
- **THEN** the persisted user record SHALL include `createdAt`
- **THEN** the persisted user record SHALL include `updatedAt`

#### Scenario: Registration response includes timestamps
- **GIVEN** a user is registered successfully
- **WHEN** the system returns the registration response
- **THEN** the response SHALL include `created_at`
- **THEN** the response SHALL include `updated_at`

#### Scenario: Created and updated timestamps are initialized consistently
- **GIVEN** a valid registration request
- **WHEN** the system creates the user
- **THEN** `createdAt` and `updatedAt` SHALL represent the initial creation time

### Requirement: Return standard error response
The system SHALL return validation and conflict errors with a standard JSON error shape containing `code`, `message`, and `details`.

#### Scenario: Validation error shape
- **GIVEN** a registration request fails validation
- **WHEN** the system returns the error response
- **THEN** the response SHALL include `code`
- **THEN** the response SHALL include `message`
- **THEN** the response SHALL include `details`

#### Scenario: Conflict error shape
- **GIVEN** a registration request conflicts with an existing unique value
- **WHEN** the system returns the error response
- **THEN** the response SHALL include `code`
- **THEN** the response SHALL include `message`
- **THEN** the response SHALL include `details`
