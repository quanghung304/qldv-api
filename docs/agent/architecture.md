# Backend Architecture

## 1. Module & vai trò
Backend chia thành 3 module, tương ứng với 3 git repository:
- `api`: lớp API public, security, orchestration, response cho frontend. Nhận request từ UI,
  xử lý service/logic nghiệp vụ, quản lý tìm kiếm, import/export, audit trail, approval flow.
  Gọi service `db` hoặc dịch vụ liên quan qua Feign/HTTP nếu cần.
- `db`: lớp dữ liệu — quản lý kết nối DB, JPA entity, repository, query custom, dữ liệu tham
  chiếu. Cung cấp endpoint nội bộ cho `api` thực hiện thao tác với database.
- `utils`: contract dùng chung giữa `api` và `db` (request/response, paging, view model).
  Không chứa business logic.

Luồng phụ thuộc: `api` phụ thuộc `utils`, `db` phụ thuộc `utils`, `api` không gọi repository
trực tiếp.

## 2. Phân lớp đề xuất

### API layer
Thư mục gợi ý trong `api`:
- `controller`
- `service`
- `feign`
- `config`
- `request`
- `response`
- `dto`
- `exception`
- `security`
- `utils`

### Data layer
Thư mục gợi ý trong `db`:
- `controller`
- `service`
- `repository`
- `entity`
- `config`
- `exception`
- `mapper`
- `specification`

### Shared contract layer
Thư mục gợi ý trong `utils`:
- `model`
- `request`
- `response`
- `pagination`
- `validation`
- `enums`

## 3. Quy tắc tách trách nhiệm
- `controller`: chỉ nhận request, gọi service, trả response.
- `service`: xử lý nghiệp vụ và điều phối giữa các nguồn dữ liệu.
- `repository`: chỉ chứa truy vấn DB.
- `entity`: chỉ map schema, không chứa logic nghiệp vụ.
- `request/response`: chỉ mô tả contract.
- `config`: chỉ cấu hình kỹ thuật.

## 4. Chuẩn contract hiện có
- `DefaultResponse<T>` cho dữ liệu đơn.
- `DefaultListResponse<T>` cho danh sách.
- `PagingRequest` cho phân trang.
- `BaseEntity` cho audit timestamp.
