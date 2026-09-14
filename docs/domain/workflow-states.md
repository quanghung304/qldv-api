# Workflow States — tham chiếu nhanh cho agent

Nguồn đầy đủ: `docs/full/02_Workflow_StateMachine.docx` (v0.2 — bảng transition đầy đủ
từng luồng ở mục 6, dùng làm cấu hình engine thật). File này chỉ giải thích cơ chế chung — khi
cần danh sách 34 transition đầy đủ, lấy nguyên bảng ở mục 6 tài liệu gốc, đừng gõ lại tay.

> **Đã viết lại toàn bộ (thay bản 6-luồng cũ)**: mô hình tổ chức chỉ còn 2 cấp thẩm quyền
> (BTCĐU / cơ sở), dẫn tới CHỈ CÒN 3 luồng phê duyệt (A/B/C) thay vì 6 luồng cũ
> (A1/A2/A3/B1/B2/luồng liên cấp). Khái niệm "đơn vị trực thuộc" và role `R-DVTT` đã LOẠI KHỎI
> DỰ ÁN. Nếu thấy tài liệu/code nào còn nhắc A1/A2/A3/B1/B2 hay `R-DVTT`, đó là thông tin CŨ.

## 8 vai trò tham gia luồng phê duyệt

| Mã | Vai trò | Cấp | Vai trò trong luồng |
|---|---|---|---|
| `R-CV` | Chuyên viên Ban TCĐU | BTCĐU | Tạo hồ sơ, trình kiểm soát |
| `R-KS` | Kiểm soát viên Ban TCĐU | BTCĐU | Kiểm soát, duyệt-chuyển hoặc chuyển trả |
| `R-LD` | Lãnh đạo Ban TCĐU | BTCĐU | Phê duyệt trình cấp có thẩm quyền ngoài phần mềm |
| `R-BPTM` | Cán bộ BPTM cấp cơ sở | Cơ sở | Tạo hồ sơ, trình kiểm soát |
| `R-KSCS` | User kiểm soát cấp cơ sở | Cơ sở | Kiểm soát, duyệt-chuyển hoặc chuyển trả (**giữ lại**, không loại bỏ) |
| `R-PDCS` | Người phê duyệt cơ sở (Bí thư/PBT) | Cơ sở | Phê duyệt/ban hành, hoặc gửi trình lên BTCĐU (Luồng C) |
| `R-ADM`, `R-QTVCS` | Quản trị | — | KHÔNG tham gia luồng phê duyệt (chỉ quản trị tài khoản) |

Chi bộ trực thuộc 1 đảng bộ cơ sở KHÔNG có user/luồng riêng — hồ sơ của nó do đúng 3 user
(`R-BPTM`/`R-KSCS`/`R-PDCS`) của đảng bộ cơ sở chủ quản xử lý, coi như hồ sơ của chính đảng bộ đó.

## Cơ chế chung — cụm trạng thái con lặp lại (áp dụng Luồng A và phần đầu Luồng B/C)

```
Đang thực hiện → [SUBMIT_CONTROL] → Trình kiểm soát
Trình kiểm soát → [RETURN, cần comment] → Đang thực hiện
Trình kiểm soát → [APPROVE_FORWARD] → Trình lãnh đạo ban (chỉ Luồng A; Luồng B/C không có
                                        bước Lãnh đạo riêng, đi thẳng mốc kế tiếp)
Trình lãnh đạo ban → [RETURN, cần comment] → Đang thực hiện (chỉ Luồng A)
Trình lãnh đạo ban → [APPROVE, cần reauth_token] → <mốc kế tiếp> (chỉ Luồng A)
```

4 action này (`SUBMIT_CONTROL`/`RETURN`/`APPROVE_FORWARD`/`APPROVE`) gọi qua **1 endpoint dùng
chung** `POST /cases/{id}/workflow-action` (xem `api-conventions.md`). Ngoài 4 action này, còn có
các **action đặc biệt** (VD `UPLOAD_BTV_RESULT`, `UPDATE_BCH_MINUTES`, `REGISTER_SIGNED_DOC`,
`APPROVE_COMPLETE`, `APPROVE_ISSUE`, `SUBMIT_TO_PARENT`, `RECEIVE_ROUTE`) — các action này KHÔNG
gọi qua endpoint dùng chung, mà do các endpoint nghiệp vụ riêng (SC-04/05/06...) gọi trực tiếp vào
lõi Workflow Engine sau khi tự xử lý xong field riêng của bước đó.

