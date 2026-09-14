# API Conventions — tham chiếu nhanh cho agent

Nguồn đầy đủ: `docs/full/05_APIContract.docx`. File này chỉ liệt kê quy ước chung + danh sách
endpoint — mở tài liệu gốc khi cần xem đúng field/JSON mẫu của 1 endpoint cụ thể.

## Quy ước bắt buộc

- Base URL: `/api/v1`
- Header bắt buộc mọi request: `Authorization: Bearer <access_token>` (token do service SSO
  ngoài cấp, xác thực qua `JwtTokenFilter`)
- Response thành công:
  ```json
  { "success": true, "message": "...", "data": { ... } }
  ```
  Danh sách có phân trang: `data` là object `{ "totalPages": 1, "currentPage": 1, "totalItems": 20, "data": [ ... ] }` (không có field `meta` riêng ở cấp ngoài).
- Response lỗi:
  ```json
  { "success": false, "message": "..." }
  ```
  Lỗi validate (`MethodArgumentNotValidException` hoặc `FieldValidationException` — dùng khi
  1 request có thể sai nhiều field cùng lúc, VD API Thành lập TCĐ) trả thêm `data` là map
  `{ "field": "message" }`, gom đủ lỗi thay vì chỉ trả lỗi đầu tiên.
- HTTP status: 200 thành công · 400 lỗi validate/business rule (mặc định) · 403 RBAC từ chối
  hoặc sai thông tin đăng nhập · 404 không tìm thấy
- Phân quyền tầng route: annotation `@RequirePermission(function, action)` trên method đối
  chiếu bảng `PMDV_ROLE_PERMISSION` theo role_code của user hiện tại; `@NoPermissionCheck` (class
  hoặc method) bỏ qua kiểm tra. Route không gắn annotation nào thì middleware không can thiệp.
  Ngoại lệ: API quản trị mẫu văn bản (`/document-templates/*`) KHÔNG gắn `@RequirePermission` —
  FN10 (cấu hình danh mục) chưa được seed vào Permission Matrix, kiểm tra role `R-ADM` hardcode
  ngay trong service (`DocumentTemplateService`) thay vì qua middleware, cho tới khi FN10 chính
  thức được đưa vào scope.
- File tải về (attachment, văn bản sinh tự động) LUÔN proxy qua backend — kiểm tra RBAC + scope
  trước khi đọc S3 rồi mới stream response, KHÔNG dùng presigned URL.
- Phân trang danh sách: query `page`, `pageSize`, tuỳ endpoint
- Ngày: `YYYY-MM-DD`; ngày giờ có timezone

## Danh sách endpoint

### Xác thực & người dùng hiện tại
- `GET /user/info` — thông tin user hiện tại (theo token)
- `POST /auth/register` — tạo tài khoản user (`@RequirePermission(FN9, CREATE)`)

### Danh mục dùng chung
- `GET /categories/organization-types` · `GET /categories/case-types` ·
  `GET /categories/statuses` (kèm `step_no`/`step_code`/`step_name` — FE deep-link đúng màn hình
  sửa theo bước, xem `data-model.md`) · `GET /categories/document-types` (`@NoPermissionCheck`,
  chỉ cần token hợp lệ; danh mục này KHÔNG còn gắn với cơ chế sinh văn bản, xem `data-model.md`)
- `GET /role/get-all-role` · `GET /role/get-by-id?id=`

### Quản trị người dùng
- `POST /user/search` (`@RequirePermission(FN9, VIEW)`)
- `GET /user/{id}` (`@RequirePermission(FN9, VIEW)`)
- `PUT /user/update` (`@RequirePermission(FN9, EDIT)`)
- `PUT /user/active` — khóa/mở khóa (`@RequirePermission(FN9, DELETE)`)
- `PUT /user/change-password`
- `DELETE /user/delete/{id}`
- `PUT /user/update-user-requested`
- `POST /user-role/add` · `POST /user-role/update` · `POST /user-role/assign`

