# Coding Convention

## 1. Naming
- Class: `PascalCase`
- Method/field: `camelCase`
- Package: `lowercase`
- Endpoint: dùng danh từ nghiệp vụ rõ nghĩa, ví dụ `/api/v1/metadata-terms`

## 2. Spring conventions
- Dùng `@RequiredArgsConstructor` cho dependency injection.
- Controller trả về `ResponseEntity`.
- Service ưu tiên `@Service`.
- Repository ưu tiên `extends JpaRepository`.
- Dùng `@RestController`, `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`.

## 3. Lombok conventions
- Dùng `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` khi phù hợp.
- Dùng `@FieldDefaults(level = AccessLevel.PRIVATE)` để thống nhất access modifier.
- Không lạm dụng Lombok ở lớp cần custom logic rõ ràng.

## 4. Response conventions
- Thành công trả qua `DefaultResponse.success(...)` hoặc `DefaultListResponse.success(...)`.
- Lỗi nên trả message ngắn gọn, đúng nghiệp vụ.

## 5. Validation conventions
- Request object phải có validation rõ ràng bằng `jakarta.validation`.
- Dữ liệu mặc định cho request dùng `@Builder.Default` nếu cần.

## 6. Entity conventions
- Entity map bảng bằng `@Entity`, `@Table`, `@Column`.
- Nếu entity có key tổng hợp, dùng `@IdClass`.
- Audit timestamp đi qua `BaseEntity` nếu phù hợp.
- Không thể hiện relationship trực tiếp trong Entity

## 7. Comment conventions
- Chỉ thêm comment khi cần giải thích ý đồ kỹ thuật hoặc rule nghiệp vụ phức tạp.
- Tránh comment dài khi tên biến/lớp đã đủ rõ.

## 8. Tối ưu truy vấn dữ liệu
- Ưu tiên gom lọc/phân trang vào 1 câu query DB (JPQL/native) thay vì fetch hết rồi lọc bằng code
  Java — nhất là với danh sách có thể lớn dần theo thời gian.
- Với dữ liệu cần cho nhiều bản ghi cùng lúc (VD tên chi nhánh theo brcd, staff theo staff_code),
  gom thành 1 lệnh gọi hàng loạt (`IN (...)`) thay vì gọi lặp lại theo từng bản ghi (N+1).
- Thông tin đã xác định được ngay lúc xác thực (role, phạm vi tổ chức của user...) thì resolve
  1 lần ở tầng middleware/filter và lưu vào `SecurityContext`, không query lại ở từng API.
- Chấp nhận nhiều hơn 1 query trong 1 API nếu không thể gộp an toàn (VD cần biết tồn tại/phạm vi
  trước khi quyết định 403 hay 404) — chỉ tối ưu khi không đánh đổi tính đúng đắn.

## 9. Ghi nhiều bảng liên quan trong 1 request — PHẢI atomic (transaction)

`qldv-api` và `qldv-db` là 2 service riêng, gọi nhau qua Feign (HTTP) — mỗi lệnh Feign là 1
request độc lập, tự commit riêng ở phía `qldv-db`. Nếu 1 API cần ghi > 1 bảng/entity liên quan
(VD tạo hồ sơ + bảng mở rộng + danh sách con + gán khóa ngoại cho bảng khác), **KHÔNG** gọi nhiều
lệnh `/save` rời rạc từ `qldv-api` — lệnh sau lỗi thì lệnh trước đã lưu vẫn còn, để lại dữ liệu
nửa vời. Thay vào đó:

1. Định nghĩa 1 request DTO "gói" (VD `EstablishmentCasePersistRequest`, `qldv-utils/request`)
   chứa toàn bộ entity/id cần ghi cho 1 nghiệp vụ.
2. Thêm đúng 1 endpoint + 1 service `@Transactional` bên `qldv-db` (VD
   `CaseEstablishmentPersistService`) thực hiện tuần tự toàn bộ bước ghi trong CÙNG 1 transaction
   — lỗi ở bước nào cũng rollback toàn bộ. Nếu 1 entity cần biết `id` vừa sinh của entity khác
   (VD `BaseEntity` dùng `GenerationType.UUID`, chỉ có `id` sau khi `save()`), gán ngay trong
   service này (`child.setParentId(savedParent.getId())`) trước khi lưu tiếp — không cần
   `qldv-api` tự đoán/sinh id trước.
3. Service `@Transactional` này CHỈ persist + gán khóa ngoại — KHÔNG validate/business rule (đã
   chạy xong ở `qldv-api` trước khi gọi xuống), giống đúng pattern `WorkflowTransitionService`.
4. `qldv-api` chỉ làm 2 việc: (a) đọc dữ liệu để validate toàn bộ TRƯỚC, chưa ghi gì cả; (b) sau
   khi validate xong hết, build 1 request "gói" và gọi đúng 1 lệnh persist.

Tham khảo nguyên bản: `EstablishmentCaseService` (qldv-api) → `EstablishmentCasePersistRequest`
(qldv-utils) → `CaseEstablishmentPersistService` + `CaseEstablishmentController#persist`
(qldv-db) — dùng cho API-SC02-01/02.

## 10. Validate nhiều lỗi cùng lúc, phân biệt Chặn vs Cảnh báo

- Khi 1 request có thể sai ở nhiều field cùng lúc, KHÔNG throw lỗi ngay khi gặp field đầu tiên —
  gom hết vào 1 `Map<String, String>` (field → "MÃ_LỖI: message"), validate xong toàn bộ rồi mới
  quyết định chặn hay không (xem `EstablishmentCaseService` — các hàm `require*`/`validate*`
  nhận `Map<String, String> errors` để ghi lỗi vào, không throw giữa chừng).
- Lỗi mức "Chặn": nếu map lỗi không rỗng sau khi validate xong, throw
  `FieldValidationException(errors)` (qldv-api/exception) — `ExceptionHandle` trả về cùng khuôn
  dạng với lỗi `MethodArgumentNotValidException` (`data` là map field→message), để FE xử lý
  thống nhất 1 chỗ.
- Lỗi mức "Cảnh báo" (không chặn lưu, chỉ mang tính thông báo — VD BR có ghi rõ "Cảnh báo"):
  KHÔNG cho vào map lỗi ở trên — gom vào 1 `Map<String, String> warnings` riêng và trả kèm trong
  response thành công (field `warnings` của response, xem `EstablishmentCaseResponse`).

## 11. Hàm dùng chung cho nhiều biến thể nghiệp vụ — không hardcode theo biến thể

Khi 2 API khác nhau (VD cấp Agribank vs cấp cơ sở, sinh văn bản theo loại nghiệp vụ khác nhau...)
dùng chung phần lớn logic, chỉ khác nhau ở vài tham số cố định:
- Viết 1 service dùng chung, nhận đúng phần khác biệt đó làm THAM SỐ tường minh (VD
  `authorityLevel`, `originFlow`, `allowedOrganizationTypeId` của
  `EstablishmentCaseService.createEstablishmentCase(...)`) — KHÔNG viết `if (authorityLevel ==
  X)` hay hardcode giá trị biến thể (VD ngưỡng số lượng, mã cố định) ngay trong service.
- Giá trị biến thể do CONTROLLER của từng API cụ thể truyền vào (controller "biết" nó đang phục
  vụ biến thể nào), service dùng chung không tự suy luận biến thể từ tham số khác.
- Ngưỡng/danh mục có thể thay đổi theo cấu hình (VD số lượng tối thiểu theo loại tổ chức) phải
  lookup từ bảng danh mục tương ứng, không so sánh với hằng số cứng trong code.