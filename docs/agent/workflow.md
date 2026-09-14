# Codex Workflow

## 1. Trước khi sửa code
- Xác định thay đổi thuộc `api`, `db` hay `utils`.
- Tìm file cùng pattern để bám theo cấu trúc hiện tại.
- Kiểm tra contract có bị ảnh hưởng không.

## 2. Khi tạo file mới
- Tạo đúng module trước, không đưa nhầm shared model vào service layer.
- Đặt tên file khớp với role của nó.
- Nếu file dùng chung cho cả `api` và `db`, cân nhắc đặt vào `utils`.

## 3. Khi sửa file hiện có
- Ưu tiên giữ nguyên API hiện tại nếu không có yêu cầu đổi.
- Tránh đổi package hoặc endpoint nếu không thật sự cần.

## 4. Khi thêm tính năng
- API mới:
  - xác định request/response
  - thêm service
  - thêm controller
  - nếu cần, thêm feign client
- DB mới:
  - thêm entity
  - thêm repository
  - thêm service
  - thêm controller nội bộ nếu cần

## 5. Khi thêm entity mới
- Tạo Entity trong `utils`
- Tạo Repository và Controller (extend BaseController) tương ứng với Entity trong `db`
- Tạo Client extend BaseClient tương ứng với Entity trong `api`

## 6. Checklist trước khi bàn giao
- Code đặt đúng module.
- Naming khớp convention hiện có.
- Response dùng chuẩn chung.
- Không kéo logic business vào utility module.
- Tài liệu liên quan đã được cập nhật.


