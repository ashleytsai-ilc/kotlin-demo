## MODIFIED Requirements

### Requirement: Issue JWT after registration
The system SHALL issue signed access and refresh JWTs when user registration succeeds.

#### Scenario: JWT is issued after registration
- **GIVEN** a user registration request succeeds
- **WHEN** the system returns the registration response
- **THEN** the response SHALL include a signed JWT in `access_token`

#### Scenario: Refresh JWT is issued after registration
- **GIVEN** a user registration request succeeds
- **WHEN** the system returns the registration response
- **THEN** the response SHALL include a signed JWT in `refresh_token`

#### Scenario: JWT contains subject
- **GIVEN** a JWT is issued for a newly registered user
- **WHEN** the token payload is decoded by the server
- **THEN** the token subject SHALL identify the registered user

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

#### Scenario: Refresh JWT is issued after login
- **GIVEN** a user login request succeeds
- **WHEN** the system returns the login response
- **THEN** the response SHALL include a signed JWT in `refresh_token`

#### Scenario: Login JWT contains subject
- **GIVEN** a JWT is issued for a logged-in user
- **WHEN** the token payload is decoded by the server
- **THEN** the token subject SHALL identify the logged-in user

#### Scenario: Login access JWT uses existing expiration
- **GIVEN** an access JWT is issued for a logged-in user
- **WHEN** the token claims are decoded by the server
- **THEN** the token expiration SHALL use the existing access token expiration setting

#### Scenario: Login refresh JWT uses refresh expiration
- **GIVEN** a refresh JWT is issued for a logged-in user
- **WHEN** the token claims are decoded by the server
- **THEN** the token expiration SHALL use the configured refresh token expiration setting

## ADDED Requirements

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
