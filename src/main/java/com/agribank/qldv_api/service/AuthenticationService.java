package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.IAMClient;
import com.agribank.qldv_api.gateway.UserGateway;
import com.agribank.qldv_api.request.IAMRegisterRequest;
import com.agribank.qldv_api.request.RegisterRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import com.agribank.qldv_api.response.user.UserTCDResponse;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.User;
import com.agribank.qldvutils.enums.TrangThai;
import com.agribank.qldvutils.exception.CommonException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    @Value("${qldv.app.id}")
    private Integer QLDV_APP_ID;

    private final IAMClient iamClient;
    private final UserGateway userGateway;
    private final ModelMapper modelMapper;

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
                    .maSo(request.getMaSo())
                    .maSoTCD(request.getMaSoTCD())
                    .ten(userResponse.getFullName())
                    .quyen(request.getQuyen())
                    .chucVu(request.getChucVu())
                    .maSoThamChieu(String.valueOf(userResponse.getId()))
                    .tel(userResponse.getPhone())
                    .email(userResponse.getEmail())
                    .trangThai(TrangThai.ACTIVE.getValue())
                    .build();

            userGateway.save(user);

            return modelMapper.map(user, UserTCDResponse.class);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

}
