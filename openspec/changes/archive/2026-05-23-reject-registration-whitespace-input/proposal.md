## Why

目前註冊流程會先對 `username` 與 `nickname` 做 trim，且空白 `nickname` 會被視為未提供，這會讓 API 實際儲存與驗證的值不同於用戶端送出的原始輸入。
為了讓註冊輸入規則更明確，`username` 與 `nickname` 應以原值驗證，不允許空白字元，也不應在服務端自動正規化。

## What Changes

- **BREAKING**：註冊時 `username` SHALL NOT 被 trim；若包含任何空白字元，請求 SHALL 被拒絕。
- **BREAKING**：註冊時 `nickname` SHALL NOT 被 trim；若提供的 `nickname` 是空白或包含任何空白字元，請求 SHALL 被拒絕。
- `nickname` 仍維持可省略；省略或 JSON null 代表沒有 nickname。
- 更新註冊輸入驗證規格與測試案例，避免驗證元件、service 或 DTO mapper 隱性 trim 使用者輸入。

## Capabilities

### New Capabilities

- 無。

### Modified Capabilities

- `user-registration`：調整註冊輸入驗證規則，將 `username` 與 `nickname` 改為不 trim 且不允許空白字元。

## Impact

- 影響 API：`POST /api/auth/register` 的輸入驗證行為會改變。
- 影響程式碼：註冊 DTO、註冊驗證邊界、service 入口或相關 mapper 中對 `username` / `nickname` 的 trim 與空白處理。
- 影響測試：既有「空白 nickname 視為未提供」測試需改為驗證空白 nickname 會回傳 `400 Bad Request`。
- 影響團隊：後端需調整驗證邏輯與測試；API 使用方需改為送出不含空白的 `username` / `nickname`。
- 回滾方式：若需回復舊行為，恢復 `user-registration` 規格中空白 nickname 視為省略的要求，並還原註冊驗證流程對 `username` / `nickname` 的 trim/空白轉 null 行為。
