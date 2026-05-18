## MODIFIED Requirements

### Requirement: Defer session management
The system MUST NOT add logout-all-devices, session list, server-side session management, or multi-device session management as part of this change.

#### Scenario: Logout all devices is out of scope
- **GIVEN** this change is implemented
- **WHEN** API endpoints and server state are reviewed
- **THEN** no logout-all-devices capability SHALL be added by this change

#### Scenario: Session management is out of scope
- **GIVEN** this change is implemented
- **WHEN** API endpoints and server state are reviewed
- **THEN** no session list, server-side session, or multi-device session management SHALL be added by this change
