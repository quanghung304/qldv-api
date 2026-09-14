# Data Model — tham chiếu nhanh cho agent

Nguồn đầy đủ: `docs/full/04_DataModel_ERD.docx`. File này chỉ liệt kê bảng/field/khóa để code
migration và entity — không giải thích lại.

> **Đã bỏ `PMDV_DOCUMENT_RULE`, `PMDV_DOCUMENT_TYPE` không còn gắn với cơ chế sinh văn bản**
> (quyết định nghiệp vụ: "chọn loại văn bản từ danh mục" không có giá trị — actor chỉ cần biết
> "file này dùng cho hồ sơ loại gì, bước nào"). Toàn bộ vai trò 2 bảng đó gộp thẳng vào
> `PMDV_DOCUMENT_TEMPLATE` (đã thiết kế lại — xem bên dưới). Entity `DocumentType.java` VẪN giữ
> trong repo (không xóa file) vì `GET /categories/document-types` và `PMDV_DOCUMENT` cũ có thể còn
> tham chiếu gián tiếp, nhưng KHÔNG còn vai trò gì trong luồng sinh văn bản.

Quy ước: PK = khóa chính, FK = khóa ngoại. Tên bảng viết hoa `PMDV_*`, tên field snake_case
thường. Trừ khi ghi chú khác, mọi bảng dùng `BaseEntity<String>` (`qldv-utils`): PK là cột
`id` (String, UUID tự sinh), có thêm `created_at`/`updated_at`.

## Nhóm danh mục

| Bảng | Field chính | Ghi chú |
|---|---|---|
| `PMDV_ORGANIZATION_TYPE` | id (PK), code, name, min_member_count | 4 loại hình tổ chức đảng |
| `PMDV_CASE_TYPE` | id (PK), code, name, min_organization_count, max_organization_count | 9 loại nghiệp vụ hồ sơ, VD `ESTABLISH` |
| `PMDV_STATUS` | id (PK), status_code, status_name, flow_code, status_type (INT — enum `EStatusType`), is_locked, step_no (INT, nullable), step_code (nullable), step_name (nullable) | Đồng bộ với `workflow-states.md`. `step_no/step_code/step_name` — deep-link FE về đúng bước UI khi sửa hồ sơ (xem mapping A/B/C trong migration `update_pmdv_status_step_mapping.sql`); NULL với trạng thái không map bước nào (VD `A-17`, `C-04`) |
| `PMDV_DOCUMENT_TYPE` | id (PK), code, name, has_validity_period, module_code (nullable), is_active | Không còn dùng trong cơ chế sinh văn bản — chỉ còn phục vụ `GET /categories/document-types` (nếu còn cần) |
| `PMDV_ROLE` | id (PK), role_code, role_name, applicable_unit_type (INT — enum `EUnitType`), max_count | 8 vai trò tham gia luồng + `R-ADM`/`R-QTVCS` không tham gia — đồng bộ `permission-rules.md` |
| `PMDV_DOCUMENT_TEMPLATE` | id (PK), case_type_id (FK → `PMDV_CASE_TYPE`, NOT NULL), authority_level (INT, NOT NULL), workflow_stage (String, NOT NULL — lưu status_code dạng chuỗi VD `A-01`, KHÔNG FK vật lý tới `PMDV_STATUS`), template_code (String, NOT NULL — định danh TỰ DO do người upload đặt, VD `TO_TRINH_BTV`, KHÔNG tra danh mục), condition_key (String, nullable — rẽ nhánh khi 1 template_code có nhiều bản cùng bước, VD `MEETING`/`BALLOT`; NULL = áp dụng mọi trường hợp), template_name (String, NOT NULL — tên hiển thị tự do), storage_path (String, NOT NULL — S3 key), status (String, NOT NULL — `DRAFT`/`PENDING_REVIEW`/`ACTIVE`/`INACTIVE`, thực tế upload nay LUÔN set `ACTIVE` ngay), generator_key (String, NOT NULL — định danh hàm Java sinh nội dung, gõ tay lúc upload, PHẢI khớp tên bean `DocumentContentProvider` ở qldv-db thì mới sinh được văn bản; không khớp chỉ báo lỗi lúc gọi API sinh draft, không chặn upload) | File mẫu .docx cho 1 "slot" cụ thể của quy trình. `@UniqueConstraint` DB trên (case_type_id, authority_level, workflow_stage, template_code, condition_key) chỉ khai báo mức thô — ràng buộc nghiệp vụ thật ("chỉ 1 bản ACTIVE/tổ hợp tại 1 thời điểm") do tầng Service tự deactivate các bản ACTIVE khác cùng tổ hợp khi kích hoạt bản mới (`DocumentTemplatePersistService`, qldv-db). KHÔNG còn cột `field_mapping_config` (JSON schema v2, đã bỏ cùng Resolver Registry — xem mục "Cơ chế sinh văn bản tự động" bên dưới); cũng không có cột `version`/`effective_date` |
| `PMDV_ROLE_PERMISSION` | id (PK), role_code, function_code, action, is_conditional | Ma trận phân quyền (role_code, function_code, action) unique. Đồng bộ `permission-rules.md` |

