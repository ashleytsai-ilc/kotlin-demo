## Context

註冊 API 目前會在輸入驗證流程中處理 `username` 與 `nickname` 的 trim/空白轉換，再進入後續檢查。這會造成用戶端送出的原始值與系統驗證、儲存、唯一性檢查使用的值不同。

本變更將輸入驗證定義為「以原始輸入值為準」，避免 DTO binding、mapper、service 或驗證元件隱性改寫 `username` 與 `nickname`。`nickname` 仍可省略或為 JSON null，但只要用戶端提供非 null 值，該值就必須是合法 nickname。

## Goals / Non-Goals

**Goals:**

- `username` 與 `nickname` 驗證前不做 trim，且接受後的值必須等於用戶端提交值。
- `username` 不允許任何空白字元。
- `nickname` 可省略或為 JSON null；若提供非 null 值，則不允許 blank 或任何空白字元。
- 更新測試，明確覆蓋 blank nickname、含空白 nickname、含空白 username 的行為。

**Non-Goals:**

- 不調整 password 的驗證與正規化規則。
- 不導入新的 validation framework 或新的外部依賴。
- 不變更 response JSON shape、錯誤 code catalog 或唯一性衝突語義。

## Decisions

1. 以原始 request value 做驗證，不在註冊流程任何驗證前置步驟中 trim `username` / `nickname`。

   理由：這能讓 API 合約與用戶端實際送出的資料一致，避免 `" alice "` 被默默轉成 `"alice"` 後成功註冊，造成用戶端誤判可接受格式。

   替代方案：保留 trim 並只禁止中間空白。此做法仍會隱性修改輸入，不符合「不 trim」的新需求。

2. `nickname` 的 optional 語義只保留在欄位省略或 JSON null，不再包含 blank string。

   理由：blank string 是用戶端有提供欄位但值無效，應回傳 validation error；省略欄位才代表沒有 nickname。

   替代方案：blank string 繼續視為 omitted。此做法與「nickname 輸入不允許空白」衝突，也會讓 validator 保留特殊轉換邏輯。

3. 空白字元檢查使用一致的業務規則實作。

   理由：`username` 與 `nickname` 都需要禁止空白，應避免在多處散落不同判斷式。若專案使用 DTO Bean Validation、共用 validation rules、annotation 常數或自訂 validator，應將規則放在註冊流程所有入口都會共用的位置。

   替代方案：只在 controller 或單一 service 呼叫點直接寫條件判斷。這會讓其他入口可能繞過同一套規則，也會降低驗證職責的清晰度。

## Risks / Trade-offs

- [Risk] 這是 breaking behavior change，原本送出 blank nickname 仍可註冊的用戶端會開始收到 `400 Bad Request`。→ Mitigation：在 proposal/spec 中標明 breaking，測試覆蓋新行為。
- [Risk] 若實作只移除 trim，卻未補上空白字元檢查，可能讓帶前後空白的 username/nickname 被儲存。→ Mitigation：新增 API 測試與聚焦的註冊驗證測試，覆蓋 leading/trailing/internal whitespace。
- [Risk] 若僅在 controller 層驗證，其他註冊入口可能仍可繞過規則。→ Mitigation：將規則放在註冊流程共用的驗證邊界，確保 DTO validation、mapper、service 或自訂 validator 不會各自產生不一致行為。

## Migration Plan

1. 先更新 OpenSpec delta，確認合約改為不 trim 並禁止空白。
2. 新增或調整 registration 測試，先看到舊行為失敗。
3. 調整註冊驗證流程與相關 rules，移除 `username` 與 `nickname` 的 trim/blank-to-null 邏輯，補上空白字元檢查。
4. 執行相關測試與完整測試。
5. 若需回滾，還原 OpenSpec 變更與註冊驗證流程中的 trim/blank-to-null 行為，並恢復 blank nickname 視為 omitted 的測試。

## Open Questions

- 無。
