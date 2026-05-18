## Why

目前使用者可以註冊、登入、刷新 token、登出與更新基本資料，但無法自行註銷帳號。新增帳號註銷能力可補齊基本帳號生命週期，並讓 POC 開始明確處理已刪除帳號與既有 token 的互動。

## What Changes

- 新增 authenticated user account deletion API：`DELETE /api/users/me`。
- 刪除方式採 soft delete，在 `user_account` 以 `deleted_at` 判斷帳號是否已註銷，不直接移除 row。
- Request body 必須包含 `password` 與 `refresh_token`。
- 成功註銷時，Spring Security 會驗證 bearer access token；系統會驗證密碼與 refresh token，並確認 authenticated user 與 refresh token 屬於同一個目前使用者。
- 成功註銷時，系統會設定目前使用者的 `deleted_at`，revoke submitted access token 與 submitted refresh token，並回傳 `204 No Content`。
- Request body 內的 `password` 或 `refresh_token` 若 missing、blank 或不合法，皆統一回 `401 Unauthorized` / `INVALID_CREDENTIALS`，message 使用 generic credential invalid 語意，不區分是哪個 credential 錯誤。
- Authorization bearer token 缺失、不合法、型別錯誤、已 revoked 或 subject 已不可用時，不適用 request body credential 統一規則，仍回 `401 Unauthorized` / `UNAUTHORIZED`。
- Missing request body 回既有統一 `400 Bad Request` / `VALIDATION_ERROR`。
- 已註銷帳號不可再登入、刷新 token 或更新 profile。
- 已註銷帳號的舊 token pair 呼叫 logout 時，系統應拒絕並回 `401 Unauthorized`。
- 註冊時判斷 username / nickname 是否可用必須排除已 soft-deleted 帳號；已註銷帳號的 username / nickname 可被重新註冊使用。
- Profile update nickname 唯一性判斷必須排除已 soft-deleted 帳號；已註銷帳號的 nickname 可被其他 active user 重用。
- 不新增 session list、logout-all-devices、token family invalidation 或硬刪除資料行為。

## Capabilities

### New Capabilities

- `user-account-deletion`: 定義 authenticated user soft delete 自己帳號的能力，包含 `DELETE /api/users/me` contract、密碼驗證、refresh token 驗證、token revocation、`deleted_at` 狀態與成功/錯誤回應。

### Modified Capabilities

- `user-registration`: username / nickname 唯一性判斷改為只針對未註銷帳號；已 soft-deleted 帳號的 username / nickname 可被重新註冊使用。
- `user-login`: 已 soft-deleted 帳號不可登入，對外回應維持 `401 Unauthorized` / `INVALID_CREDENTIALS`。
- `refresh-token`: refresh token subject 若對應已 soft-deleted 帳號，應視為無效 refresh token。
- `user-logout`: logout token pair 若對應已 soft-deleted 帳號，應拒絕並回 `401 Unauthorized`。
- `user-profile`: profile update 的 token subject 若對應已 soft-deleted 帳號，應視為不存在或不可用並回 `401 Unauthorized`；profile nickname uniqueness 改為只針對 active users。

## Impact

- 受影響 API：新增 `DELETE /api/users/me`；既有 register/login/refresh/logout/profile update 的 active-user 判斷需要納入 `deleted_at`。
- 受影響程式碼：新增 account deletion route/controller/service/request DTO/validation，擴充 `UserAccount` soft delete 狀態與 repository active-user 查詢，並重用既有 JWT、password encoder、refresh token validation 與 token revocation 元件。
- 受影響資料：`user_account` 需要新增 nullable `deleted_at`、`active_username_key`、`active_nickname_key` 欄位；unique constraints 從 `username` / `nickname` 移到 active key 欄位；註銷帳號時更新 `deleted_at` 並清空 active key，不刪除 row。
- 受影響安全性：刪除帳號是破壞性操作，必須要求 valid bearer access token、正確密碼與同 user refresh token；成功後 submitted token pair 必須 revoked。
- 受影響團隊：backend/API 開發者，以及會呼叫帳號註銷、註冊、登入、refresh 或 profile update flow 的 client developers。
- Rollback plan：移除 `DELETE /api/users/me` endpoint 與相關 service/DTO/tests；停止使用 `deleted_at` active-user 判斷。若資料欄位已加入，rollback 可先保留 nullable `deleted_at` 欄位不使用，或在後續 migration 中移除。