### Field enum lưu INT

Các field dưới đây lưu `INTEGER` (thứ tự theo enum Java trong `qldv-utils/enums`), mã chữ gốc
là tên hằng số:

| Field (bảng) | 1 | 2 | 3 |
|---|---|---|---|
| `operation_status` (`PMDV_ORGANIZATION`) | ACTIVE | DISSOLVED | DISBANDED |
| `data_source` (`PMDV_STAFF`) | HR_SYSTEM | MANUAL | |
| `position` (`PMDV_COMMITTEE_MEMBER`, `PMDV_CASE_ESTB_COMMITTEE.proposed_position`) | SECRETARY | DEPUTY_SECRETARY | MEMBER |
| `status` (`PMDV_COMMITTEE_MEMBER`) | PROPOSED | OFFICIAL | DISMISSED |
| `authority_level` (`PMDV_CASE`, `PMDV_DOCUMENT_TEMPLATE`) | BANK_LEVEL | GRASSROOTS_LEVEL | |
| `link_role` (`PMDV_CASE_ORGANIZATION`) | SOURCE | TARGET | |
| `origin` (`PMDV_DOCUMENT`) | REFERENCE | GENERATED | |
| `status_type` (`PMDV_STATUS`) | INTERNAL | EXTERNAL_WAIT | FINAL |
| `method` (`PMDV_CASE_BOARD_REVIEW`), `btv_method` (`PMDV_CASE`) | MEETING | BALLOT | |
| `applicable_unit_type` (`PMDV_ROLE`) | enum `EUnitType`, 7 giá trị: TRUNG_TAM_QUAN_LY_DU_LIEU, BAN_TO_CHUC_DANG_UY, DANG_BO_CO_SO, CHI_BO_CO_SO, CHI_BO_TRUC_THUOC_TRU_SO_CHINH, CHI_BO_TRUC_THUOC_DANG_BO_CO_SO, DON_VI_TRUC_THUOC | | |

