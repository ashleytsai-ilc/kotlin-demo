## Why

- 專案需要提供用戶註冊能力，作為後續登入、修改基本資料與註銷帳戶等 RESTful API 的基礎。
- 目前專案尚未定義用戶資料模型、註冊流程、驗證機制與 in-memory DB 設定，因此需要先建立一致的 API 與安全性基礎。

## What Changes

- 新增用戶註冊 API，用於建立新用戶帳戶，註冊成功後立即回傳 JWT。
- 用戶資料欄位包含 `id`、`username`、`nickname`、`password`、`createdAt`、`updatedAt`。
- `username` 必須唯一，重複註冊時應回傳明確錯誤。
- `username` 僅允許英數字與底線，長度最少 8 個字元、最多 15 個字元。
- `nickname` 不必填，空白視為未提供；若提供則不可重複，且最多 30 個字元。
- `password` 最小長度為 8 個字元，且必須包含英文大寫、英文小寫、數字與至少一個特殊符號。
- `password` 不得以明文儲存，應使用安全雜湊方式保存。
- 錯誤回應格式需統一包含 `code`、`message`、`details`。
- DB 使用 H2 in-memory database，適合 POC 與測試階段快速啟動。
- `id` 不使用單純流水號，固定使用 ULID 作為有序且不易推測的字串識別碼。
- 用戶資料需記錄建立時間與更新時間。
- 新增 JWT 相關基礎能力，讓註冊後的用戶可銜接後續受保護 API 的驗證流程。
- 登入 API 不包含在本次 change，將由後續 proposal 處理。
- 新增對應的測試，依專案 TDD 流程先描述註冊行為與錯誤情境。
- 不包含 UI 變更。
- 不包含正式 production database 遷移。

## Capabilities

### New Capabilities

- `user-registration`: 定義用戶註冊 API、用戶資料欄位、唯一 username、密碼儲存與註冊錯誤情境。
- `jwt-authentication`: 定義 JWT 驗證基礎能力，包含 token 發放或驗證策略，以及後續受保護 API 的驗證行為。

### Modified Capabilities

- 無。

## Impact

- 受影響程式碼：
  - `src/main/java/com/example/demo`
  - `src/test/java/com/example/demo`
  - `src/main/resources/application.yaml`
  - `pom.xml`
- 可能新增的 Spring 元件：
  - REST controller
  - service
  - repository
  - JPA entity
  - DTO/request/response model
  - security/JWT support classes
- 可能新增的 dependencies：
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - H2 database
  - Spring Security OAuth2 JOSE / Resource Server
  - `ulid-creator` ULID generator library
- API 影響：
  - 新增註冊 endpoint。
  - 註冊成功 response 以 `access_token` 回傳 JWT，讓前端可直接登入使用。
  - 登入 endpoint 延後至下一個 proposal。
  - 後續若加入登入或受保護 API，將使用 JWT 作為驗證方式。
- 資料影響：
  - H2 in-memory DB 會在應用程式重啟後清空資料。
  - 用戶 ID 使用 ULID，保留時間排序特性，但不暴露單純流水號。
  - 用戶資料保存 `createdAt` 與 `updatedAt`，用於後續查詢、除錯與稽核。
- 受影響團隊：
  - 後端 API 開發。
  - API 消費端或前端整合者。
  - 測試與 QA。
- 回滾計畫：
  - 移除新增的 controller、service、repository、entity、security/JWT 類別與測試。
  - 還原 `pom.xml` 中新增的 dependencies。
  - 還原 `application.yaml` 中新增的 H2/JPA/JWT 設定。
  - 若只需停用外部入口，可先移除或停用註冊 endpoint mapping。
