## Why

目前使用者取得 access token 與 refresh token 後，缺少明確的登出機制。新增登出 API 可讓 client 主動結束目前裝置的認證狀態，並讓被登出的 refresh token 與 access token 立即失效。

## What Changes

- 新增 `POST /api/users/logout` endpoint。
- 登出請求需要有效 access token，並在 request body 帶入 `refresh_token`。
- 登出成功後撤銷目前 access token 與 refresh token。
- 登出成功回傳 `204 No Content`，不回 response body。
- 重複登出相同 token pair 時維持 idempotent，回傳 `204 No Content`。
- 已撤銷的 refresh token 不可再用於 refresh endpoint。
- 已撤銷的 access token 不可再用於 protected API。
- 不新增登出所有裝置、session list、server-side session 管理或 refresh token reuse detection。

## Capabilities

### New Capabilities

- `user-logout`: 定義使用者登出 API、token 撤銷行為、成功與失敗 response，以及本次登出 scope。

### Modified Capabilities

- `jwt-authentication`: protected API bearer token 驗證需要拒絕已撤銷的 access token。
- `refresh-token`: refresh endpoint 需要拒絕已撤銷的 refresh token。
- `user-login`: 既有 session management non-goals 需要移除禁止 logout 與 refresh-token revocation 的舊描述。

## Impact

- 受影響 API：新增 `POST /api/users/logout`，並調整 protected API 與 refresh endpoint 的 token 驗證行為。
- 受影響程式碼：user auth controller/service、JWT issuing/validation、Spring Security resource server validator、token revocation persistence、API tests。
- 受影響資料儲存：新增撤銷 token 所需的 persistence model，例如記錄 token id 與過期時間。
- 受影響 specs：新增 `user-logout`，修改 `jwt-authentication`、`refresh-token` 與 `user-login`。
- 受影響團隊：backend/API consumers，以及需要在登出後清理本機 auth state 的 client application developers。
- Rollback plan：移除 logout endpoint、token revocation persistence 與 resource server/refresh endpoint 的 revocation checks，恢復 access token 與 refresh token 只依 JWT 簽章與 expiration 判斷有效性的行為。
