package com.agribank.qldv_api.security;

import com.agribank.qldv_api.exception.ForbiddenException;
import com.agribank.qldv_api.gateway.RolePermissionClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.service.UserService;
import com.agribank.qldvutils.entity.RolePermission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * Middleware RBAC tầng route: đọc {@link RequirePermission} trên handler method, đối chiếu
 * PMDV_ROLE_PERMISSION (Role x Function x Action) theo TOÀN BỘ role_code của user hiện tại
 * (lấy từ UserDetailsImpl — đã resolve 1 lần lúc xác thực ở S1-05, không query lại
 * PMDV_USER_ROLE ở đây).
 *
 * Route có {@link NoPermissionCheck}: cho qua thẳng, không tra bảng quyền.
 * Route có {@link RequirePermission}: phải có ÍT NHẤT 1 role khớp (role_code, function, action)
 * trong PMDV_ROLE_PERMISSION mới cho qua; ngược lại 403 (ERR-GL-02).
 * Route KHÔNG có annotation nào cả: nằm NGOÀI phạm vi middleware này (route có từ trước Sprint
 * 1 RBAC, chưa được rà soát vào ma trận) — bỏ qua, giữ nguyên hành vi hiện có (không tự suy ra
 * là "được phép" hay "bị chặn").
 *
 * {@code rolePermissionClient}/{@code userService} được inject {@code @Lazy}: interceptor này
 * là dependency của {@code WebMvcConfig} (một {@code WebMvcConfigurer}), vốn được
 * Spring MVC resolve rất sớm trong quá trình khởi động. Nếu inject eager, việc tạo Feign
 * client (RolePermissionClient, và các Feign client bên trong UserService) sẽ kích hoạt tạo
 * child ApplicationContext của Feign ngay trong lúc parent context còn đang refresh, gây
 * BeanCurrentlyInCreationException (circular reference qua mvcResourceUrlProvider). Lazy proxy
 * hoãn việc tạo các bean này tới lần preHandle() đầu tiên, sau khi context đã refresh xong.
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {
    private static final String CONDITIONAL_ATTRIBUTE = "permissionConditional";

    private final RolePermissionClient rolePermissionClient;
    private final UserService userService;

    public PermissionInterceptor(@Lazy RolePermissionClient rolePermissionClient,
                                  @Lazy UserService userService) {
        this.rolePermissionClient = rolePermissionClient;
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        if (handlerMethod.hasMethodAnnotation(NoPermissionCheck.class)
                || handlerMethod.getBeanType().isAnnotationPresent(NoPermissionCheck.class)) {
            return true;
        }

        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (requirePermission == null) {
            // Route chưa được gắn annotation nào — ngoài phạm vi middleware này, không can thiệp.
            return true;
        }

        UserDetailsImpl userRequested = userService.getUserRequested();
        if (userRequested == null) {
            throw new ForbiddenException("ERR-GL-02: Không xác thực được người dùng");
        }

        List<String> roleCodes = userRequested.getRoleCodes();
        if (roleCodes == null || roleCodes.isEmpty()) {
            throw new ForbiddenException("ERR-GL-02: Bạn không có quyền thực hiện thao tác này");
        }

        List<RolePermission> matches = rolePermissionClient
                .check(roleCodes, requirePermission.function(), requirePermission.action())
                .getData();

        if (matches == null || matches.isEmpty()) {
            throw new ForbiddenException("ERR-GL-02: Bạn không có quyền thực hiện thao tác này");
        }

        boolean conditional = matches.stream().allMatch(m -> Boolean.TRUE.equals(m.getIsConditional()));
        request.setAttribute(CONDITIONAL_ATTRIBUTE, conditional);
        return true;
    }
}
