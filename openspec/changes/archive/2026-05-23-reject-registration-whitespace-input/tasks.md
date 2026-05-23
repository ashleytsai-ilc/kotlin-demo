## 1. Tests

- [x] 1.1 Update the registration API test so blank `nickname` expects `400 Bad Request` and `VALIDATION_ERROR` instead of successful registration.
- [x] 1.2 Add registration API tests proving `nickname` values with leading, trailing, and internal whitespace are rejected.
- [x] 1.3 Add registration API tests proving `username` values with leading, trailing, and internal whitespace are rejected.
- [x] 1.4 Add or update registration API tests proving omitted `nickname` and JSON null `nickname` still create users without a nickname.
- [x] 1.5 If focused registration validation tests exist, cover `username` / `nickname` no-trim behavior and whitespace rejection there as well.

## 2. Implementation

- [x] 2.1 Update registration validation rules to centrally define that `username` and `nickname` MUST NOT contain whitespace.
- [x] 2.2 Remove pre-validation trim behavior for `username` so validation uses the request value as submitted.
- [x] 2.3 Remove pre-validation trim and blank-to-null behavior for provided `nickname` values so blank or whitespace-containing nicknames produce validation errors.
- [x] 2.4 Preserve the optional nickname behavior for omitted `nickname` and JSON null `nickname`, including allowing multiple users without a nickname.

## 3. Verification

- [x] 3.1 Run the relevant registration tests and confirm the new tests pass with the old blank-nickname behavior updated.
- [x] 3.2 Run `./mvnw test`.
- [x] 3.3 Run `openspec validate reject-registration-whitespace-input --strict`.
- [x] 3.4 Self-review the change to confirm no unused trim helper, unused function, or test name conflicting with the new spec remains.
