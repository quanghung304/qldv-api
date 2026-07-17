package com.agribank.qldv_api.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Đánh dấu TƯỜNG MINH route không cần kiểm tra RBAC (PMDV_ROLE_PERMISSION) — chỉ cần xác
 * thực token hợp lệ (VD GET /auth/me, /categories/*, /staff/search). Dùng annotation này thay
 * vì để route không có {@link RequirePermission} nào — tránh middleware tự suy đoán route nào
 * "cố tình bỏ qua" so với route "quên gắn annotation".
 *
 * Phạm vi middleware: PermissionInterceptor CHỈ can thiệp vào route có 1 trong 2 annotation
 * ({@link RequirePermission} hoặc annotation này). Route hiện có từ trước, chưa được rà soát
 * vào ma trận Role x Function x Action (VD đổi mật khẩu, xem thông tin bản thân...) sẽ KHÔNG bị
 * ảnh hưởng — giữ nguyên hành vi cũ, không tự gán 403 cho route ngoài phạm vi task này.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface NoPermissionCheck {
}