`origin_flow` (`PMDV_CASE`) là `String`, giá trị `A`/`B`/`C` (3 luồng thẩm quyền — xem
`workflow-states.md`). `status_id` (`PMDV_CASE`, FK lưu `status_code`) nhận 1 trong 26 mã:
`A-01`..`A-17` (Luồng A), `B-01`..`B-05` (Luồng B), `C-01`..`C-04` (Luồng C) — enum
`ECaseStatusCode` (`qldv-api/enums`). `action` (`PMDV_CASE_HISTORY`) là `String`, hiện có 9 giá
trị — enum `ECaseWorkflowAction` (`qldv-utils/enums`): 4 action dùng chung qua
`POST /cases/{id}/workflow-action` (`SUBMIT_CONTROL`/`RETURN`/`APPROVE_FORWARD`/`APPROVE`) + 5
action đặc biệt do endpoint nghiệp vụ riêng gọi thẳng Workflow Engine
(`REGISTER_SIGNED_DOC`/`APPROVE_COMPLETE`/`APPROVE_ISSUE`/`SUBMIT_TO_PARENT`/`RECEIVE_ROUTE`).
Không ép enum INT cho 2 field này (đồng bộ CSDL/API/tài liệu nghiệp vụ, xem `AGENTS.md`).
`status` (`PMDV_DOCUMENT_TEMPLATE`) và `workflow_stage`/`template_code`/`condition_key` là
`String` tự do, KHÔNG ép enum (xem bảng danh mục ở trên).

## Nhóm nghiệp vụ lõi

