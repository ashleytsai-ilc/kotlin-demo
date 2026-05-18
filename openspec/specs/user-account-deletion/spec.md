# user-account-deletion Specification

## Purpose
Define authenticated self-service account deletion behavior, including soft deletion, active-user uniqueness release, and submitted token revocation.

## Requirements
### Requirement: Delete current user account
The system SHALL provide a RESTful API for an authenticated user to soft delete their own account.

#### Scenario: Successful account deletion
- **GIVEN** a client has a valid access token for an active user
- **AND** the client has the matching refresh token for the same user
- **AND** the client knows the user's current password
- **WHEN** the client submits `DELETE /api/users/me` with `password` and `refresh_token`
- **THEN** the system SHALL mark the user account as deleted
- **THEN** the system SHALL return `204 No Content`
- **THEN** the response SHALL NOT include a body

#### Scenario: Account deletion sets deleted timestamp
- **GIVEN** a user account is active
- **WHEN** the user deletes their own account successfully
- **THEN** the persisted user account SHALL have `deletedAt` set

#### Scenario: Account deletion preserves original profile values
- **GIVEN** a user account has a `username` and `nickname`
- **WHEN** the user deletes their own account successfully
- **THEN** the persisted user account SHALL preserve the original `username`
- **THEN** the persisted user account SHALL preserve the original `nickname`

#### Scenario: Account deletion releases active unique values
- **GIVEN** a user account is active
- **WHEN** the user deletes their own account successfully
- **THEN** the deleted user account SHALL no longer reserve its username for active-user uniqueness
- **THEN** the deleted user account SHALL no longer reserve its nickname for active-user uniqueness
- **THEN** the persisted user account SHALL have `activeUsernameKey` set to null
- **THEN** the persisted user account SHALL have `activeNicknameKey` set to null

### Requirement: Validate account deletion credentials
The system SHALL validate the access token, password, and refresh token before deleting the current user account.

Authorization bearer token failures SHALL be reported as `UNAUTHORIZED`; only request body `password` and `refresh_token` credential failures SHALL be reported as `INVALID_CREDENTIALS`.

#### Scenario: Missing access token
- **GIVEN** an account deletion request has no Authorization bearer token
- **WHEN** the client submits `DELETE /api/users/me`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `UNAUTHORIZED`

#### Scenario: Invalid access token
- **GIVEN** an account deletion request has an invalid Authorization bearer token
- **WHEN** the client submits `DELETE /api/users/me`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `UNAUTHORIZED`

#### Scenario: Refresh token cannot be used as bearer token
- **GIVEN** a client has a valid refresh token
- **WHEN** the client submits it as the Authorization bearer token for `DELETE /api/users/me`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `UNAUTHORIZED`

#### Scenario: Revoked access token cannot delete account
- **GIVEN** a client's access token has been revoked
- **WHEN** the client submits `DELETE /api/users/me` with that access token
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `UNAUTHORIZED`

#### Scenario: Missing request body
- **GIVEN** a client has a valid access token for an active user
- **WHEN** the client submits `DELETE /api/users/me` without a request body
- **THEN** the system SHALL reject the request with `400 Bad Request`
- **THEN** the error response SHALL include code `VALIDATION_ERROR`

#### Scenario: Missing password
- **GIVEN** a client has a valid access token for an active user
- **WHEN** the client submits `DELETE /api/users/me` without `password`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_CREDENTIALS`

#### Scenario: Blank password
- **GIVEN** a client has a valid access token for an active user
- **WHEN** the client submits `DELETE /api/users/me` with a blank `password`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_CREDENTIALS`

#### Scenario: Incorrect password
- **GIVEN** a client has a valid access token for an active user
- **WHEN** the client submits `DELETE /api/users/me` with an incorrect `password`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_CREDENTIALS`
- **THEN** the system SHALL NOT delete the user account

#### Scenario: Missing refresh token
- **GIVEN** a client has a valid access token for an active user
- **WHEN** the client submits `DELETE /api/users/me` without `refresh_token`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_CREDENTIALS`

#### Scenario: Blank refresh token
- **GIVEN** a client has a valid access token for an active user
- **WHEN** the client submits `DELETE /api/users/me` with a blank `refresh_token`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_CREDENTIALS`

#### Scenario: Invalid refresh token
- **GIVEN** a client has a valid access token for an active user
- **WHEN** the client submits `DELETE /api/users/me` with an invalid `refresh_token`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_CREDENTIALS`

#### Scenario: Expired refresh token
- **GIVEN** a client has a valid access token for an active user
- **WHEN** the client submits `DELETE /api/users/me` with an expired `refresh_token`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_CREDENTIALS`

#### Scenario: Access token cannot be used as refresh token
- **GIVEN** a client has a valid access token for an active user
- **WHEN** the client submits the access token as `refresh_token`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_CREDENTIALS`

#### Scenario: Revoked refresh token
- **GIVEN** a client has a revoked refresh token
- **WHEN** the client submits `DELETE /api/users/me` with that `refresh_token`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_CREDENTIALS`

#### Scenario: Tokens must belong to the same user
- **GIVEN** an account deletion request has a valid access token for one user
- **WHEN** the client submits a valid refresh token for a different user
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_CREDENTIALS`

#### Scenario: Already deleted user cannot delete account again
- **GIVEN** a client has a valid access token whose subject belongs to a soft-deleted user account
- **WHEN** the client submits `DELETE /api/users/me`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `UNAUTHORIZED`

### Requirement: Revoke submitted tokens on account deletion
The system SHALL revoke the submitted access token and submitted refresh token when account deletion succeeds.

#### Scenario: Submitted tokens are revoked
- **GIVEN** a user deletes their own account successfully
- **WHEN** persisted revoked token records are inspected
- **THEN** the submitted access token SHALL be recorded as revoked
- **THEN** the submitted refresh token SHALL be recorded as revoked

#### Scenario: Deleted account refresh token cannot refresh
- **GIVEN** a user has deleted their own account successfully
- **WHEN** the client submits that user's refresh token to `POST /api/users/tokens/refresh`
- **THEN** the system SHALL reject the request with `401 Unauthorized`
- **THEN** the error response SHALL include code `INVALID_REFRESH_TOKEN`

#### Scenario: No logout all devices is added
- **GIVEN** this change is implemented
- **WHEN** account deletion behavior is reviewed
- **THEN** no logout-all-devices capability SHALL be added

#### Scenario: No token family invalidation is added
- **GIVEN** this change is implemented
- **WHEN** account deletion token revocation behavior is reviewed
- **THEN** no token family invalidation capability SHALL be added
