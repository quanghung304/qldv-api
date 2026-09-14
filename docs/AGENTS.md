# AGENTS.md — Module Tổ chức Đảng (Agribank)

Hướng dẫn cho AI coding agent (Claude Code, Codex, Cursor...) khi làm việc trong repo này.
File này CHỈ trỏ đường dẫn — không nhắc lại chi tiết. Đọc file trong `docs/domain/` tương ứng
phần việc đang làm, đừng tải hết mọi file cùng lúc.

> Lưu ý cho Claude Code: file này dùng chung cho mọi agent. Nếu bạn có CLAUDE.md, dòng đầu tiên
> của CLAUDE.md nên là `@AGENTS.md` để import file này thay vì copy nội dung.

## Dự án này là gì

Module quản lý nghiệp vụ Tổ chức Đảng (Thành lập / Giải thể / Sáp nhập / Hợp nhất / Chia tách /
Đổi tên tổ chức đảng) thuộc phần mềm "Tổ chức xây dựng Đảng trong Đảng bộ Agribank".

## Tech stack

- **Backend**: Java + Spring Boot (Maven multi-module: `qldv-api`, `qldv-db`, `qldv-utils`).
- **API framework**: Spring Web MVC, Spring Security, Spring Validation, OpenFeign, Springdoc OpenAPI.
- **ORM / persistence**: Spring Data JPA / Jakarta Persistence, Hibernate via Spring Boot.
- **CSDL**: Oracle Database (`ojdbc11`).
- **Auth**: Spring Security + JWT (`jjwt`); SSO ngoài xử lý đăng nhập/đăng xuất/refresh token.
- **Frontend**: Chưa có source FE trong repo này; không tự giả định framework FE khi làm việc.

## Lệnh thường dùng

  

## Tài liệu thiết kế (đọc khi cần, theo đúng phần việc)

| Đang làm gì | Đọc file |
|---|---|
| Tạo/sửa bảng CSDL, viết migration, entity/model | `docs/domain/data-model.md` |
| Viết endpoint, request/response, mã lỗi | `docs/domain/api-conventions.md` |
| Kiểm tra quyền, viết middleware phân quyền | `docs/domain/permission-rules.md` |
| Xử lý chuyển trạng thái hồ sơ, luồng duyệt | `docs/domain/workflow-states.md` |
| Xác định module/layer, tổ chức thư mục | `docs/agent/architecture.md` |
| Naming, Lombok, validation, response convention | `docs/agent/coding-convention.md` |
| Quy trình trước/sau khi sửa code | `docs/agent/workflow.md` |

Tài liệu gốc đầy đủ (văn xuôi, có ghi chú GC-xx bàn về các điểm URD chưa rõ) nằm ở
`docs/full/` — chỉ mở khi cần lý do đằng sau một quyết định thiết kế, không dùng làm nguồn
code hằng ngày.

## Quy ước bắt buộc (agent không thể tự suy ra từ code)

- **Tên bảng CSDL**: `PMDV_{TÊN_MODEL}` viết hoa, VD `PMDV_ORGANIZATION`, `PMDV_CASE`, `PMDV_USER`.
- **Tên field, endpoint, request/response**: tiếng Anh, snake_case cho field DB, camelCase cho JSON.
- **Mô tả, comment, thông báo lỗi hiển thị cho người dùng**: tiếng Việt.
- **RBAC kiểm tra ở tầng API (middleware), không chỉ ẩn nút trên giao diện** — mọi endpoint đều
  phải qua kiểm tra quyền, không có ngoại lệ. Chi tiết: `docs/domain/permission-rules.md`.
- **Chuyển trạng thái hồ sơ chỉ qua 1 endpoint dùng chung** (`POST /cases/{id}/workflow-action`,
  tham số `action`) — không tự viết logic chuyển trạng thái riêng ở nơi khác. Chi tiết:
  `docs/domain/workflow-states.md`.

## Ranh giới — không tự ý mở rộng

- KHÔNG đổi mã trạng thái hồ sơ (`A-01`, `B-03`...) hay mã hành động (`SUBMIT_CONTROL`...) —
  các mã này dùng chung xuyên suốt CSDL, API, và tài liệu nghiệp vụ; đổi ở 1 chỗ mà không đổi
  đồng bộ sẽ làm sai lệch toàn hệ thống.

## Khi gặp điểm chưa rõ

Nếu yêu cầu chạm vào phần được đánh dấu "cần xác nhận" trong `docs/domain/*.md`, DỪNG lại và hỏi người giao việc thay vì tự suy đoán rồi code — các điểm này đều là chỗ tài liệu nghiệp vụ gốc (URD) chưa quy định rõ, đoán sai sẽ phải sửa lại toàn bộ.

## Cách review kết quả agent làm

- Đối chiếu lại đúng danh sách trường/ràng buộc trong `docs/domain/data-model.md` và
`docs/domain/api-conventions.md` trước khi báo hoàn thành một API.
- Chỉ thực hiện code, không cần compile và build lại service.
