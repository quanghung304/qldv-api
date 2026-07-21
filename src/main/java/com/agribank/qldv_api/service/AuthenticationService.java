package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.*;
import com.agribank.qldv_api.gateway.IAMClient;
import com.agribank.qldv_api.gateway.StaffClient;
import com.agribank.qldv_api.gateway.UserClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.IAMRegisterRequest;
import com.agribank.qldv_api.request.RegisterRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.user.ADResponse;
import com.agribank.qldv_api.response.user.UserIamResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import com.agribank.qldv_api.service.log.AuthenticationLogService;
import com.agribank.qldvutils.entity.Staff;
import com.agribank.qldvutils.entity.User;
import com.agribank.qldvutils.entity.UserRole;
import com.agribank.qldvutils.enums.EAccountStatus;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.user.UserEntityRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
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

        validateInternal(request, registerRequest);

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
            UserIamResponse userIamResponse = iamClient.register(registerRequest).getData();

            user.setIdIam(userIamResponse.getId());
            UserEntityRequest userEntityRequest = new UserEntityRequest();
            userEntityRequest.setUser(user);
            userEntityRequest.setStaff(createPMStaff(user, request));
            userEntityRequest.setUserRoles(request.getRoleIds().stream()
                    .map(ur -> UserRole.builder()
                            .roleId(ur).build())
                    .toList());

            userClient.saveEntity(userEntityRequest);

            writeLog(registerRequest);
            return modelMapper.map(user, UserResponse.class);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    private Staff createPMStaff(User user, RegisterRequest request){
        if (Objects.isNull(request.getStaffCode())) {
            return null;
        }

        Staff existedStaff = staffClient.findByStaffCode(user.getStaffCode()).getData();
        if (Objects.nonNull(existedStaff)) {
            existedStaff.setBrcd(request.getBrcd());
            existedStaff.setOrganizationId(request.getOrganizationId());
            existedStaff.setFullName(request.getFullName());
            return existedStaff;
        }

        return Staff.builder()
                .staffCode(user.getStaffCode())
                .fullName(user.getFullName())
                .organizationId(request.getOrganizationId())
                .brcd(request.getBrcd())
                .build();
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


    private void validateInternal(RegisterRequest request, IAMRegisterRequest registerRequest){
        if (Objects.isNull(request.getBrcd())) {
            throw new CommonException("Bạn chưa chọn chi nhánh");
        }

        if (Objects.isNull(request.getDepId())) {
            throw new CommonException("Bạn chưa chọn phòng ban");
        }

        try {
            iamClient.registerValidate(registerRequest);
        }catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }
}
