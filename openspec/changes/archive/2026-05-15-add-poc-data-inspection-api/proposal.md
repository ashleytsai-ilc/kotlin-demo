## Why

目前 POC 在執行註冊、登入、refresh、logout 等流程後，需要直接確認 user 與 revoked token persistence state 是否符合預期。新增簡單的資料檢視 API 可降低手動查 DB 或額外除錯工具的成本。

## What Changes

- 新增 POC 專用的 read-only inspection API，可取得所有 user account 資料。
- 新增 POC 專用的 read-only inspection API，可取得所有 revoked token 資料。
- 這些 inspection API 不需要任何身份驗證，方便在本機或 POC 驗證流程中直接呼叫。
- Response 以 JSON 回傳資料列集合，保留足以檢查 persistence state 的欄位。
- User inspection response 需包含 `deleted_at`、`active_username_key`、`active_nickname_key`，讓 POC 可檢查 soft delete 狀態與 active uniqueness helper 欄位；未註銷帳號的 `deleted_at` 為 null，已註銷帳號的 active key 欄位為 null。
- 不新增 create、update、delete、filter、pagination、sorting 或匯出功能。
- 不改變既有 register、login、refresh、logout API contract。
- 此能力僅供 POC 使用，不視為 production-ready admin API。

## Capabilities

### New Capabilities

- `poc-data-inspection`: 定義 POC 專用資料檢視 API，可未經身份驗證取得 user account 與 revoked token 的所有資料。

### Modified Capabilities

- None.

## Impact

- 受影響 API：新增 POC inspection endpoints。
- 受影響程式碼：新增 read-only controller/service 或同等邊界，讀取 user account 與 revoked token repository；user inspection DTO 需輸出 `deleted_at`、`active_username_key`、`active_nickname_key`。
- 受影響資料：暴露 user account 與 revoked token persistence state，包含僅適合 POC 檢視的欄位。
- Cross-change dependency：`deleted_at`、`active_username_key`、`active_nickname_key` 欄位由 `add-user-account-deletion-api` 的 schema 變更提供；本 change 只負責在 POC inspection response 輸出這些欄位。
- 受影響安全性：刻意不加入身份驗證，因此只能用於 POC 或受控本機環境，不應部署為正式環境公開 API。
- 受影響團隊：backend/API 開發者、測試與驗證 POC 流程的人員。
- Rollback plan：移除 POC inspection endpoints、相關 controller/service/DTO 與測試，不影響既有 user auth 功能；本 change 不移除 `add-user-account-deletion-api` 所提供的 persistence schema 欄位。
