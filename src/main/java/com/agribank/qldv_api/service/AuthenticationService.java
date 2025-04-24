package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.IAMClient;
import com.agribank.qldv_api.gateway.UserClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.IAMRegisterRequest;
import com.agribank.qldv_api.request.RegisterRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import com.agribank.qldv_api.response.user.UserTCDResponse;
import com.agribank.qldv_api.service.log.AuthenticationLogService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.User;
import com.agribank.qldvutils.enums.TrangThai;
import com.agribank.qldvutils.exception.CommonException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final IAMClient iamClient;
    private final UserClient userClient;
    private final ModelMapper modelMapper;
    private final AuthenticationLogService authenticationLogService;

    public UserTCDResponse register(RegisterRequest request) {
        IAMRegisterRequest registerRequest = IAMRegisterRequest.builder()
                .username(CommonUtils.splitUsername(request.getEmail()))
                .brcd(request.getBrcd())
                .email(request.getEmail())
                .phone(request.getTel())
                .fullName(request.getTen())
                .applicationIds(List.of(QLDV_APP_ID))
                .build();

        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authorHeader = "Bearer " + CommonUtils.getAccessToken(servletRequest);
        try {
            DefaultResponse response = iamClient.register(authorHeader, registerRequest);
            UserResponse userResponse = (UserResponse) response.getData();

            User user = User.builder()
                    .idIam(userResponse.getId())
                    .email(userResponse.getEmail())
                    .phone(userResponse.getPhone())
                    .fullName(userResponse.getFullName())
                    .username(userResponse.getUsername())
                    .vneid(userResponse.getVneid())
                    .brcd(userResponse.getBrcd())
                    .depId(userResponse.getDepartment().getId())
                    .phone(userResponse.getPhone())
                    .vneid(userResponse.getVneid())
                    .build();
            user.setId(UUID.randomUUID().toString());

            DefaultResponse<User> savedUserResponse = userClient.save(user);
            if (!savedUserResponse.getSuccess() || Objects.isNull(savedUserResponse.getData())) {
                throw new CommonException(savedUserResponse.getMessage());
            }

//            List<IAMRegisterRequest> iamRegisterRequests = new ArrayList<>();
//            iamRegisterRequests.add(registerRequest);
//            authenticationLogService.writeLogRegister(iamRegisterRequests);
            return modelMapper.map(savedUserResponse.getData(), UserTCDResponse.class);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }





}
