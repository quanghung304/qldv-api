package com.agribank.qldv_api.service.role;

import com.agribank.qldv_api.gateway.UserRoleClient;
import com.agribank.qldv_api.request.role.UserRoleRequest;
import com.agribank.qldv_api.response.DefaultResponse;

import com.agribank.qldv_api.response.role.RoleResponse;
import com.agribank.qldvutils.entity.UserRole;
import com.agribank.qldvutils.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final ModelMapper modelMapper;

    private final UserRoleClient userRoleClient;

    @Autowired
    private final RoleService roleService;

    public List<String> addUserRole(List<UserRoleRequest> userRoles) {
        List<String> addError = new ArrayList<>();
        List<UserRole> listRole = new ArrayList<>();
        try {
            for (UserRoleRequest data : userRoles) {
                List<String> roles = validateRole(data.getRoleIds());
                if (roles.isEmpty()) {
                    addError.add(data.getUserId());
                    continue;
                }
                listRole.addAll(roles.stream().map(role -> new UserRole(UUID.randomUUID().toString(), data.getUserId(), role)).toList());
            }
            DefaultResponse<List<UserRole>> roleDefaultResponse = userRoleClient.saveAll(listRole);
            if (!roleDefaultResponse.getSuccess()) {
                addError.addAll(userRoles.stream().map(UserRoleRequest::getUserId).toList());
            }
            return addError;
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }
    public List<String> updateUserRole(List<UserRoleRequest> userRoles) {
        List<String> updateError = new ArrayList<>();
        List<UserRole> listRole = new ArrayList<>();
        try {
            for (UserRoleRequest data : userRoles) {
                List<String> roles = validateRole(data.getRoleIds());
                if (roles.isEmpty() && !data.getRoleIds().isEmpty()) {
                    updateError.add(data.getUserId());
                    continue;
                }
                DefaultResponse<String> checkUserRole = userRoleClient.deleteById(data.getUserId());
                if (!checkUserRole.getSuccess()) {
                    updateError.add(data.getUserId());
                    continue;
                }
                if (data.getRoleIds().isEmpty()) {
                    continue;
                }
                listRole.addAll(roles.stream().map(role -> new UserRole(UUID.randomUUID().toString(), data.getUserId(), role)).toList());
            }
            DefaultResponse<List<UserRole>> roleDefaultResponse = userRoleClient.saveAll(listRole);
            if (!roleDefaultResponse.getSuccess()) {
                updateError.addAll(userRoles.stream().map(UserRoleRequest::getUserId).toList());
            }
            return updateError;
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    private List<String> validateRole(List<String> roles) {
        List<String> validateList = new ArrayList<>();
        if (roles.isEmpty()) {
            return validateList;
        }
        List<String> distinctList = roles.stream().distinct().toList();
        List<String> roleData = roleService.getAllRole().stream().map(RoleResponse::getId).toList();
        for (String role : distinctList) {
            if (roleData.contains(role)) {
                validateList.add(role);
            }
        }
        return  validateList;
    }
}
