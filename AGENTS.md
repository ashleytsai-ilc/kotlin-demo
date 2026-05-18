# 儲存庫指南

## 專案結構與模組組織

- 這是一個小型 Spring Boot Maven 專案。
- 應用程式程式碼位於 `src/main/java/com/example/demo`。
- `DemoApplication.java` 是主要進入點。
- 執行時設定應放在 `src/main/resources/application.yaml`。
- 測試會在 `src/test/java/com/example/demo` 下對應主要套件結構。
- 請讓測試類別靠近其驗證的程式碼。
- Maven wrapper 檔案（`mvnw`、`mvnw.cmd`、`.mvn/wrapper/`）應保留在版本控制中，讓貢獻者使用相同的建置進入點。
- OpenSpec 專案中繼資料位於 `openspec/`。
- 請將提案變更放在 OpenSpec 目錄下，而不是把規劃文件混入原始碼套件。

## 建置、測試與開發指令

- `./mvnw spring-boot:run`: 在本機啟動應用程式。
- `./mvnw test`: 執行 JUnit 測試套件。
- `./mvnw package`: 編譯、測試，並在 `target/` 中建置應用程式 jar。
- `./mvnw clean`: 移除產生的建置輸出。

- 專案以 Java 17 為目標，如 `pom.xml` 中所宣告。
- 在診斷建置失敗前，請先使用 `java -version` 確認版本。

## 開發流程

- 開發方式採用 TDD。
- 新增或修改功能前，先撰寫能描述預期行為的測試。
- 先執行測試並確認它因尚未實作而失敗。
- 接著撰寫最小可行的實作，讓測試通過。
- 測試通過後再重構程式碼，並保持測試持續通過。
- 修正 bug 時，先加入能重現問題的失敗測試，再進行修正。
- 除非只是文件、格式或純設定調整，否則程式碼變更應附上相對應的測試。

## 程式碼風格與命名慣例

- 使用標準 Java 慣例。
- 使用 4 個空格縮排。
- 大括號與宣告放在同一行。
- 類別使用 `PascalCase`。
- 方法與欄位使用 `camelCase`。
- 套件名稱使用小寫。
- 請將 Spring 元件放在 `com.example.demo` 或其子套件下，讓元件掃描能找到它們。
- 新增 service 或 controller 時，依賴項目優先使用建構子注入。
- 若邏輯沒有明顯重用、無法降低複雜度，或抽出後只是增加跳轉成本，則不需要特別抽成獨立函式；優先保持呼叫點直接且可讀。
- 開始重複出現的邏輯優先考慮使用函式包裝呼叫。
- `application.yaml` 中的設定鍵請保持小寫，並以點號分隔。
- Java 內部欄位與方法維持 `camelCase`。
- 專案中不得散落 magic number 或 magic string；業務規則、錯誤 code、錯誤 message、API field 名稱、資料庫 table/column/constraint 名稱與長度限制，都應集中定義為常數、enum 或 catalog。
- API error 的 `code` 與對外 `message` 應透過共用 catalog 對應，不要在 throw site 直接寫字串。
- API error detail 的 `field` 應透過欄位 enum 或同等集中定義管理，再於 response 邊界轉成字串。
- 所有 API response 的 JSON key 必須使用 `snake_case`。
- DTO 對外輸出時，若 Java 欄位為 `createdAt`、`updatedAt`，JSON key 應為 `created_at`、`updated_at`。
- token 類 response key 應使用 `access_token` 這類 `snake_case` 命名，不使用 `accessToken`。
- 若有未使用的變數、函式等均需移除，避免出現程式碼雜訊。

## 測試指南

- 測試透過 `spring-boot-starter-test` 使用 JUnit Jupiter。
- Spring context 測試類別請以 `Tests` 作為後綴。
- 聚焦的單元測試請以 `Test` 作為後綴。
- 測試方法名稱應具描述性，例如 `createsTodoWhenInputIsValid`。
- 新的業務邏輯應新增聚焦的單元測試。
- 測試應優先描述可觀察行為，而不是實作細節。
- 只有在 Spring context 是待驗證行為的一部分時，才使用 `@SpringBootTest`。
- 每次完成 red-green-refactor 循環後，請執行相關測試。
- 提交變更前請執行 `./mvnw test`。
- 不存在或不在此次實作範圍的功能，無需特地加入 not found 測試。

## Commit 與 Pull Request 指南

- 此目錄目前不是 Git 儲存庫，因此沒有可用的本機提交紀錄。
- 請使用簡潔、祈使語氣的 commit message，例如 `Add health check endpoint` 或 `Fix application config loading`。
- Pull request 應包含簡短摘要。
- Pull request 應包含測試證據，例如 `./mvnw test`。
- 適用時，Pull request 應連結相關 issue 或 OpenSpec 變更。

## 注意事項
- 在實作如 JWT、ULID 等常見功能時，先在網路上搜尋是否有合適的套件可使用，如果有則提供出來供用戶決策，做出決策後再繼續進行後續任務。

## Agent 專用指示

- 請勿將產生的檔案或暫存檔放入原始碼目錄。
- 使用 OpenSpec 時，請在 `openspec/changes/<change-name>/` 下建立與更新 artifacts。
- 只在變更任務明確後才開始實作，有任何設計上或實作上會遇到的問題包含你的建議，都提出來幫助用戶決策。
- 在每次變更結束後，都做出變更總結，包含改動哪些檔案、改動了什麼。
- 在每次實作結束後提交給用戶前，都必須先進行自我校驗、review 這次實作做的改動是否符合需求、是否有需要清理的 useless code。