| Bảng | Field chính | Ghi chú |
|---|---|---|
| `PMDV_ORGANIZATION` | id (PK), organization_code, organization_name, organization_type_id (FK), brcd (mã chi nhánh), parent_organization_id (FK self), is_authorized, member_count, committee_member_count, operation_status (INT), establish_decision_no, establish_decision_date, dissolve_decision_no, dissolve_decision_date | Chỉ được ghi CHÍNH THỨC khi hồ sơ Thành lập đạt `POST /cases/{id}/complete` (`CaseCompleteService`) — không sớm hơn |
| `PMDV_STAFF` | id (PK), staff_code, full_name, gender, date_of_birth, ethnicity, religion, hometown, official_party_admission_date, qualification, brcd (mã chi nhánh công tác), organization_id (FK → `PMDV_ORGANIZATION`, chi/đảng bộ sinh hoạt), data_source (INT), synced_at | |
| `PMDV_COMMITTEE_MEMBER` | organization_id (PK, FK), staff_code (PK, FK → `PMDV_STAFF.staff_code`), committee_member_id, position (INT), status (INT), appointment_decision_no, effective_date, expiry_date | PK = tổ hợp (organization_id, staff_code) qua `@IdClass`, không dùng `BaseEntity` |
| `PMDV_CASE` | id (PK), case_code (sinh tuần tự dạng `{case_type_code}-{yyyyMMdd}-{seq 4 chữ số}`, xem `EstablishmentCaseService.generateCaseCode`), case_type_id (FK), authority_level (INT), origin_flow (String, `A`/`B`/`C`), status_id (FK, lưu status_code), created_by (FK → `QLDV_USER`), completed_at (set bởi `CaseCompleteService` khi case đạt trạng thái Hoàn thành — trước đó luôn NULL), affected_member_count, affected_committee_count, proposed_organization_name, btv_method (INT, nullable) | Không còn cột `brcd` (chuyển sang `PMDV_CASE_ESTABLISHMENT.brcd`). `btv_method`: hình thức xử lý Ban Thường vụ **DỰ KIẾN**, chuyên viên xác định NGAY LÚC TẠO HỒ SƠ (`POST /cases/establishments`) — ĐỘC LẬP với `PMDV_CASE_BOARD_REVIEW.method` (quyết định thật của Bước 2), dùng làm `condition_key` khi chọn `PMDV_DOCUMENT_TEMPLATE` lúc sinh văn bản |
| `PMDV_CASE_ORGANIZATION` | case_id (PK, FK), organization_id (PK, FK), case_organization_id, link_role (INT) | PK = tổ hợp (case_id, organization_id) qua `@IdClass` |
| `PMDV_CASE_HISTORY` | id (PK), entity_table (String, VD `PMDV_CASE`), entity_id (String, id của bản ghi trong entity_table), from_status_id (FK, lưu status_code), action (String), to_status_id (FK, lưu status_code), performed_by (FK → `QLDV_USER`), performed_role_id (FK, lưu role_code), note, processed_at | Polymorphic qua (entity_table, entity_id) — dùng chung cho `PMDV_CASE` (module Tổ chức Đảng) và module Đảng viên sau này. Cột `note` (không phải `comment` — tránh từ khoá dành riêng của Oracle) |
| `PMDV_CASE_ESTABLISHMENT` | id (PK), case_id (FK → `PMDV_CASE`, unique — quan hệ 1-1, KHÔNG phải PK của bảng này), brcd, staff_count, leadership_staff_id, leadership_info_text, board_decision_no, board_decision_date, board_decision_summary, organization_type_id (FK → `PMDV_ORGANIZATION_TYPE`), member_count, committee_member_count, committee_structure, political_standard_conclusion_no, political_standard_conclusion_date | Bảng mở rộng lưu 15 field nghiệp vụ Bước 1 SC-02 (Thành lập TCĐ) — tra cứu theo hồ sơ phải dùng `findByCaseId(...)`, KHÔNG dùng `findById` (đó là PK UUID riêng của bảng này) |
| `PMDV_CASE_ESTB_COMMITTEE` | case_id (PK, FK → `PMDV_CASE`), staff_code (PK, FK → `PMDV_STAFF.staff_code`), proposed_position (INT) | PK = tổ hợp (case_id, staff_code) qua `@IdClass`, không dùng `BaseEntity`. Danh sách cấp ủy DỰ KIẾN (field 12 SC-02) — "chính thức hóa" thành `PMDV_COMMITTEE_MEMBER` (status=OFFICIAL) lúc `POST /cases/{id}/complete` |
| `PMDV_CASE_BOARD_REVIEW` | id (PK), case_id (FK → `PMDV_CASE`, unique, not null), method (INT), board_document_no, board_document_date, ballots_issued, ballots_returned, ballots_agree, ballots_disagree, opinion_notes | SC-04 — ghi nhận ý kiến Ban Thường vụ (họp trực tiếp hoặc lấy phiếu), quan hệ 1-1 với `PMDV_CASE`. `method` ĐỘC LẬP với `PMDV_CASE.btv_method` (xem trên) |
| `PMDV_DOCUMENT` | id (PK), case_id (FK), template_id (FK → `PMDV_DOCUMENT_TEMPLATE.id`, NULLABLE — NULL khi văn bản là REFERENCE nhập tay không qua template), template_code (String, denormalize từ `PMDV_DOCUMENT_TEMPLATE.template_code` lúc sinh, dùng để dựng lại S3 key lúc tải draft), document_name (String, nullable — BẮT BUỘC khi template_id NULL, tên văn bản tự do do chuyên viên tự gõ, KHÔNG phụ thuộc danh mục document_type nào nữa), document_no, document_date, effective_date, summary, origin (INT), created_by (FK → `QLDV_USER`) | Không còn cột `document_type_id`. Upsert theo (case_id, template_id) khi sinh tự động (lần 3 — LƯU Ý: template_id đổi mỗi lần admin kích hoạt version mới của cùng 1 template, nên sinh lại sau khi có version mới sẽ tạo THÊM 1 dòng thay vì cập nhật dòng cũ, đánh đổi có chủ đích của thiết kế mới), theo (case_id, document_name) khi nhập tay (`DecisionDocumentsService`) |
| `PMDV_ATTACHMENT` | id (PK), case_id (FK, nullable), document_id (FK, nullable), file_name, file_path, workflow_stage (String, NOT NULL — status_code của case TẠI THỜI ĐIỂM UPLOAD, server tự gán từ `case.status_id`, KHÔNG nhận từ client; là dấu vết LỊCH SỬ, giữ nguyên dù hồ sơ đã sang bước khác), uploaded_by (FK → `QLDV_USER`), uploaded_at, is_locked | Không còn cột `file_format`/`file_size_kb`/`attachment_type`. Cơ chế CỘNG DỒN: mỗi lần `POST /api/v1/attachments/{caseId}` upload chỉ ghi thêm dòng mới, KHÔNG đụng tới attachment đã có (kể cả cùng case_id/workflow_stage) — xóa từng file cụ thể do người dùng tự gọi `DELETE /api/v1/attachments/{id}` |