### Tổ chức đảng & cán bộ
- `POST /organizations` — tìm kiếm; chỉ trả tổ chức "đỉnh" của mỗi nhánh khớp điều kiện (ẩn
  bớt nếu tổ tiên của nó cũng khớp — dùng `/subordinates` bên dưới để mở rộng xem con cháu)
  (`@RequirePermission(FN7, VIEW)`)
- `GET /organizations/{id}` (`@RequirePermission(FN7, VIEW)`)
- `GET /organizations/{id}/committee-members` (`@RequirePermission(FN7, VIEW)`)
- `GET /organizations/{organizationId}/subordinates` — toàn bộ tổ chức trực thuộc (con cháu mọi
  cấp) đang hoạt động, kèm `level` theo độ sâu (`@RequirePermission(FN7, VIEW)`)

### Hồ sơ nghiệp vụ (khung chung)
- `POST /cases` — tìm kiếm theo bộ lọc (body, KHÔNG phải `GET /cases`) (`@RequirePermission(FN4, VIEW)`)
- `GET /cases/{id}` · `GET /cases/{id}/history` (`@RequirePermission(FN4, VIEW)`)
- `DELETE /cases/{id}` (API-GL-01) — hard-delete, CHỈ khi hồ sơ còn "Đang thực hiện" lần đầu
  (chưa từng qua action `SUBMIT_CONTROL`, `PMDV_CASE_HISTORY` rỗng); cascade xóa `PMDV_ATTACHMENT`
  liên quan (`@RequirePermission(FN1, DELETE)`)

### Luồng phê duyệt — dùng chung mọi loại hồ sơ
`POST /cases/{id}/workflow-action` (`@RequirePermission(FN2, APPROVE)`) — body:
`{ "action": "...", "comment": "..." }` (không còn `reauth_token` — đã bỏ yêu cầu xác thực lại;
field này bị bỏ qua nếu client vẫn gửi lên, không lỗi).
`action` ∈ `SUBMIT_CONTROL | RETURN | APPROVE_FORWARD | APPROVE` (enum `ECaseWorkflowAction`,
`qldv-utils/enums`). `comment` bắt buộc khi RETURN (thiếu → lỗi ERR-SC03-02). Guard (rule khớp
theo `flow_code`/trạng thái hiện tại/action, role_code khớp) tra theo cấu hình transition hardcode
ở `CaseWorkflowConfig` (qldv-api) — xem `workflow-states.md`. Các action đặc biệt
(`REGISTER_SIGNED_DOC`, `APPROVE_COMPLETE`, `APPROVE_ISSUE`, `SUBMIT_TO_PARENT`, `RECEIVE_ROUTE`)
KHÔNG gọi qua endpoint này — do các endpoint nghiệp vụ riêng gọi trực tiếp lõi Workflow Engine.

### Thành lập TCĐ cấp Agribank
- `POST /cases/establishments` — API-SC02-01, tạo mới Bước 1 (15 field + `btv_method` nullable —
  hình thức xử lý Ban Thường vụ DỰ KIẾN, không bắt buộc, không chặn Trình kiểm soát nếu thiếu)
  (`@RequirePermission(FN1, CREATE)`)
- `PUT /cases/{id}/establishment` — API-SC02-02, cập nhật một phần (field null = không đổi),
  guard ở service: hồ sơ đang ở bước 1 (`A-01`/`B-01`/`C-01` tuỳ `origin_flow`) HOẶC đang ở bước
  "Trình kiểm soát" kế tiếp (`A-02`/`B-02`/`C-02`) — mở cho Kiểm soát viên (`R-KS` Luồng A,
  `R-KSCS` Luồng B/C) đang được giao xử lý hồ sơ ở bước đó cũng sửa được; cả 2 trường hợp đều
  chặn thêm bằng `assignedUserId` (chỉ đúng người đang được giao mới sửa được, xem
  `WorkflowAssigneeGuard`) (`@RequirePermission(FN1, EDIT)`)
