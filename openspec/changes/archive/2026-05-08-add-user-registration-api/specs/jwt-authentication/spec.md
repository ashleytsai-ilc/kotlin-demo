## ADDED Requirements

### Requirement: Issue JWT after registration
The system SHALL issue a signed JWT when user registration succeeds.

#### Scenario: JWT is issued after registration
- **GIVEN** a user registration request succeeds
- **WHEN** the system returns the registration response
- **THEN** the response SHALL include a signed JWT in `access_token`

#### Scenario: JWT contains subject
- **GIVEN** a JWT is issued for a newly registered user
- **WHEN** the token payload is decoded by the server
- **THEN** the token subject SHALL identify the registered user

#### Scenario: JWT expires after one hour
- **GIVEN** a JWT is issued for a newly registered user
- **WHEN** the token claims are decoded by the server
- **THEN** the token expiration SHALL be one hour after issuance

### Requirement: Defer login API
The system MUST NOT add a login API as part of this change.

#### Scenario: Login API is out of scope
- **GIVEN** this change is implemented
- **WHEN** API endpoints are reviewed
- **THEN** no login endpoint SHALL be added by this change

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

### Requirement: Keep registration public
The system SHALL allow unauthenticated clients to call the registration API.

#### Scenario: Register without token
- **GIVEN** a client does not have a JWT
- **WHEN** the client submits a valid registration request
- **THEN** the system SHALL allow the registration request to proceed

### Requirement: Configure JWT signing secret
The system SHALL load the JWT signing secret from application configuration.

#### Scenario: Signing secret is configured
- **GIVEN** the application starts with a JWT signing secret configured
- **WHEN** the system issues or validates JWTs
- **THEN** the system SHALL use the configured signing secret
