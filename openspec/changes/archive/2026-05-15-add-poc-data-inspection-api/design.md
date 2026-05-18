## Context

目前專案已具備 user account persistence 與 revoked token persistence。POC 驗證時，開發者需要在執行 register、login、refresh、logout 等流程後，直接確認資料表中的實際狀態，例如 user 是否建立、password hash 是否存在、logout 後 access / refresh token id 是否被寫入 revoked token store。

這個能力只服務 POC 觀察與測試，不是正式 admin API。因使用者明確要求不需身份驗證，implementation 必須把安全風險限制在清楚命名的 POC scope。

## Goals / Non-Goals

**Goals:**

- 新增無身份驗證的 POC read-only endpoints。
- 取得所有 user account rows。
- 取得所有 revoked token rows。
- Response 使用 JSON 並符合既有 `snake_case` key 慣例。
- 回傳足以檢查 persistence state 的欄位，包含 user 的 `password_hash`、`deleted_at`、`active_username_key`、`active_nickname_key` 與 revoked token 的 `token_id`、`user_id`、`token_type`、`expires_at`、`revoked_at`。
- 不改變既有 register、login、refresh、logout API contract。

**Non-Goals:**

- 不新增正式 admin / backoffice API。
- 不新增身份驗證、授權、角色或 permission model。
- 不新增 create、update、delete、filter、pagination、sorting 或 export。
- 不由本 change 改變資料庫 schema；`deleted_at`、`active_username_key`、`active_nickname_key` 欄位由 `add-user-account-deletion-api` 的 schema 變更提供，本 change 只定義 POC inspection 如何輸出這些欄位。
- 不隱藏 POC endpoint 中的 persisted password hash；此 endpoint 僅供受控 POC 環境使用。

## Decisions

### 1. 使用獨立 POC inspection package

新增 `com.example.demo.poc.inspection` 作為 controller、service、route constants 與 DTO 的位置。這讓 POC-only 能力與 `user`、`auth` domain package 保持分離，避免把非正式產品能力混入正式 authentication flow。

替代方案：

- 放在 `user` 或 `auth` package：可以少一個 package，但會讓 POC inspection 與正式 domain behavior 混在一起。
- 放在 `common`：不適合，因為這不是跨 domain helper，而是具體 API capability。

### 2. 提供兩個 read-only REST endpoints

新增：

- `GET /api/poc/users`
- `GET /api/poc/revoked-tokens`

兩個 endpoint 都不需要 Authorization header，且只回傳目前資料表中所有 rows。若 request 帶有 Authorization header，POC inspection endpoint 仍會忽略該 header，不進行 bearer token 驗證。

選擇此作法的原因：

- user account 與 revoked token 是不同 resource，拆開 endpoint 較符合 RESTful 風格。
- 測試與手動驗證可以只讀取需要的資料表。
- 未來若移除 POC endpoint，可直接移除整個 POC package 與 route permit rule。

替代方案：

- 單一 `GET /api/poc/data` aggregate response：呼叫較少，但會把兩個 resource 綁在同一個 response contract，後續調整彈性較低。
- 使用 H2 console 或直接 DB client：可行，但不利於 API test 自動驗證，也不符合目前以 REST API 驗證 POC 的工作方式。

### 3. Response DTO 明確對應 persistence 欄位

User response item 包含：

- `id`
- `username`
- `nickname`
- `password_hash`
- `created_at`
- `updated_at`
- `deleted_at`
- `active_username_key`
- `active_nickname_key`

`deleted_at` 表示 soft delete 狀態；active user 也必須輸出 `deleted_at: null`。因全域 Jackson 設定是 `non_null`，DTO 需要針對 `deletedAt` 欄位覆蓋 serialization include 行為。

