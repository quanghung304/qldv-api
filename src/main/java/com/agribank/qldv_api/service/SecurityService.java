package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.ERole;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SecurityService {
    public static Integer BTCDU_CODE = 1000;

    public boolean isBTCDUTeller (Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        boolean isTeller = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(ERole.TELLER.getName()));

        if (isTeller && Objects.equals(userDetails.getBrcd(), BTCDU_CODE)) {
            return true;
        }

        return false;
    }
}