## 3 luồng thẩm quyền (origin_flow)

| Mã | Áp dụng khi |
|---|---|
| `A` | BTCĐU tự khởi tạo và phê duyệt — 17 trạng thái (A-01…A-17), 22 transition, đầy đủ nhất (4 lần lặp cụm dùng chung: trước họp BTV, sau họp BTV, sau họp BCH, ban hành QĐ), dùng làm khuôn mẫu. |
| `B` | Đảng bộ cơ sở tự khởi tạo và phê duyệt — 5 trạng thái (B-01…B-05), 5 transition. Chỉ 1 cấp kiểm soát (`R-KSCS`) rồi thẳng `R-PDCS`, không tách Kiểm soát/Lãnh đạo như Luồng A. |
| `C` | Đảng bộ cơ sở khởi tạo, trình lên BTCĐU — 4 trạng thái (C-01…C-04), 5 transition. Giai đoạn 1 giống Luồng B tới bước "Trình cấp thẩm quyền cơ sở"; khác biệt: `R-PDCS` **gửi trình lên BTCĐU** (`SUBMIT_TO_PARENT`) thay vì tự ban hành. Giai đoạn 2: hệ thống tự điều hướng (`RECEIVE_ROUTE`) về `R-CV`, chạy **nguyên vẹn toàn bộ Luồng A** (không rút gọn). |

Tổng cộng **32 transition** (22 + 5 + 5)

**Quyết định lưu trữ**: 32 transition này HARDCODE trong code (1 file cấu hình dạng danh sách rule
rõ ràng, không rải if/else), KHÔNG lưu bảng CSDL — vì đây là quy trình nghiệp vụ Đảng đã quy định,
hiếm khi đổi, và dự án xác nhận không cần giao diện quản trị cho phần này.

## Đã chốt: transition RECEIVE_ROUTE

Transition `RECEIVE_ROUTE` (C-04 → A-01, điểm bàn giao Luồng C sang Luồng A): khi kích hoạt, hồ sơ
đổi `status_id` sang `A-01`, nhưng `origin_flow` **giữ nguyên `'C'`** (không đổi sang `'A'`) — để
sau này còn biết hồ sơ khởi phát từ cơ sở. Đây cũng là quy tắc chung cho toàn bộ 32 transition:
Workflow Engine không đổi `origin_flow` ở bất kỳ transition nào.

## Trạng thái đặc biệt cần biết

- Trạng thái "Lưu trữ" (`A-16`, `B-04`) tự động kích hoạt (không qua hành động thủ công) khi nhập
  đủ số văn bản đã ký + upload bản scan — không phải người dùng bấm nút.
- Luồng C KHÔNG có trạng thái `FINAL` riêng — sau `RECEIVE_ROUTE`, hồ sơ hoàn thành theo đúng
  `A-17` của Luồng A.

## Ràng buộc số lượng tổ chức đảng theo loại nghiệp vụ biến động

Khi code `POST /cases/changes`, số lượng `organization_ids` gửi lên phải khớp
`PMDV_CASE_TYPE.min/max_organization_count` (xem `data-model.md`):
Giải thể ≥1 · Sáp nhập/Hợp nhất ≥2 · Chia tách =1 (tổ chức nguồn) · Đổi tên ≥1.
(Không thay đổi so với trước — không liên quan tới việc rút gọn luồng phê duyệt.)

## Định hướng đa module (không đổi)

Workflow Engine hiện cài đặt cho `PMDV_CASE` (module Tổ chức Đảng). Dự án sẽ mở rộng thêm 4 module
nghiệp vụ đảng viên dùng chung 1 bảng hồ sơ riêng `PMDV_MEMBER_CASE` (chưa triển khai):
- Engine cần nhận tham số `case_table` (hoặc định danh module tương đương) ở mọi hành động, thay
  vì hardcode thao tác lên `PMDV_CASE` — để tái sử dụng nguyên vẹn cho `PMDV_MEMBER_CASE` sau này.
- `PMDV_CASE_HISTORY` đã polymorphic (`entity_table` + `entity_id`) — xem `data-model.md`.
- Bảng trạng thái/transition mô tả trong file này (mã A/B/C) là RIÊNG cho module Tổ chức Đảng.
  Module đảng viên khi triển khai sẽ có mã luồng/trạng thái riêng, chạy trên cùng 1 engine.
- API dự kiến: `POST /member-cases/{id}/workflow-action` (chưa triển khai) — dùng lại cùng bộ mã
  action và response envelope, chỉ đổi path.