`active_username_key` 與 `active_nickname_key` 是 soft delete 實作使用的 active uniqueness helper 欄位。它們由 `add-user-account-deletion-api` 的 schema 變更提供，仍屬於內部 persistence 欄位，不屬於正式 user-facing API，但 POC inspection 需要輸出這兩個欄位以驗證 active user uniqueness state：active user 的 `active_username_key` 等於 `username`；active user 有 nickname 時 `active_nickname_key` 等於 `nickname`，無 nickname 時為 null；soft-deleted user 的兩個 active key 皆為 null。因全域 Jackson 設定是 `non_null`，DTO 也需要針對這兩個欄位覆蓋 serialization include 行為，確保 null key 仍出現在 POC response。

Revoked token response item 包含：

- `token_id`
- `user_id`
- `token_type`
- `expires_at`
- `revoked_at`

DTO 會使用 Java `camelCase` 欄位，並透過既有 Jackson snake_case 設定或明確 JSON property annotation 確保 response key 為 `snake_case`。API field 名稱仍需集中管理，避免 magic string 散落。

替代方案：

- 直接回傳 entity：實作最快，但會讓 persistence model 直接成為 API contract，也較難控制 JSON key 與未來欄位變化。
- 隱藏 `password_hash`：安全性較好，但不符合本次「確認所有資料」的 POC 需求；正式 API 不應採用這種 response。

### 4. SecurityConfig 明確 permit POC routes

`SecurityConfig` 需明確允許上述 POC GET endpoints，不要求 bearer token。這是刻意設計，不是沿用 protected API authentication。

替代方案：

- 不調整 SecurityConfig，讓 endpoint 需要 bearer token：違反本次 POC 需求。
- 使用 profile 或 feature flag 包住 endpoint：正式環境較安全，但本次 POC 先不引入額外設定複雜度；若未來要部署到共享環境，應優先補上這層保護。

## Risks / Trade-offs

- [Risk] Endpoint 未驗證且會暴露 password hash 與 token revocation state。 -> Mitigation: 使用 `/api/poc/...` 路徑與獨立 POC package 明確標示用途，並在 proposal/spec/tasks 中維持 POC-only scope。
- [Risk] 回傳所有資料在資料量增加後可能造成效能問題。 -> Mitigation: 本次只支援 POC，小資料量可接受；pagination / filtering 明確列為 non-goal。
- [Risk] POC endpoint 被誤認為正式 admin API。 -> Mitigation: 命名、文件與測試都使用 POC inspection 語意，不放入正式 user/auth controller。
- [Risk] DTO 與 entity 欄位同步成本增加。 -> Mitigation: 欄位數少且 read-only，顯式 DTO 可換取更清楚的 API contract。
- [Risk] nullable persistence fields 受全域 non-null serialization 影響而被省略。 -> Mitigation: 在 user inspection DTO 的 `deletedAt`、`activeUsernameKey`、`activeNicknameKey` 欄位上覆蓋 include 行為，並以 API test 驗證 active user 與 soft-deleted user response 仍包含這些 nullable keys。

## Migration Plan

1. 新增 POC inspection route constants、controller、service 與 response DTO。
2. 在 `SecurityConfig` 允許 POC inspection GET endpoints。
3. 新增 API tests 覆蓋未帶 Authorization header 仍可取得 users 與 revoked tokens。
4. 驗證 response key 使用 `snake_case`，且資料與 persistence state 一致。
5. 驗證 user inspection 對 active user 也輸出 `deleted_at: null`，並輸出 `active_username_key` / `active_nickname_key`。
6. 驗證 user inspection 對 soft-deleted user 輸出 persisted `deleted_at`，且 `active_username_key` / `active_nickname_key` 皆為 null。

Rollback plan：移除 POC inspection package、SecurityConfig permit rules 與相關測試；本 change 不直接擁有資料庫 schema 變更，因此 rollback 不移除 `add-user-account-deletion-api` 所提供的 `deleted_at` / active key 欄位，也不影響既有 auth endpoints。

## Open Questions

- None.
