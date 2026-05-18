## MODIFIED Requirements

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
