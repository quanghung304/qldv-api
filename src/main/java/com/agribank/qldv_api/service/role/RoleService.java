package com.agribank.qldv_api.service.role;

import com.agribank.qldv_api.gateway.RoleClient;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.role.RoleResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import com.agribank.qldvutils.entity.Role;
import com.agribank.qldvutils.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    @Value("${qldv.app.id}")
    private Integer QLDV_APP_ID;

    @Value("${app.service.publicKeyPath}")
    private String publicKeyPath;

    private final ModelMapper modelMapper;

    private final RoleClient roleClient;

    public RoleResponse findRoleById(String id) {
        try {
            DefaultResponse<Role> roleDefaultResponse = roleClient.findRoleById(id);
            return modelMapper.map(roleDefaultResponse.getData(), RoleResponse.class);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<RoleResponse> getAllRole() {
        try {
            DefaultResponse<List<Role>> roleDefaultResponse = roleClient.getAllRole();
            return roleDefaultResponse.getData().stream()
                    .map(role -> modelMapper.map(role, RoleResponse.class))
                    .toList();
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }
}
