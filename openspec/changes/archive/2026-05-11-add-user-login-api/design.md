## Context

- 專案已完成用戶註冊、密碼雜湊儲存、JWT 簽發與 bearer token 驗證。
- 目前註冊成功會直接回傳 `access_token`，但既有用戶沒有登入 API 可重新取得 token。
- 現有使用者資料以 `username` 作為唯一登入識別，密碼只保存 hash。
- 本 change 仍屬 POC 階段，優先提供最小可用登入流程，不導入 session 狀態或 refresh token。

## Goals / Non-Goals

**Goals:**

- 提供 `POST /api/users/login`。
- 使用 `username` 與 `password` 驗證登入。
- 登入成功後回傳 top-level 攤平格式，包含 user profile 與 `access_token`。
- 登入失敗一律回 `401 Unauthorized` 與 `INVALID_CREDENTIALS`，避免洩漏帳號是否存在。
- 沿用既有 JWT secret、token 期限與 `JwtService`。

**Non-Goals:**

- 不支援 nickname、email 或其他登入識別。
- 不加入 refresh token、logout、server-side session 或多裝置 session 管理。
- 不修改註冊 API 行為。
- 不變更 password hashing 演算法。

## Decisions

- 登入識別使用 `username` + `password`。
  - 理由：`username` 已具唯一約束，且目前資料模型沒有 email 等其他可登入欄位。
  - 替代方案：支援 nickname/email 登入會增加識別衝突、驗證規則與錯誤語意複雜度，留待後續需求明確後再設計。
- 登入成功 response 採 top-level 攤平格式。
  - 理由：與註冊成功 response 保持一致，API 消費端可用相同資料形狀處理登入後狀態。
  - 替代方案：使用 nested `user` 物件可讓 token 與 profile 邊界更清楚，但會與目前註冊 response 不一致。
- 登入失敗統一回 `INVALID_CREDENTIALS`。
  - 理由：避免透過錯誤訊息判斷帳號是否存在，也讓缺少 username/password 的登入嘗試與其他憑證失敗使用同一個對外語意。
  - 替代方案：缺欄位回 `VALIDATION_ERROR` 可提供更精準表單錯誤，但會讓登入 endpoint 的資訊揭露策略變得不一致。
- 登入 endpoint 公開，其他 protected API 維持 bearer token 驗證。
  - 理由：尚未取得 token 的既有用戶必須能呼叫登入 API。
  - 替代方案：不適用，登入本身就是取得 token 的入口。
- JWT 簽發沿用既有 `JwtService`。
  - 理由：註冊與登入成功後取得 token 的語意相同，重用同一個簽發邏輯可降低行為分歧。
- 用戶身份驗證流程集中在 `UserAuthController` 與 `UserAuthService`。
  - 理由：註冊、登入與後續 logout/refresh/session 類功能都屬於同一個用戶身份驗證流程；集中在同一組 controller/service 可共用 token issuing、profile response mapping 與錯誤處理，避免每新增一個 auth use case 就膨脹一組檔案。
  - 替代方案：保留 `UserRegistrationController` / `UserLoginController` 等一功能一組類別，邊界較細，但目前 POC 功能數量少且 response 與 dependency 高度重複，會增加不必要樣板。

## Risks / Trade-offs

- [Risk] 缺少 username/password 也回 `INVALID_CREDENTIALS`，前端無法取得精準欄位錯誤。→ Mitigation：前端可先做本地必填檢查；API 只暴露統一登入失敗語意。
- [Risk] 沒有 refresh token 時，token 到期後只能重新登入。→ Mitigation：本 change 明確維持 POC 範圍，refresh token 由後續 proposal 設計。
- [Risk] top-level response 會讓 token 與 profile 欄位混在同一層。→ Mitigation：先維持與註冊 response 一致；若後續 API response envelope 統一調整，再用獨立 change 處理。
- [Risk] `UserAuthService` 後續可能累積過多職責。→ Mitigation：目前只集中身份驗證流程；若未來加入帳戶資料修改、註銷等非 auth use case，仍應拆到對應 user/account service。

## Migration Plan

- 使用 `UserAuthController` 承載 register/login endpoint。
- 使用 `UserAuthService` 集中 register/login 流程，共用 token issuing 與 profile response mapping。
- 新增 `LoginRequest` 與共用 `UserAuthResponse` DTO。
- 在 Spring Security 設定中允許 `POST /api/users/login` 未驗證存取。
- 以 `UserRepository` 查詢 username，以 `PasswordEncoder.matches` 驗證密碼。
- 登入成功時使用 `JwtService.issueToken(user.getId())` 產生 access token。
- 新增 API 測試覆蓋成功登入、錯誤憑證、缺少欄位、public login endpoint 與未新增 refresh/logout/session 行為。
- 回滾時移除 login endpoint 相關 DTO 與測試，並還原 SecurityConfig 中 login permit rule。

## Open Questions

- 無。登入識別、response shape、錯誤碼、JWT 設定與非目標項目已確認。
