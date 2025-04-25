package com.agribank.qldv_api.service;

import com.agribank.qldv_api.exception.ValidationException;
import com.agribank.qldv_api.gateway.IAMClient;
import com.agribank.qldv_api.gateway.UserClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.user.*;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.apiLog.UserSearchResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.entity.User;
import com.agribank.qldvutils.response.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;


@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${qldv.app.id}")
    private Integer QLDV_APP_ID;

    private Integer BRANCH_CODE_HEAD_QUARTER = 1090;

    private final IAMClient iamClient;

    private final UserClient userClient;

    private final ModelMapper modelMapper;

    @Value("${app.service.publicKeyPath}")
    private String publicKeyPath;

    public UserDetailsImpl getUserRequested() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }

        return null;
    }

    public UserSearchResponse searchUserIam(SearchUserIAMRequest request){
        String authorHeader = getAuthorHeader();

        request.validate();
        if(!request.getPrntbrcd().isBlank() && BRANCH_CODE_HEAD_QUARTER >= Integer.parseInt(request.getPrntbrcd())){
            request.setBrcd("");
            request.setPrntbrcd("");
        }

        return iamClient.search(authorHeader, QLDV_APP_ID,
                request.getBrcd(),
                request.getName(),
                request.getPrntbrcd(),
                request.getPage(),
                request.getPageSize()
                ).getData();
    }

    private String getAuthorHeader(){
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return  "Bearer " + CommonUtils.getAccessToken(servletRequest);
    }

    public PageResponse<UserResponse> searchQLDV(SearchUserRequest request){
        PageResponse<User> userPageResponse = userClient.search(request).getData();
        PageResponse<UserResponse> response = new PageResponse<>();
        if (Objects.isNull(userPageResponse)) {
            return response;
        }

        response.setTotalPages(userPageResponse.getTotalPages());
        response.setCurrentPage(userPageResponse.getCurrentPage());
        response.setTotalItems(userPageResponse.getTotalItems());

        if (Objects.nonNull(userPageResponse.getData())) {
            response.setData(userPageResponse.getData().stream()
                    .map( user -> modelMapper.map(user, UserResponse.class)
                    ).toList()
            );
        }

        return response;
    }

    public String changePassword(PasswordRequest request){
        request.setOldPassword(CommonUtils.handleEncryptPassword(request.getOldPassword(), publicKeyPath));
        request.setNewPassword(CommonUtils.handleEncryptPassword(request.getNewPassword(), publicKeyPath));
        return iamClient.changePassword(getAuthorHeader(), request).getData();
    }

    public String resetPassword(ResetPasswordRequest request){
        User user = userClient.findByUsername(request.getUsername()).getData();
        if(Objects.isNull(user)){
            throw new ValidationException("Kiểm tra lại username!");
        }

        request.setPassword(CommonUtils.handleEncryptPassword(request.getPassword(), publicKeyPath));
        return iamClient.resetPassword(getAuthorHeader(), request).getMessage();
    }


    public String update(UserUpdateRequest userUpdateRequest){
        User user = userClient.findByIdIAM(userUpdateRequest.getIdIam()).getData();
        if(Objects.isNull(user)){
            throw new ValidationException("Không tồn tại user vui lòng kiểm tra lại");
        }

        UserIAMUpdate userIAMUpdate = UserIAMUpdate.builder()
                .appId(QLDV_APP_ID)
                .userId(userUpdateRequest.getIdIam())
                .brcd(userUpdateRequest.getBrcd())
                .depId(userUpdateRequest.getDepId())
                .fullName(userUpdateRequest.getFullName())
                .build();

        user.setBrcd(userIAMUpdate.getBrcd());
        user.setDepId(userIAMUpdate.getDepId());
        user.setFullName(userIAMUpdate.getFullName());
        try {
            DefaultResponse<String> response = iamClient.updateUserIAM(getAuthorHeader(), userIAMUpdate);
            userClient.save(user);

            return response.getMessage();
        }catch (Exception e){
            throw new ValidationException(e.getMessage());
        }
    }
}
