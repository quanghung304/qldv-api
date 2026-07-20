package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.gateway.IAMClient;
import com.agribank.qldv_api.gateway.StaffClient;
import com.agribank.qldv_api.gateway.UserClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.IAMRegisterRequest;
import com.agribank.qldv_api.request.RegisterRequest;
import com.agribank.qldv_api.request.role.UserRoleRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.user.ADResponse;
import com.agribank.qldv_api.response.user.UserIamResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import com.agribank.qldv_api.service.log.AuthenticationLogService;
import com.agribank.qldv_api.service.role.UserRoleService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.Staff;
import com.agribank.qldvutils.entity.User;
import com.agribank.qldvutils.enums.EAccountStatus;
import com.agribank.qldvutils.exception.CommonException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRoleService userRoleService;
    @Value("${qldv.app.id}")
    private Integer QLDV_APP_ID;


    private final IAMClient iamClient;
    private final UserClient userClient;
    private final StaffClient staffClient;
    private final ModelMapper modelMapper;
    private final AuthenticationLogService authenticationLogService;

    public UserResponse register(RegisterRequest request) {
        IAMRegisterRequest registerRequest = IAMRegisterRequest.builder()
                .username(request.getUsername())
                .brcd(request.getBrcd())
                .email(request.getUsername() + Constants.EMAIL_DOMAIN)
                .fullName(request.getFullName())
                .applicationIds(List.of(QLDV_APP_ID))
                .depId(request.getDepId())
                .staffCode(request.getStaffCode())
                .build();

        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authorHeader = "Bearer " + CommonUtils.getAccessToken(servletRequest);

        if (!EAuthType.SSO_EMAIL.name().equals(request.getAuthType())) {
            validateInternal(request, registerRequest, authorHeader);
        }
        UserIamResponse userIamResponse = null;
        try {
            DefaultResponse<ADResponse> adResponse = iamClient.checkAd(registerRequest.getUsername(), 0);

            if (Objects.isNull(adResponse) || Objects.isNull(adResponse.getData())) {
                throw new CommonException("Email không đúng định dạng Agribank vui lòng kiểm tra lại");
            }
            User user = new User();
            user.setUsername(registerRequest.getUsername());

            UserDetailsImpl userRequested = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            user.setFullName(request.getFullName());
            user.setBrcd(request.getBrcd());
            user.setDepId(request.getDepId());
            user.setDeleted(0);
            user.setAccountStatus(EAccountStatus.ACTIVE.getId());
            user.setCreatedBy(userRequested.getId());
            user.setEmail(request.getUsername());
            user.setStaffCode(Objects.nonNull(request.getStaffCode())
            ? request.getStaffCode() + ""
                    : null
                    );


            user = userClient.save(user).getData();
            createPMStaff(user, request);

            assignRole(user.getId(), request.getRoleIds(), user);
            //ghi log
            DefaultResponse<UserIamResponse> response = iamClient.register(registerRequest);

            userIamResponse = response.getData();

            user.setIdIam(userIamResponse.getId());
            user = userClient.save(user).getData();

            writeLog(registerRequest);
            return modelMapper.map(user, UserResponse.class);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    private void createPMStaff(User user, RegisterRequest request){
        if (Objects.isNull(user.getStaffCode()) || user.getStaffCode().isBlank()) {
            return;
        }

        Staff existedStaff = staffClient.findByStaffCode(user.getStaffCode()).getData();
        if (Objects.nonNull(existedStaff)) {
            return;
        }

        Staff staff = Staff.builder()
                .staffCode(user.getStaffCode())
                .fullName(user.getFullName())
                .organizationId(request.getOrganizationId())
                .brcd(request.getBrcd())
                .build();
        staffClient.save(staff);
    }

    private void writeLog(IAMRegisterRequest registerRequest) {
        try {
            List<IAMRegisterRequest> iamRegisterRequests = new ArrayList<>();
            iamRegisterRequests.add(registerRequest);
            authenticationLogService.writeLogRegister(iamRegisterRequests);
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void assignRole(String id, List<String> roles, User user) {
        UserRoleRequest request = UserRoleRequest.builder()
                .userId(id)
                .roleIds(roles)
                .build();
        try {
            userRoleService.assignUserRole(request);
        }catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    private void validateInternal(RegisterRequest request, IAMRegisterRequest registerRequest, String authorHeader){
        if (Objects.isNull(request.getBrcd())) {
            throw new CommonException("Bạn chưa chọn chi nhánh");
        }

        if (Objects.isNull(request.getDepId())) {
            throw new CommonException("Bạn chưa chọn phòng ban");
        }

        try {
            iamClient.registerValidate(authorHeader, registerRequest);
        }catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }
}