## Nhóm người dùng & bảo mật

| Bảng | Field chính | Ghi chú |
|---|---|---|
| `QLDV_USER` | id (PK), staff_code, id_iam, username, full_name, email, brcd, dep_id, phone, vneid, active, deleted | Entity `User.java`, không phải `PMDV_USER` |
| `PMDV_USER_ROLE` | id (PK, String), user_id (FK → `QLDV_USER`), role_id (FK → `PMDV_ROLE`) | Không dùng `BaseEntity` (không có created_at/updated_at). N-N giữa user và role |
| `PMDV_USER_SCOPE` | user_id (PK, FK), organization_id (PK, FK), user_scope_id | PK = tổ hợp (user_id, organization_id) qua `@IdClass`, không dùng `BaseEntity` |
| `PMDV_AUDIT_LOG` | id (PK), entity_name, entity_id, action, change_detail, performed_by (FK → `QLDV_USER`), performed_at, ip_address | Log toàn hệ thống, polymorphic qua (entity_name, entity_id) |

## Enum vai trò/trạng thái/action dùng cho code (không phải cột CSDL riêng)

| Enum | Vị trí | Giá trị |
|---|---|---|
| `ERoleCode` | `qldv-utils/enums` | `R_ADM`("R-ADM"), `R_CV`("R-CV"), `R_KS`("R-KS"), `R_LD`("R-LD"), `R_QTVCS`("R-QTVCS"), `R_BPTM`("R-BPTM"), `R_KSCS`("R-KSCS"), `R_PDCS`("R-PDCS") — 8 role_code dùng cho guard Workflow Engine/scope |
| `ERole` | `qldv-api/enums` | `R_ADM`("R-ADM"), `R_QTVCS`("R-QTVCS") — enum RIÊNG, chỉ 2 role quản trị, khác `ERoleCode` (không dùng cho Workflow Engine) |
| `ECaseStatusCode` | `qldv-api/enums` | 26 hằng số `A_01`..`A_17`, `B_01`..`B_05`, `C_01`..`C_04`, mỗi hằng số có `code` (VD "A-01") + `description` tiếng Việt |
| `ECaseWorkflowAction` | `qldv-utils/enums` | `SUBMIT_CONTROL`, `RETURN`, `APPROVE_FORWARD`, `APPROVE`, `REGISTER_SIGNED_DOC`, `APPROVE_COMPLETE`, `APPROVE_ISSUE`, `SUBMIT_TO_PARENT`, `RECEIVE_ROUTE` |
| `EBoardReviewMethod` | `qldv-utils/enums` | `MEETING`(1), `BALLOT`(2) — dùng chung cho cả `PMDV_CASE_BOARD_REVIEW.method` và `PMDV_CASE.btv_method` |

## Cơ chế sinh văn bản tự động (lần 3 — DocumentContentProvider) — đọc động từ `PMDV_DOCUMENT_TEMPLATE`

> **Đã thay thế hoàn toàn** cơ chế Resolver Registry/`field_mapping_config` (schema v2, FieldCatalog,
> FieldPathRegistry, FieldMappingService/Client) của 2 lần thiết kế trước — cơ chế tự động dò
> placeholder + tự suy luận resolver không khả thi khi áp dụng vào mẫu biểu thật. Mỗi file mẫu nay
> gắn với ĐÚNG 1 hàm Java viết tay (`generator_key`).

