## MODIFIED Requirements

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
