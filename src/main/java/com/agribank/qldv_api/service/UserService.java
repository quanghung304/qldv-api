package com.agribank.qldv_api.service;

import com.agribank.qldv_api.gateway.IAMClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.user.SearchUserRequest;
import com.agribank.qldv_api.response.apiLog.UserSearchResponse;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
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

    public UserDetailsImpl getUserRequested() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }

        return null;
    }

    public UserSearchResponse search(SearchUserRequest request){
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
}
