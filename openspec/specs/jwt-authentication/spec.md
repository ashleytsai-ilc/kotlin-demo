# jwt-authentication Specification

## Purpose
TBD - created by archiving change add-user-registration-api. Update Purpose after archive.
## Requirements
### Requirement: Issue JWT after registration
The system SHALL issue signed access and refresh JWTs when user registration succeeds.

#### Scenario: JWT is issued after registration
- **GIVEN** a user registration request succeeds
- **WHEN** the system returns the registration response
- **THEN** the response SHALL include a signed JWT in `access_token`

#### Scenario: JWT contains subject
- **GIVEN** a JWT is issued for a newly registered user
- **WHEN** the token payload is decoded by the server
- **THEN** the token subject SHALL identify the registered user

#### Scenario: Refresh JWT is issued after registration
- **GIVEN** a user registration request succeeds
- **WHEN** the system returns the registration response
- **THEN** the response SHALL include a signed JWT in `refresh_token`

#### Scenario: Registration access JWT uses existing expiration
- **GIVEN** an access JWT is issued for a newly registered user
- **WHEN** the token claims are decoded by the server
- **THEN** the token expiration SHALL use the existing access token expiration setting

#### Scenario: Registration refresh JWT uses refresh expiration
- **GIVEN** a refresh JWT is issued for a newly registered user
- **WHEN** the token claims are decoded by the server
- **THEN** the token expiration SHALL use the configured refresh token expiration setting

### Requirement: Issue JWT after login
The system SHALL issue signed access and refresh JWTs when user login succeeds.

#### Scenario: JWT is issued after login
- **GIVEN** a user login request succeeds
- **WHEN** the system returns the login response
- **THEN** the response SHALL include a signed JWT in `access_token`

#### Scenario: Login JWT contains subject
- **GIVEN** a JWT is issued for a logged-in user
- **WHEN** the token payload is decoded by the server
- **THEN** the token subject SHALL identify the logged-in user

#### Scenario: Refresh JWT is issued after login
- **GIVEN** a user login request succeeds
- **WHEN** the system returns the login response
- **THEN** the response SHALL include a signed JWT in `refresh_token`

#### Scenario: Login access JWT uses existing expiration
- **GIVEN** an access JWT is issued for a logged-in user
- **WHEN** the token claims are decoded by the server
- **THEN** the token expiration SHALL use the existing access token expiration setting

#### Scenario: Login refresh JWT uses refresh expiration
- **GIVEN** a refresh JWT is issued for a logged-in user
- **WHEN** the token claims are decoded by the server
- **THEN** the token expiration SHALL use the configured refresh token expiration setting

### Requirement: Distinguish JWT token types
The system SHALL distinguish access tokens from refresh tokens using a JWT claim.

#### Scenario: Access token has access type
- **GIVEN** an access token is issued
- **WHEN** the token claims are decoded by the server
- **THEN** the token SHALL include a token type claim identifying it as an access token

#### Scenario: Refresh token has refresh type
- **GIVEN** a refresh token is issued
- **WHEN** the token claims are decoded by the server
- **THEN** the token SHALL include a token type claim identifying it as a refresh token

#### Scenario: Refresh token cannot authenticate protected APIs
- **GIVEN** a refresh token is issued
- **WHEN** the client submits it as a bearer token to a protected API
- **THEN** the system SHALL reject the request with `401 Unauthorized`

### Requirement: Identify JWTs for revocation
The system SHALL assign a unique token identifier to each issued access and refresh JWT.

#### Scenario: Access token has token id
- **GIVEN** an access token is issued
- **WHEN** the token claims are decoded by the server
- **THEN** the token SHALL include a token id claim

#### Scenario: Refresh token has token id
- **GIVEN** a refresh token is issued
- **WHEN** the token claims are decoded by the server
- **THEN** the token SHALL include a token id claim

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

#### Scenario: Revoked bearer token
- **GIVEN** a protected API requires authentication
- **WHEN** the client calls the API with a revoked access token
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