- `GET /cases/{id}/establishment` — chi tiết đầy đủ 15 field (`@RequirePermission(FN1, VIEW)`)
- `PUT /cases/{id}/establishment/board-review` — API-SC04-01, ghi nhận ý kiến Ban Thường vụ
  (họp trực tiếp hoặc lấy phiếu), áp dụng khi hồ sơ ở `A-04` (R-CV nhập liệu) HOẶC `A-06`
  ("Trình kiểm soát (trước họp BCH)", R-KS) + `assignedUserId` (`@RequirePermission(FN1, EDIT)`)
- `GET /cases/{id}/establishment/board-review` (`@RequirePermission(FN1, VIEW)`)
- `PUT /cases/{id}/establishment/committee-review` — API-SC05-01, Bước 3 giai đoạn 1 (trước ban
  hành), guard status `A-08` (R-CV) HOẶC `A-10` ("Trình kiểm soát (ban hành QĐ)", R-KS) +
  `assignedUserId` ở service — KHÔNG hardcode role trong code, việc phân biệt R-CV/R-KS suy ra từ
  `assignedUserId` do Workflow Engine tự gán đúng vai trò khi `SUBMIT_CONTROL`
  (`@RequirePermission(FN1, EDIT)`)
- `GET /cases/{id}/establishment/committee-review` (`@RequirePermission(FN1, VIEW)`)
- `PUT /cases/{id}/establishment/decision-documents` — API-SC05-02, Bước 3 giai đoạn 2 (sau ban
  hành), guard status CHỈ `A-12` (R-CV, chưa mở cho Kiểm soát viên) — cho lưu nháp từng phần, ghi
  3 văn bản (Quyết định thành lập/Quyết định chuẩn y cấp ủy/Kết luận tiêu chuẩn chính trị).
  KHÔNG tự động chuyển trạng thái — sau khi đủ `document_no`/`document_date`/`effective_date` +
  scan đính kèm cho cả 3 văn bản (response trả field `missing` để biết còn thiếu gì), R-CV phải tự
  gọi `POST /cases/{id}/workflow-action` (action `SUBMIT_CONTROL`) để trình kiểm soát (`A-12→A-13`)
  (`@RequirePermission(FN1, EDIT)`)
- `POST /cases/{id}/archive` — API-SC06-01, kích hoạt Lưu trữ (guard case đã đạt `A-14`/`B-04` —
  Luồng A dùng lại mã `A-14` cho "Lưu trữ", không phải `A-16` cũ, xem `ECaseStatusCode`; guard
  status hiện đang bị comment tắt trong `ArchiveCaseService`, chỉ còn check role thuộc luồng qua
  `CaseFlowRoleGuard`); sinh 2 văn bản lưu trữ qua engine sinh văn bản dùng chung, cần Admin seed
  sẵn `PMDV_DOCUMENT_TEMPLATE` với `workflow_stage="A-14"`/`"B-04"` (`@RequirePermission(FN3, APPROVE)`)
- `POST /cases/{id}/complete` — API-SC06-02, phê duyệt Hoàn thành: tạo CHÍNH THỨC
  `PMDV_ORGANIZATION` + `PMDV_COMMITTEE_MEMBER` (status=OFFICIAL) trong 1 transaction ở qldv-db,
  set `case.completed_at` (`@RequirePermission(FN3, APPROVE)`)

### Đính kèm tài liệu (dùng chung mọi loại hồ sơ, FN5)
Cơ chế CỘNG DỒN — mỗi lần upload chỉ ghi thêm file mới, KHÔNG xóa/đụng tới file đã có; xóa từng
file cụ thể qua endpoint DELETE riêng, xem `data-model.md`.
- `POST /api/v1/attachments/{caseId}` — multipart `files[]`, ghi thêm vào danh sách tài liệu hiện
  có (không thay thế); validate đuôi + magic bytes (pdf/doc/docx/jpg/jpeg/png), ≤20480KB/file, tổng
  ≤`attachment.max-total-size-kb` (config), không trùng tên trong 1 lượt (`@RequirePermission(FN5, CREATE)`)
