package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.Constants;
import com.agribank.qldv_api.gateway.OrganizationClient;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CheckAuthorityService {
    private final OrganizationClient organizationClient;

    public void hasAuthorityOverOrganization(String organizationCode) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (Objects.equals(userDetails.getOrganizationCode(), Constants.BTCDU_CODE)) {
            return;
        }

        List<Organization> childOrganizations = organizationClient.findByParent(organizationCode).getData();
        for (Organization child: childOrganizations) {
            if (Objects.equals(child.getCode(), userDetails.getOrganizationCode())) {
                return;
            }
        }

        throw new CommonException("Tài khoản không có quyền thao tác với tổ chức đảng này");
    }
}
