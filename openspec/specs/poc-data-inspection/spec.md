# poc-data-inspection Specification

## Purpose
Define POC-only read APIs for inspecting persisted user account and revoked token rows during local development and tests.

## Requirements
### Requirement: Inspect user account data
The system SHALL provide a POC-only API for reading all persisted user account data without authentication.

#### Scenario: List all user accounts without authentication
- **GIVEN** user account rows exist
- **WHEN** a client submits `GET /api/poc/users` without an Authorization header
- **THEN** the system SHALL return `200 OK`
- **THEN** the response SHALL include every persisted user account row

#### Scenario: User account response exposes persistence fields
- **GIVEN** a user account row exists
- **WHEN** a client submits `GET /api/poc/users`
- **THEN** each user item SHALL include `id`
- **THEN** each user item SHALL include `username`
- **THEN** each user item SHALL include `nickname`
- **THEN** each user item SHALL include `password_hash`
- **THEN** each user item SHALL include `created_at`
- **THEN** each user item SHALL include `updated_at`
- **THEN** each user item SHALL include `deleted_at`
- **THEN** each user item SHALL include `active_username_key`
- **THEN** each user item SHALL include `active_nickname_key`

#### Scenario: Active user account inspection includes null deleted_at
- **GIVEN** an active user account row exists
- **WHEN** a client submits `GET /api/poc/users`
- **THEN** that user item SHALL include `deleted_at`
- **THEN** that user item's `deleted_at` value SHALL be null

#### Scenario: Active user account inspection includes active unique keys
- **GIVEN** an active user account row exists with a persisted username and nickname
- **WHEN** a client submits `GET /api/poc/users`
- **THEN** that user item SHALL include `active_username_key`
- **THEN** that user item's `active_username_key` value SHALL equal the persisted username
- **THEN** that user item SHALL include `active_nickname_key`
- **THEN** that user item's `active_nickname_key` value SHALL equal the persisted nickname

#### Scenario: Active user account without nickname inspection includes null active nickname key
- **GIVEN** an active user account row exists without a nickname
- **WHEN** a client submits `GET /api/poc/users`
- **THEN** that user item SHALL include `active_nickname_key`
- **THEN** that user item's `active_nickname_key` value SHALL be null

#### Scenario: Soft-deleted user account inspection includes deleted_at timestamp
- **GIVEN** a soft-deleted user account row exists
- **WHEN** a client submits `GET /api/poc/users`
- **THEN** that user item SHALL include `deleted_at`
- **THEN** that user item's `deleted_at` value SHALL be the persisted deletion timestamp

#### Scenario: Soft-deleted user account inspection includes null active unique keys
- **GIVEN** a soft-deleted user account row exists
- **WHEN** a client submits `GET /api/poc/users`
- **THEN** that user item SHALL include `active_username_key`
- **THEN** that user item's `active_username_key` value SHALL be null
- **THEN** that user item SHALL include `active_nickname_key`
- **THEN** that user item's `active_nickname_key` value SHALL be null

#### Scenario: Empty user account list
- **GIVEN** no user account rows exist
- **WHEN** a client submits `GET /api/poc/users`
- **THEN** the system SHALL return `200 OK`
- **THEN** the response SHALL include an empty collection

#### Scenario: User account inspection ignores authorization header
- **GIVEN** a user account inspection request includes an Authorization header
- **WHEN** a client submits `GET /api/poc/users`
- **THEN** the system SHALL ignore the Authorization header
- **THEN** the system SHALL return `200 OK`

### Requirement: Inspect revoked token data
The system SHALL provide a POC-only API for reading all persisted revoked token data without authentication.

#### Scenario: List all revoked tokens without authentication
- **GIVEN** revoked token rows exist
- **WHEN** a client submits `GET /api/poc/revoked-tokens` without an Authorization header
- **THEN** the system SHALL return `200 OK`
- **THEN** the response SHALL include every persisted revoked token row

#### Scenario: Revoked token response exposes persistence fields
- **GIVEN** a revoked token row exists
- **WHEN** a client submits `GET /api/poc/revoked-tokens`
- **THEN** each revoked token item SHALL include `token_id`
- **THEN** each revoked token item SHALL include `user_id`
- **THEN** each revoked token item SHALL include `token_type`
- **THEN** each revoked token item SHALL include `expires_at`
- **THEN** each revoked token item SHALL include `revoked_at`

#### Scenario: Empty revoked token list
- **GIVEN** no revoked token rows exist
- **WHEN** a client submits `GET /api/poc/revoked-tokens`
- **THEN** the system SHALL return `200 OK`
- **THEN** the response SHALL include an empty collection

#### Scenario: Revoked token inspection ignores authorization header
- **GIVEN** a revoked token inspection request includes an Authorization header
- **WHEN** a client submits `GET /api/poc/revoked-tokens`
- **THEN** the system SHALL ignore the Authorization header
- **THEN** the system SHALL return `200 OK`

### Requirement: Keep POC inspection read-only
The system SHALL limit POC inspection APIs to read-only access.

#### Scenario: No write operation is added
- **GIVEN** this change is implemented
- **WHEN** POC inspection API endpoints are reviewed
- **THEN** no create, update, delete, filter, pagination, sorting, or export operation SHALL be added by this change

#### Scenario: Existing auth contracts remain unchanged
- **GIVEN** this change is implemented
- **WHEN** existing register, login, refresh, and logout APIs are reviewed
- **THEN** their request and response contracts SHALL remain unchanged
