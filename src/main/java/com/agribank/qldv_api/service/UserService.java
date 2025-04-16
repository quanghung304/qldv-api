package com.agribank.qldv_api.service;

import com.agribank.qldvutils.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    public UserDto getUserRequested() {
        UserDto user = null;
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(Objects.nonNull(requestAttributes)){
            user = (UserDto) requestAttributes.getAttribute("User", RequestAttributes.SCOPE_REQUEST);
        }
        return user;
    }
}
