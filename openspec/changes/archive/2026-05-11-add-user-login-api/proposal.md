## Why

- 專案已完成用戶註冊與 JWT 基礎能力，但既有用戶尚無正式登入 API 可重新取得 access token。
- 本 change 補上最小登入流程，讓已註冊用戶能以 `username` 與 `password` 驗證身分並取得後續 API 所需的 JWT。

## What Changes

- 新增 `POST /api/users/login` API。
- 登入 request 使用 `username` 與 `password`。
- 登入成功時回傳 `access_token` 與 user profile。
- 登入失敗時統一回傳 `INVALID_CREDENTIALS`，不區分帳號不存在或密碼錯誤。
- JWT 簽發、期限與 signing secret 沿用目前設定。
- 本次不加入 refresh token、logout、server-side session 或多裝置 session 管理；這些留待後續 proposal。
- 更新既有 JWT authentication spec，移除「此階段不新增 login API」的限制，並補上登入成功後簽發 JWT 的行為。

## Capabilities

### New Capabilities

- `user-login`: 定義用戶以 `username` 與 `password` 登入、成功回傳 access token 與 user profile、失敗時回傳統一錯誤的 API 行為。

### Modified Capabilities

- `jwt-authentication`: 登入成功後也會簽發 JWT，並移除先前 registration change 中「不新增 login API」的限制。

## Impact

- 受影響程式碼：
  - `src/main/java/com/example/demo`
  - `src/test/java/com/example/demo`
  - `src/main/resources/application.yaml` 若需要新增登入相關設定
- API 影響：
  - 新增 `POST /api/users/login`。
  - 登入成功 response 包含 `access_token` 與 user profile。
  - 登入失敗 response 使用標準錯誤格式，錯誤 code 為 `INVALID_CREDENTIALS`。
- 既有系統影響：
  - 沿用目前 `PasswordEncoder` 驗證密碼。
  - 沿用目前 `JwtService` 簽發 token。
  - 不變更註冊 API 與既有 bearer token 驗證規則。
- 受影響團隊：
  - 後端 API 開發。
  - API 消費端或前端整合者。
  - 測試與 QA。
- 回滾計畫：
  - 移除 login 相關 endpoint、DTO 與測試。
  - 若需要拆回更細粒度，將 `UserAuthController` / `UserAuthService` 重新拆分。
  - 還原 Spring Security 中允許 login endpoint 的設定。
  - 還原本 change 對 OpenSpec specs 的修改。
