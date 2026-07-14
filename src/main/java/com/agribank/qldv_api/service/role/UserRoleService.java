package com.agribank.qldv_api.service.role;

import com.agribank.qldv_api.gateway.UserClient;
import com.agribank.qldv_api.gateway.UserRoleClient;
import com.agribank.qldv_api.request.role.UserRoleRequest;
import com.agribank.qldv_api.response.DefaultResponse;

import com.agribank.qldv_api.response.role.RoleResponse;
import com.agribank.qldvutils.entity.Role;
import com.agribank.qldvutils.entity.User;
import com.agribank.qldvutils.entity.UserRole;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.response.DefaultListResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final ModelMapper modelMapper;

    private final UserRoleClient userRoleClient;
    private final UserClient userClient;
    private final RoleService roleService;

    public String assignUserRole(UserRoleRequest userRoleRequest) {
        User user = userClient.findById(userRoleRequest.getUserId()).getData().orElse(null);
        if (Objects.isNull(user)) {
            throw new CommonException("User not found");
        }

        List<String> idRoleRequests = userRoleRequest.getRoleIds().stream().distinct().toList();
        List<Role> roles = roleService.findByIdIn(idRoleRequests);

        List<Role> userRoleDB = roleService.getRoleByUserId(userRoleRequest.getUserId());
        Map<String, Role> userRoleDBMap = new HashMap<>();
        if (!userRoleDB.isEmpty()){
            for (Role role : userRoleDB) {
                userRoleDBMap.put(role.getId(), role);
            }
        }

        if (roles.isEmpty()) {
            throw new CommonException("Kiểm tra lại id role");
        }

        List<UserRole> userRoles = new ArrayList<>();
        for (Role role : roles) {
            Role roleDB = userRoleDBMap.getOrDefault(role.getId(), null);

            if (Objects.isNull(roleDB)) {
                userRoles.add(UserRole.builder()
                        .id(UUID.randomUUID().toString())
                        .roleId(role.getId())
                        .userId(userRoleRequest.getUserId())
                        .build());
            }
        }

        List<String> idRolesDeleted = new ArrayList<>();
        if (!userRoleDB.isEmpty()){
            idRolesDeleted = userRoleDB.stream()
                    .filter(r -> !idRoleRequests.contains(r.getId()))
                    .map(Role::getId)
                    .toList();
        }

        if (!idRolesDeleted.isEmpty()) {
            deleteById(UserRoleRequest.builder()
                    .userId(userRoleRequest.getUserId())
                    .roleIds(idRolesDeleted)
                    .build());
        }

        userRoleClient.saveAll(userRoles);
        return "Gán role thành công!";
    }

    public List<String> addUserRole(List<UserRoleRequest> userRoles) {
        List<String> addError = new ArrayList<>();
        List<UserRole> listUserRole = new ArrayList<>();
        List<String> roleData = roleService.getAllRole().stream().map(RoleResponse::getId).toList();
        try {
            for (UserRoleRequest data : userRoles) {
                List<String> roles = validateRole(data.getRoleIds(), roleData, new ArrayList<>());
                if (roles.isEmpty() && !data.getRoleIds().isEmpty()) {
                    addError.add(data.getUserId());
                    continue;
                }
                if (data.getRoleIds().isEmpty()) {
                    continue;
                }
                listUserRole.addAll(roles.stream().map(role -> new UserRole(UUID.randomUUID().toString(), data.getUserId(), role)).toList());
            }
            DefaultListResponse<UserRole> roleDefaultResponse = userRoleClient.saveAll(listUserRole);
            if (!roleDefaultResponse.getSuccess()) {
                addError.addAll(userRoles.stream().map(UserRoleRequest::getUserId).toList());
            }
            return addError;
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }
    public List<String> updateUserRole(UserRoleRequest userRoles) {
        List<String> updateError = new ArrayList<>();
        List<String> roleData = roleService.getAllRole().stream().map(RoleResponse::getId).toList();
        try {
            List<String> oldRoles = userRoleClient.getById(userRoles.getUserId()).getData().stream().map(UserRole::getRoleId).toList();
            List<String> roles = validateRole(userRoles.getRoleIds(), roleData, oldRoles);
            if (roles.isEmpty() && !userRoles.getRoleIds().isEmpty()) {
                updateError.add(userRoles.getUserId());
                return updateError;
            }
            UserRoleRequest delRequest = new UserRoleRequest();
            delRequest.setUserId(userRoles.getUserId());
            delRequest.setRoleIds(oldRoles.stream().filter(item-> !userRoles.getRoleIds().contains(item)).toList());
            DefaultResponse<String> checkUserRole = deleteById(delRequest);
            if (!checkUserRole.getSuccess()) {
                updateError.add(userRoles.getUserId());
                return updateError;
            }
            if (userRoles.getRoleIds().isEmpty()) {
                return updateError;
            }
            List<UserRole> listRole = new ArrayList<>(roles.stream().map(role -> new UserRole(UUID.randomUUID().toString(), userRoles.getUserId(), role)).toList());
            DefaultListResponse<UserRole> roleDefaultResponse = userRoleClient.saveAll(listRole);
            if (!roleDefaultResponse.getSuccess()) {
                updateError.add(userRoles.getUserId());
            }
            return updateError;
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    private List<String> validateRole(List<String> roles, List<String> roleData, List<String> oldRole) {
        try {
            List<String> validateList = new ArrayList<>();
            if (roles.isEmpty()) {
                return validateList;
            }
            List<String> distinctList = roles.stream().distinct().toList();
            for (String role : distinctList) {
                if (roleData.contains(role)) {
                    validateList.add(role);
                }
            }
            if (oldRole.isEmpty()) {
                return validateList;
            }
            validateList.removeIf(oldRole::contains);
            return validateList;
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    public DefaultResponse<String> deleteById(UserRoleRequest request) {
        try {
            return userRoleClient.deleteById(request);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }
}
