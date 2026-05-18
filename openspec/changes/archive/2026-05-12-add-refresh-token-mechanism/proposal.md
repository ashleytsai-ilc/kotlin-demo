## Why

Access token 生命週期較短，客戶端需要一個正式支援的方式，在 access token 過期後延續已驗證狀態，而不是要求使用者重新登入。本次變更會新增 refresh token 更新機制，並將登出與 token 撤銷保留到後續 change 處理。

## What Changes

- 新增以 JWT-based refresh token 為基礎的 refresh token 機制。
- 註冊與登入成功回應都會回傳 `access_token` 與 `refresh_token`。
- 新增 RESTful refresh endpoint，接受有效的 refresh token，並回傳新的 `access_token` 與新的 `refresh_token`。
- 維持既有 access token 行為與目前 JWT authentication flow 相容。
- 登出、refresh token 撤銷、server-side session 管理，以及多裝置 session 管理不包含在本次變更範圍內。
- 對只消費 `access_token` 的既有註冊或登入 client，不預期產生 breaking change。

## Capabilities

### New Capabilities

- `refresh-token`: 定義 refresh token 簽發、refresh endpoint 行為、refresh token 驗證，以及 token 更新回應。

### Modified Capabilities

- `user-registration`: 註冊成功回應除了既有 profile 欄位與 `access_token`，也會包含 `refresh_token`。
- `user-login`: 登入成功回應除了既有 profile 欄位與 `access_token`，也會包含 `refresh_token`。
- `jwt-authentication`: JWT 簽發行為會新增 refresh-token JWT，並支援透過有效 refresh token 更新 access token。

## Impact

- 受影響 API：註冊回應、登入回應，以及新增的 refresh endpoint。
- 受影響程式碼：user auth controller/service、auth DTO、JWT service/configuration、security route configuration，以及 API tests。
- 受影響 specs：`user-registration`、`user-login`、`jwt-authentication`，以及新的 `refresh-token`。
- 受影響團隊：backend/API consumers，以及負責管理 authentication state 的 client application developers。
- Rollback plan：移除 refresh endpoint 與 `refresh_token` response 欄位，將註冊/登入回應恢復為只回傳 access token，並移除 refresh-token JWT 設定與測試。
