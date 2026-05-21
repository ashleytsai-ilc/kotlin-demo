## 為什麼

目前 refresh token 在成功換發新 token 後仍可重複使用，導致同一個 refresh token 在過期或被明確撤銷前，可以持續換發多組新的 token。這會削弱 token rotation 的安全性，因為一旦 refresh token 被複製，即使合法用戶端已經完成換發，該舊 token 仍然有效。

## 變更內容

- 在 `POST /api/users/tokens/refresh` 成功換發 token 後，撤銷本次提交的 refresh token。
- 後續若再次使用同一個已消耗的 refresh token，系統必須回傳 `401 Unauthorized`，並使用錯誤代碼 `INVALID_REFRESH_TOKEN`。
- 保持成功 refresh 的 response 形狀不變：仍回傳 `access_token` 與 `refresh_token`，且不包含 profile 欄位。
- refresh endpoint 仍只使用 request body 內的 `refresh_token` 作為驗證依據；Authorization header 仍不參與 refresh 驗證。
- 本變更不新增 token family invalidation、logout-all-devices、session list 或 device/session management 能力。
- 預期不會造成 API response shape 的 breaking change。

## Capabilities

### New Capabilities

- 無。

### Modified Capabilities

- `refresh-token`：調整 refresh token 的需求，使 refresh token 在成功換發後成為一次性 token，不能再被重複用來換發新 token。

## 影響

- 受影響程式碼：`UserAuthService` 的 refresh token 處理、JWT refresh 驗證、revoked-token 記錄與檢查，以及相關 auth API 測試。
- 受影響 API：`POST /api/users/tokens/refresh` 對重複使用同一個 refresh token 的行為會改變；第一次成功使用仍回傳 `200 OK`，後續重複使用會回傳 `401 Unauthorized` 與 `INVALID_REFRESH_TOKEN`。
- 受影響資料：成功 refresh 時會新增一筆 revoked-token 紀錄，用來標記本次已消耗的 refresh token。
- 受影響團隊：backend/API 維護者，以及目前可能在收到新 token pair 後仍 retry 或重複使用舊 refresh token 的 API client。
- Rollback plan：移除 refresh 成功時撤銷舊 refresh token 的步驟，並恢復既有 `refresh-token` 需求，也就是舊 refresh token 只要未被撤銷且未過期就仍可使用。
