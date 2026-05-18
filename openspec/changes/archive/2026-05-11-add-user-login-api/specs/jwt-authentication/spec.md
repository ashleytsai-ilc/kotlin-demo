## ADDED Requirements

### Requirement: Issue JWT after login
The system SHALL issue a signed JWT when user login succeeds.

#### Scenario: JWT is issued after login
- **GIVEN** a user login request succeeds
- **WHEN** the system returns the login response
- **THEN** the response SHALL include a signed JWT in `access_token`

#### Scenario: Login JWT contains subject
- **GIVEN** a JWT is issued for a logged-in user
- **WHEN** the token payload is decoded by the server
- **THEN** the token subject SHALL identify the logged-in user

#### Scenario: Login JWT uses existing expiration
- **GIVEN** a JWT is issued for a logged-in user
- **WHEN** the token claims are decoded by the server
- **THEN** the token expiration SHALL use the existing access token expiration setting

## REMOVED Requirements

### Requirement: Defer login API
**Reason**: This change introduces the login API that was intentionally deferred during user registration.
**Migration**: Use the new `user-login` capability and `POST /api/users/login`.
The system MUST NOT add a login API as part of this change.

#### Scenario: Login API is out of scope
- **GIVEN** this change is implemented
- **WHEN** API endpoints are reviewed
- **THEN** no login endpoint SHALL be added by this change