- `GET /api/v1/attachments/{caseId}?workflow_stage=` — không truyền `workflow_stage` → trả toàn
  bộ tài liệu mọi bước (`@RequirePermission(FN5, VIEW)`)
- `GET /api/v1/attachments/{id}/download` — proxy S3, `Content-Disposition: inline` (FE tự xem
  trước PDF/ảnh hoặc tải về) (`@RequirePermission(FN5, VIEW)`)
- `DELETE /api/v1/attachments/{id}` — chỉ khi hồ sơ chứa tài liệu CHƯA ở trạng thái Hoàn thành
  (`@RequirePermission(FN5, DELETE)`)

### Mẫu văn bản & sinh văn bản tự động (dùng chung mọi loại hồ sơ, lần 3 — DocumentContentProvider)
Đọc động 100% từ `PMDV_DOCUMENT_TEMPLATE` (`status=ACTIVE`) theo
case_type_id/authority_level/workflow_stage — KHÔNG hardcode danh sách văn bản theo case_type ở
bất kỳ đâu (xem `data-model.md`). Toàn bộ API nhóm này gộp về CHUNG 1 gốc URL
`/api/v1/document-templates` (1 controller, `DocumentTemplateController`). **BREAKING CHANGE** —
thay thế hoàn toàn route `/api/v1/generated-documents/*` cũ (Resolver Registry/field_mapping_config,
đã xoá) và 2 bản nháp trung gian trước đó (`/api/v1/cases/{caseId}/document-templates`/`.../documents/*`,
rồi `/api/v1/document-templates/cases/{caseId}/{templateId}/...`), không giữ song song nhiều bộ
endpoint.

Quản trị mẫu (Admin, R-ADM — FN10 chưa seed permission):
- `POST /api/v1/document-templates` — multipart, upload file `.docx` (placeholder `[ten_field]`),
  body `case_type_id`/`authority_level`/`workflow_stage`/`template_code`/`condition_key`
  (nullable)/`template_name`/`generator_key` (gõ tay, định danh hàm Java sinh nội dung — KHÔNG cần
  khớp bean `DocumentContentProvider` nào có sẵn ngay lúc upload); LUÔN kích hoạt `ACTIVE` ngay (và
  deactivate các bản ACTIVE khác cùng tổ hợp) — không còn trạng thái `PENDING_REVIEW` chờ mapping
  như thiết kế Resolver Registry cũ
- `GET /api/v1/document-templates/{id}` · `GET /api/v1/document-templates?case_type_id=&authority_level=&workflow_stage=&template_code=&status=`

Sinh văn bản theo hồ sơ — `workflowStage` LUÔN do FE truyền tường minh qua query param, KHÔNG tự
mặc định lấy `case.status_id` hiện tại (cho phép xem/sinh lại văn bản của bước đã qua, không chỉ
bước đang xử lý):
- `GET /api/v1/document-templates/cases/{caseId}?workflowStage=` — danh mục biểu mẫu ACTIVE khớp
  case_type_id/authority_level của hồ sơ + `workflowStage` truyền vào, lọc thêm theo điều kiện
  họp/không họp (`condition_key`, tra qua `WorkflowConditionResolver`) nếu template có khai báo —
  template bị ẩn do chưa xác định điều kiện thì response trả kèm `hasHiddenTemplates=true` +
  `warningMessage`, KHÔNG lỗi (`@RequirePermission(FN6, VIEW)`)
- `POST /api/v1/document-templates/generate?caseId=&templateId=&workflowStage=` — sinh 1 văn bản
  cụ thể theo `templateId` (không còn sinh hàng loạt theo workflowStage qua endpoint công khai —
  hàng loạt chỉ còn dùng nội bộ ở `POST /cases/{id}/archive`); `templateId` không khớp
  case_type_id/authority_level của hồ sơ hoặc không thuộc đúng `workflowStage` truyền vào, hoặc
  `generator_key` chưa có `DocumentContentProvider` đăng ký → lỗi rõ ràng, chặn ngay request này
  (`@RequirePermission(FN6, CREATE)`)
