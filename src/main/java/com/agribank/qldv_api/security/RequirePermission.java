package com.agribank.qldv_api.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Gắn lên method controller để khai báo route này cần quyền (function_code, action) nào,
 * đối chiếu ma trận PMDV_ROLE_PERMISSION (Role x Function x Action). PermissionInterceptor
 * đọc annotation này ở tầng route, trước khi vào business logic.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequirePermission {
    String function();

    String action();
}
