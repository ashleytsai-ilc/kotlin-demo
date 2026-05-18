## Why

目前使用者在註冊後無法更新自己的顯示名稱。新增 nickname 更新能力可讓已登入使用者維護自己的基本資料，同時限制操作範圍只作用在目前 authenticated user。

## What Changes

- 新增用戶資料修改 API，允許已登入使用者修改自己的 `nickname`。
- 更新行為必須以 bearer access token 識別目前使用者，不允許指定或修改其他使用者。
- Request 中只有 `nickname` 是可修改欄位；未知欄位或不可修改欄位會被忽略，不回 validation error。
- Nickname 更新沿用既有 nickname 規則：request body 必須包含 `nickname`；空字串或 blank 時清除 nickname；null 或省略 `nickname` 會回 validation error；非空值不可超過 30 字元且必須唯一。
- 成功更新後回傳更新後的 user profile，包含 `id`、`username`、`nickname`、`created_at`、`updated_at`；若 nickname 已被清除，response 的 `nickname` 顯示為空字串。
- 不允許透過此 API 修改 `username`、`password`、token、user id 或其他欄位；若 request 夾帶這些欄位，系統會忽略它們。
- 既有 register、login、refresh、logout 的一般成功與 token contract 不變；missing request body 統一改由框架層回 `400 Bad Request` / `VALIDATION_ERROR`。

## Capabilities

### New Capabilities

- `user-profile`: 定義 authenticated user profile 更新能力，包含只允許目前使用者修改自己的 `nickname`、輸入驗證、衝突處理與成功 response。

### Modified Capabilities

- `user-registration`: missing request body 改由框架層統一回 `400 Bad Request` / `VALIDATION_ERROR`。
- `user-login`: missing request body 改由框架層統一回 `400 Bad Request` / `VALIDATION_ERROR`；其他 invalid credentials 行為維持不變。
- `refresh-token`: missing request body 改由框架層統一回 `400 Bad Request` / `VALIDATION_ERROR`；缺少、空白、無效、過期或 revoked `refresh_token` 仍維持既有 `INVALID_REFRESH_TOKEN` contract。
- `user-logout`: missing request body 改由框架層統一回 `400 Bad Request` / `VALIDATION_ERROR`；access token 與 refresh token 驗證 contract 維持不變。

## Impact

- 受影響 API：新增 authenticated user nickname update endpoint，並統一既有 request-body API 的 missing body 錯誤語意。
- 受影響程式碼：新增或擴充 user profile controller/service/DTO，讀取 bearer access token authentication subject，並在 nickname 實際變更時更新 `UserAccount.nickname` 與 `updatedAt`。
- 受影響資料：nickname 實際變更時更新既有 user account row 的 `nickname` 與 `updatedAt`，不新增資料表。
- 受影響安全性：此 API 必須要求有效 bearer access token，並且只能更新 token subject 對應的 user。
- 受影響團隊：backend/API 開發者，以及會呼叫 user profile update flow 的 client developers。
- Rollback plan：移除 user profile update endpoint、相關 controller/service/DTO 與測試；不需要資料 migration，既有 user account schema 維持不變。
