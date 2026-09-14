# Permission Rules — tham chiếu nhanh cho agent

Nguồn đầy đủ: `docs/full/03_PermissionMatrix.docx` (có ma trận Role x Function x Action
đầy đủ 10 nhóm chức năng, kèm điều kiện Y-đk chi tiết từng ô). File này chỉ liệt kê 9 vai trò
và nguyên tắc chung — **khi code 1 API cụ thể, luôn mở tài liệu gốc để tra đúng ô Role x
Function x Action tương ứng, đừng đoán.**

## Nguyên tắc bắt buộc

**RBAC kiểm tra ở tầng API (middleware), không chỉ ẩn/hiện nút trên giao diện.** Mọi request
đều phải qua bước: xác định `role_id` của user → đối chiếu `function_code` + hành động đang
gọi → nếu không đủ quyền, trả `403` kèm mã lỗi `ERR-GL-02` — **trước khi** chạm vào business
logic của controller.

## 9 vai trò hệ thống

| Mã | Tên vai trò | Đơn vị | Phạm vi dữ liệu |
|---|---|---|---|
| `R-ADM` | Quản trị viên hệ thống | Trung tâm QLDL | Toàn hệ thống — CHỈ quản trị tài khoản/phân quyền, KHÔNG thao tác nghiệp vụ hồ sơ |
| `R-CV` | Chuyên viên Ban TCĐU | Ban TCĐU | Toàn bộ tổ chức đảng thuộc Đảng bộ Agribank |
| `R-KS` | Kiểm soát viên Ban TCĐU | Ban TCĐU | Như R-CV; có quyền phê duyệt/chuyển trả, KHÔNG có quyền khởi tạo hồ sơ mới |
| `R-LD` | Lãnh đạo Ban TCĐU | Ban TCĐU | Như R-CV; có quyền phê duyệt trình cấp trên, KHÔNG có quyền khởi tạo hồ sơ mới |
| `R-QTVCS` | Quản trị viên cơ sở | Đơn vị cơ sở | Chỉ quản trị tài khoản user thuộc đơn vị mình; KHÔNG thao tác nghiệp vụ hồ sơ |
| `R-BPTM` | Cán bộ BPTM cấp cơ sở | Đơn vị cơ sở | Chỉ tổ chức đảng do đơn vị cơ sở mình quản lý |
| `R-PDCS` | Người phê duyệt cơ sở (Bí thư/Phó Bí thư) | Đơn vị cơ sở | Như R-BPTM; có quyền phê duyệt/chuyển trả, KHÔNG có quyền khởi tạo hồ sơ mới |
| `R-KSCS` | User kiểm soát cấp đảng bộ cơ sở | Đảng bộ cơ sở | Chỉ tổ chức đảng do đảng bộ cơ sở mình quản lý |

## 10 nhóm chức năng (function_code) để đối chiếu ma trận

`FN1` Khởi tạo & chỉnh sửa hồ sơ nghiệp vụ · `FN2` Xử lý luồng phê duyệt · `FN3` Lưu trữ hồ sơ ·
`FN4` Tra cứu hồ sơ · `FN5` Đính kèm tài liệu · `FN6` Sinh văn bản tự động ·
`FN7` Danh sách tổ chức đảng (chỉ hệ thống ghi, mọi vai trò khác chỉ Xem) · `FN8` Báo cáo/thống
kê *(không triển khai — xem AGENTS.md)* · `FN9` Quản trị người dùng & phân quyền (chỉ
`R-ADM`, `R-QTVCS`) · `FN10` Cấu hình danh mục hệ thống (chỉ `R-ADM`, ngoài phạm vi hiện tại)

6 hành động mỗi function: Xem / Tạo / Sửa / Xóa / Duyệt / Xuất — giá trị Y (có quyền) /
N (không) / Y-đk (có điều kiện, tra chi tiết trong tài liệu gốc).

## Quy tắc luôn đúng bất kể function nào

- `R-ADM` và `R-QTVCS` **không** thao tác dữ liệu nghiệp vụ (hồ sơ, tổ chức đảng) — chỉ quản
  trị tài khoản.
- `R-KS`, `R-LD`, `R-PDCS`, `R-KSCS` **không** có quyền Tạo hồ sơ mới — chỉ Sửa (khi đang giữ
  hồ sơ ở bước của mình), Duyệt, Xem, Xuất.
- Tệp đính kèm: sau khi hồ sơ chuyển "Hoàn thành", **không ai** (kể cả `R-ADM`) được xóa —
  đối chiếu `PMDV_ATTACHMENT.is_locked` trong `data-model.md`.