- `GET /api/v1/document-templates/download?caseId=&templateId=` — tải bản draft đã sinh (KHÔNG
  sinh lại), proxy S3 (`@RequirePermission(FN4, VIEW)`)

### Thành lập TCĐ cấp cơ sở (SC-07)
- `POST /cases/grassroots-establishments` — SUBSET của body `POST /cases/establishments` (bỏ
  `organizationTypeId` — server gán cứng loại hình "Chi bộ trực thuộc đảng bộ cơ sở", tra theo
  `code=CBTT_DUCS`, KHÔNG hardcode UUID; bỏ `leadershipInfo`), thêm `flowType` (`"B"`/`"C"` — CHƯA
  có quy tắc tự động chọn luồng, nhận trực tiếp từ client). `authorityLevel=GRASSROOTS_LEVEL`,
  `origin_flow`/`status_id` khởi tạo theo `flowType` (`B-01`/`C-01`) (`@RequirePermission(FN1, CREATE)`)
- `PUT /cases/{id}/establishment` — DÙNG LẠI NGUYÊN VẸN endpoint cấp Agribank ở trên cho cả hồ sơ
  grassroots (cùng entity `CaseEstablishment`); guard bước 1/bước kiểm soát tự nhận diện đúng
  `A-01`/`B-01`/`C-01` (bước 1) hoặc `A-02`/`B-02`/`C-02` (kiểm soát) theo `origin_flow` của từng
  hồ sơ — KHÔNG có endpoint update riêng cho grassroots
- `POST /cases/{id}/submit-to-parent` — BR-SC07-03, action `SUBMIT_TO_PARENT` (C-03 → C-04, R-PDCS
  gửi trình BTCĐU). Action ĐẶC BIỆT, KHÔNG qua `POST /cases/{id}/workflow-action` dùng chung — chặn
  nếu hồ sơ chưa có tài liệu đính kèm nào (`PMDV_ATTACHMENT` rỗng) (`@RequirePermission(FN2, APPROVE)`)

### Nghiệp vụ biến động (SC-08 — Giải thể/Sáp nhập/Hợp nhất/Chia tách/Đổi tên)
Bước 1 dùng CHUNG 1 entity `CaseChange`/1 cặp API cho cả 5 loại nghiệp vụ biến động, field hiển
thị/bắt buộc động theo `case_type_id` (VD Giải thể ẩn `proposedTargetName`, Sáp nhập cần thêm
`survivorOrganizationId`); ràng buộc số lượng `organizationIds` tra từ
`PMDV_CASE_TYPE.min/max_organization_count` — KHÔNG hardcode ngưỡng theo case_type. Hiện tại
CONTROLLER chỉ gọi service dùng chung với `authorityLevel=BANK_LEVEL`/`originFlow="A"` cố định
(nhánh `R-BPTM`/`GRASSROOTS_LEVEL` cấp cơ sở CHƯA triển khai, tương tự SC-07 establishment).
- `POST /cases/changes` — API-SC08-01, tạo mới Bước 1 (`@RequirePermission(FN1, CREATE)`)
- `PUT /cases/{id}/change` — API-SC08-02, cập nhật một phần (field null = không đổi); `caseTypeId`
  BẤT BIẾN, luôn lấy từ hồ sơ gốc, field này trong request (nếu có gửi) bị bỏ qua hoàn toàn. Guard
  ở service: hồ sơ đang ở bước 1 (`A-01`) HOẶC đang ở bước "Trình kiểm soát" kế tiếp (`A-02`) +
  `assignedUserId` — cùng cơ chế mở quyền Kiểm soát viên như `PUT /cases/{id}/establishment`
  (`@RequirePermission(FN1, EDIT)`)
