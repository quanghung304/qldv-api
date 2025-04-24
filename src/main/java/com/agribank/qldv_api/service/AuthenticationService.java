package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.IAMClient;
import com.agribank.qldv_api.gateway.UserClient;
import com.agribank.qldv_api.request.IAMRegisterRequest;
import com.agribank.qldv_api.request.RegisterRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.user.UserIamResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import com.agribank.qldv_api.service.log.AuthenticationLogService;
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
    @Value("${qldv.app.id}")
    private Integer QLDV_APP_ID;

    @Value("${app.service.publicKeyPath}")
    private String publicKeyPath;

    private final IAMClient iamClient;
    private final UserClient userClient;
    private final ModelMapper modelMapper;
    private final AuthenticationLogService authenticationLogService;

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
            DefaultResponse response = iamClient.register(authorHeader, registerRequest);

            UserIamResponse userIamResponse = (UserIamResponse) response.getData();

            User user = User.builder()
                    .idIam(userIamResponse.getId())
                    .email(userIamResponse.getEmail())
                    .phone(userIamResponse.getPhone())
                    .fullName(userIamResponse.getFullName())
                    .username(userIamResponse.getUsername())
                    .vneid(userIamResponse.getVneid())
                    .brcd(userIamResponse.getBrcd())
                    .depId(userIamResponse.getDepartment().getId())
                    .phone(userIamResponse.getPhone())
                    .vneid(userIamResponse.getVneid())
                    .build();
            user.setId(UUID.randomUUID().toString());

            DefaultResponse<User> savedUserResponse = userClient.save(user);
            if (!savedUserResponse.getSuccess() || Objects.isNull(savedUserResponse.getData())) {
                throw new CommonException(savedUserResponse.getMessage());
            }

            List<IAMRegisterRequest> iamRegisterRequests = new ArrayList<>();
            iamRegisterRequests.add(registerRequest);
            authenticationLogService.writeLogRegister(iamRegisterRequests);
            return modelMapper.map(savedUserResponse.getData(), UserResponse.class);
        } catch (Exception e) {

            throw new CommonException(e.getMessage());
        }
    }





}
