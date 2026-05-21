## 1. 測試先行

- [x] 1.1 更新 `UserAuthApiTest` 中既有 refresh 行為測試，將「同一個原 refresh token 可再次使用」改為「成功 refresh 後重複使用原 token 會回 `401 Unauthorized` 與 `INVALID_REFRESH_TOKEN`」。
- [x] 1.2 新增或調整測試，確認成功 refresh 後新核發的 `refresh_token` 仍可再次用來換發 token pair。
- [x] 1.3 新增或調整測試，確認成功 refresh 後原 refresh token 的 token id 會出現在 revoked-token 紀錄中。
- [x] 1.4 視現有測試工具可行性，加入 refresh token consume path 的重複寫入測試，確認 duplicate token id 不會被視為成功 consume。
- [x] 1.5 先執行相關測試並確認至少一個新需求測試在實作前失敗。

## 2. Refresh Token Consume 實作

- [x] 2.1 在 revoked-token 記錄元件中新增 refresh 專用 consume path，保留 logout 使用的 idempotent `record()` 行為不變。
- [x] 2.2 讓 refresh 專用 consume path 參與 caller transaction，不使用 `REQUIRES_NEW`，並在 insert revoked token 後 `flush`，使 duplicate key 能在回傳前被偵測。
- [x] 2.3 將 duplicate token id 或同等唯一鍵衝突轉換成 refresh 流程可判斷的失敗結果，再由 API 邊界回應 `INVALID_REFRESH_TOKEN`。
- [x] 2.4 確認 refresh 專用 consume path 不新增 token family invalidation、session management 或 logout-all-devices 行為。

## 3. Refresh 流程整合

- [x] 3.1 將 `UserAuthService.refresh` 從 `@Transactional(readOnly = true)` 調整為一般 `@Transactional`。
- [x] 3.2 更新 refresh 流程順序：驗證 refresh token、確認 active user、產生新 token pair、consume 原 refresh token、回傳新 token pair。
- [x] 3.3 當原 refresh token 已被撤銷或已被 consume 時，確保 refresh 回傳 `401 Unauthorized` 與 `INVALID_REFRESH_TOKEN`。
- [x] 3.4 保持 refresh response shape 不變，只回傳 `access_token` 與 `refresh_token`，不加入 profile 欄位。
- [x] 3.5 確認 Authorization header 仍不參與 refresh endpoint 的驗證邏輯。

## 4. 驗證與收尾

- [x] 4.1 執行 `openspec validate prevent-refresh-token-reuse --strict`。
- [x] 4.2 執行相關測試，例如 `./mvnw -Dtest=UserAuthApiTest test`。
- [x] 4.3 執行完整測試 `./mvnw test`；若 rename-heavy 或 incremental compile 狀態可疑，改跑 `./mvnw clean test`。
- [x] 4.4 Review 最終 diff，確認沒有新增未使用程式碼、magic string、非需求內的 token family/session 行為，且錯誤 code/message 仍走既有 catalog。
