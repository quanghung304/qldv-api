package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.EApiLogType;
import com.agribank.qldv_api.gateway.IAMClient;
import com.agribank.qldv_api.gateway.UserClient;
import com.agribank.qldv_api.request.IAMRegisterRequest;
import com.agribank.qldv_api.request.RegisterRequest;
import com.agribank.qldv_api.request.dv.DVRequest;
import com.agribank.qldv_api.request.role.UserRoleRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.user.UserIamResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import com.agribank.qldv_api.service.log.AuthenticationLogService;
import com.agribank.qldv_api.service.log.UserLogService;
import com.agribank.qldv_api.service.role.RoleService;
import com.agribank.qldv_api.service.role.UserRoleService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.User;
import com.agribank.qldvutils.exception.CommonException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRoleService userRoleService;
    @Value("${qldv.app.id}")
    private Integer QLDV_APP_ID;

    @Value("${app.service.publicKeyPath}")
    private String publicKeyPath;

    private final IAMClient iamClient;
    private final UserClient userClient;
    private final DVService dvService;
    private final ModelMapper modelMapper;
    private final UserLogService userLogService;
    private final AuthenticationLogService authenticationLogService;
    private final RoleService roleService;

    public UserResponse register(RegisterRequest request) {
        IAMRegisterRequest registerRequest = IAMRegisterRequest.builder()
                .username(CommonUtils.splitUsername(request.getEmail()))
                .brcd(request.getBrcd())
                .email(request.getEmail())
                .phone(request.getPhone())
                .fullName(request.getFullName())
                .applicationIds(List.of(QLDV_APP_ID))
                .vneid(request.getVneid())
                .address(request.getAddress())
                .staffCode(request.getStaffCode())
                .depId(request.getDepId())
                .userKind(request.getUserKind())
                .password(CommonUtils.handleEncryptPassword(request.getPassword(), publicKeyPath))
                .build();

        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authorHeader = "Bearer " + CommonUtils.getAccessToken(servletRequest);
        try {
            DefaultResponse<UserIamResponse> response = iamClient.register(authorHeader, registerRequest);

            UserIamResponse userIamResponse = response.getData();

            User userNew = userClient.getUserByEmail(userIamResponse.getEmail()).getData();
            User userOld = new User();
            String action = EApiLogType.UPDATE.getValue();

            if (Objects.isNull(userNew)) {
                userNew = new User();
                userNew.setId(UUID.randomUUID().toString());
                action = EApiLogType.INSERT.getValue();
                userNew.setIdIam(userIamResponse.getId());
                userNew.setEmail(userIamResponse.getEmail());
                userNew.setUsername(userIamResponse.getUsername());
            }else {
                userOld.setId(userNew.getId());
                userOld.setIdIam(userNew.getIdIam());
                userOld.setEmail(userNew.getEmail());
                userOld.setUsername(userNew.getUsername());
                userOld.setPhone(userNew.getPhone());
                userOld.setFullName(userNew.getFullName());
                userOld.setVneid(userNew.getVneid());
                userOld.setBrcd(userNew.getBrcd());
                userOld.setDepId(userNew.getDepId());
                userOld.setActive(userNew.getActive());
                userOld.setStaffCode(userNew.getStaffCode());
                userNew.setDeleted(userNew.getDeleted());
            }

            userNew.setFullName(userIamResponse.getFullName());
            userNew.setPhone(userIamResponse.getPhone());
            userNew.setVneid(userIamResponse.getVneid());
            userNew.setBrcd(userIamResponse.getBrcd());
            userNew.setDepId(userIamResponse.getDepartment().getId());
            userNew.setActive(userIamResponse.getActive());
            userNew.setDeleted(0);
            if (Objects.nonNull(userIamResponse.getStaffCode())){
                userNew.setStaffCode(String.valueOf(userIamResponse.getStaffCode()));
            }

            DefaultResponse<User> savedUserResponse = userClient.save(userNew);
            if (!savedUserResponse.getSuccess() || Objects.isNull(savedUserResponse.getData())) {
                throw new CommonException(savedUserResponse.getMessage());
            }

            if (Objects.nonNull(userIamResponse.getStaffCode())) {
                DVRequest dvRequest = DVRequest.builder()
                        .code(String.valueOf(userIamResponse.getStaffCode()))
                        .fullName(userIamResponse.getFullName())
                        .gender(userIamResponse.getGender())
                        .vneid(String.valueOf(userIamResponse.getVneid()))
                        .build();
                List<DVRequest> dvRequests = new ArrayList<>();
                dvRequests.add(dvRequest);
                dvService.create(dvRequests);
            }

            assignRole(userNew.getId(), request.getRoleIds());
            //ghi log
            writeLog(action, userNew, userOld, registerRequest);
            return modelMapper.map(savedUserResponse.getData(), UserResponse.class);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    private void writeLog(String action, User userNew, User userOld, IAMRegisterRequest registerRequest) {
        if (EApiLogType.UPDATE.getValue().equals(action)) {
            userLogService.handlerWriteLogUpdate(userOld, userNew);
        }else {
            List<IAMRegisterRequest> iamRegisterRequests = new ArrayList<>();
            iamRegisterRequests.add(registerRequest);
            authenticationLogService.writeLogRegister(iamRegisterRequests);
        }
    }


    private void assignRole(String id, List<String> roles) {
        UserRoleRequest request = UserRoleRequest.builder()
                .userId(id)
                .roleIds(roles)
                .build();
        try {
            userRoleService.assignUserRole(request);
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