- **KHÔNG** hardcode danh sách văn bản cần sinh theo case_type/bước ở bất kỳ đâu trong code —
  `GET /api/v1/document-templates/cases/{caseId}?workflowStage=` (Phần 4) luôn query
  `PMDV_DOCUMENT_TEMPLATE WHERE case_type_id/authority_level/workflow_stage khớp AND status=ACTIVE`
  — `workflowStage` do FE truyền tường minh, KHÔNG tự suy ra từ `case.status_id` hiện tại. Nhiều bản cùng
  `template_code` (khác `condition_key`) → lọc theo `WorkflowConditionResolver.resolveConditionValue`
  (`qldv-api/workflow`, hardcode trong code — KHÔNG lưu DB): tuỳ workflowStage, đọc
  `PMDV_CASE.btv_method` (bước 1/2) hoặc `PMDV_CASE_BOARD_REVIEW.method` (bước 3), map
  1=MEETING/2=BALLOT; chưa xác định được → ẨN template đó khỏi danh mục, KHÔNG lỗi (response trả
  kèm `hasHiddenTemplates`/`warningMessage`).
- **DocumentContentProvider** (đọc giá trị placeholder `[ten_field]` trong file mẫu): sống ở qldv-db
  (`com.agribank.qldvdb.docgen`, interface `DocumentContentProvider` — mỗi implementation là 1
  `@Component` riêng, tên bean = `generator_key`, Spring tự gom vào `Map<String,
  DocumentContentProvider>` qua `DocumentContentResolutionService`, cần Spring-managed JPA
  repository trực tiếp). `POST /api/v1/document-templates/generate?caseId=&templateId=&workflowStage=` (Phần 5,
  qldv-api) gọi xuống qua `DocumentContentClient` (`GET /api/v1/document-content/resolve`,
  nội bộ — không phải API công khai) để lấy `Map<String,String>` giá trị ĐÃ FORMAT SẴN (ngày
  dd/MM/yyyy...) — tầng merge chỉ string-replace thuần túy. `generator_key` không khớp bean nào →
  lỗi rõ ràng NGAY LÚC GỌI API sinh draft (không lỗi lúc upload template).
- Field provider không lấy được dữ liệu → value = chuỗi rỗng `""` trong Map (KHÔNG throw, KHÔNG
  `null`); placeholder hoàn toàn không có trong Map (provider không biết field đó) → GIỮ NGUYÊN
  `[ten_field]` trong văn bản — văn bản sinh ra luôn là bản DRAFT.
- `POST /cases/{id}/archive` (API-SC06-01, `ArchiveCaseService`) tái sử dụng
  `DocumentContentGenerationService.generateAllForStage` (sinh TOÀN BỘ template ACTIVE của 1
  workflowStage, mỗi template xử lý độc lập/an toàn) — KHÔNG phải endpoint công khai riêng.
- S3 key: `{project_prefix}/case-documents/{yyyy}/{MM}/{case_id}/{template_code}-draft.docx`
  (`{yyyy}/{MM}` lấy từ `case.created_at`) — ghi đè mỗi lần sinh lại, dùng chung
  `S3Service`/`StorageKeyBuilder` (`qldv-api/storage`) với API đính kèm tài liệu.

## Chiến lược xóa dữ liệu

- `DELETE /cases/{id}` (API-GL-01) hard-delete `PMDV_CASE` — CHỈ khi hồ sơ còn "Đang thực hiện"
  lần đầu (chưa từng qua `SUBMIT_CONTROL`, `PMDV_CASE_HISTORY` rỗng). Cascade xóa
  `PMDV_ATTACHMENT` gắn thẳng `case_id` lẫn gắn qua `document_id` của các document thuộc case đó
  (`AttachmentRepository.deleteByCaseId`/`deleteByDocumentIdIn`).
- Chưa có cột đánh dấu soft-delete (VD `is_deleted`/`deleted_at`) cho các nghiệp vụ khác — xóa
  hồ sơ sau khi đã Trình kiểm soát KHÔNG được hỗ trợ (theo đúng guard trên).
