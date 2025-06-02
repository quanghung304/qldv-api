package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.EApiLogType;
import com.agribank.qldv_api.gateway.IAMClient;
import com.agribank.qldv_api.gateway.UserClient;
import com.agribank.qldv_api.request.IAMRegisterRequest;
import com.agribank.qldv_api.request.RegisterRequest;
import com.agribank.qldv_api.request.role.UserRoleRequest;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.user.ADResponse;
import com.agribank.qldv_api.response.user.UserIamResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import com.agribank.qldv_api.service.log.AuthenticationLogService;
import com.agribank.qldv_api.service.log.UserLogService;
import com.agribank.qldv_api.service.role.UserRoleService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.dto.EmployeeInfoDto;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.entity.User;
import com.agribank.qldvutils.exception.CommonException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRoleService userRoleService;
    @Value("${qldv.app.id}")
    private Integer QLDV_APP_ID;

    @Value("${app.service.publicKeyPath}")
    private String publicKeyPath;

    private final IAMClient iamClient;
    private final UserClient userClient;
    private final DVService dvService;
    private final ModelMapper modelMapper;
    private final UserLogService userLogService;
    private final AuthenticationLogService authenticationLogService;
    private final EmployeeInfoService employeeInfoService;

    public UserResponse register(RegisterRequest request) {
        IAMRegisterRequest registerRequest = IAMRegisterRequest.builder()
                .username(CommonUtils.splitUsername(request.getEmail()))
                .brcd(request.getBrcd())
                .email(request.getEmail())
                .phone(request.getPhone())
                .fullName(request.getFullName())
                .applicationIds(List.of(QLDV_APP_ID))
                .vneid(request.getVneid())
                .address(request.getAddress())
                .staffCode(request.getStaffCode())
                .depId(request.getDepId())
                .userKind(request.getUserKind())
                .password(CommonUtils.handleEncryptPassword(request.getPassword(), publicKeyPath))
                .build();

        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String authorHeader = "Bearer " + CommonUtils.getAccessToken(servletRequest);

        UserIamResponse userIamResponse = null;
        try {
            DefaultResponse<ADResponse> adResponse = iamClient.checkAd(authorHeader, registerRequest.getUsername(), 0);

            if (Objects.isNull(adResponse) || Objects.isNull(adResponse.getData())) {
                throw new CommonException("Email không đúng định dạng Agribank vui lòng kiểm tra lại");
            }

            EmployeeInfoDto employeeInfoDto = employeeInfoService.findByEmpno(request.getStaffCode()+"");
            if (Objects.isNull(employeeInfoDto)) {
                throw new CommonException("Mã nhân viên không chính xác vui lòng kiểm tra lại!");
            }

            User userNew = userClient.getUserByEmail(request.getEmail()).getData();
            User userOld = new User();
            String action = EApiLogType.UPDATE.getValue();

            if (Objects.isNull(userNew)) {
                userNew = new User();
                action = EApiLogType.INSERT.getValue();
                userNew.setEmail(request.getEmail());
                userNew.setUsername(registerRequest.getUsername());
            }else {
                userOld.setId(userNew.getId());
                userOld.setIdIam(userNew.getIdIam());
                userOld.setEmail(userNew.getEmail());
                userOld.setUsername(userNew.getUsername());
                userOld.setPhone(userNew.getPhone());
                userOld.setFullName(userNew.getFullName());
                userOld.setVneid(userNew.getVneid());
                userOld.setBrcd(userNew.getBrcd());
                userOld.setDepId(userNew.getDepId());
                userOld.setActive(userNew.getActive());
                userOld.setStaffCode(userNew.getStaffCode());
                userNew.setDeleted(userNew.getDeleted());
            }

            userNew.setFullName(request.getFullName());
            userNew.setActive(0);
            userNew.setDeleted(1);
            userNew.setBrcd(request.getBrcd());
            userNew.setDepId(request.getDepId());

            if (!String.valueOf(request.getBrcd()).equals(employeeInfoDto.getBrcd())){
                throw new CommonException("Kiểm tra lại mã nhân viên và chi nhánh trực thuộc");
            }
            userNew.setStaffCode(String.valueOf(request.getStaffCode()));

            DefaultResponse<User> savedUserResponse = userClient.save(userNew);
            if (!savedUserResponse.getSuccess() || Objects.isNull(savedUserResponse.getData())) {
                throw new CommonException(savedUserResponse.getMessage());
            }

            assignRole(savedUserResponse.getData().getId(), request.getRoleIds());
            //ghi log
            DefaultResponse<UserIamResponse> response = iamClient.register(authorHeader, registerRequest);

            userIamResponse = response.getData();

            userNew = userClient.getUserByEmail(request.getEmail()).getData();
            userNew.setPhone(userIamResponse.getPhone());
            userNew.setVneid(userIamResponse.getVneid());
            userNew.setIdIam(userIamResponse.getId());
            userNew.setDeleted(0);
            savedUserResponse = userClient.save(userNew);
            createDV(request, userIamResponse, employeeInfoDto);

            writeLog(action, userNew, userOld, registerRequest);
            return modelMapper.map(savedUserResponse.getData(), UserResponse.class);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    private void createDV(RegisterRequest request, UserIamResponse userIamResponse, EmployeeInfoDto employeeInfoDto){
        DV dv = dvService.findByStaffCode(String.valueOf(request.getStaffCode()));
        if (Objects.isNull(dv)){
            dv = new DV();
            dv.setStaffCode(String.valueOf(request.getStaffCode()));
        }

        dv.setOrganizationCode(request.getOrganizationCode());
        dv.setFullName(userIamResponse.getFullName());
        dv.setUsingName(employeeInfoDto.getEmpUsualName());
        dv.setVneid(String.valueOf(userIamResponse.getVneid()));
        dv.setBirthday(CommonUtils.timestampConvert(employeeInfoDto.getBirthdt()));
        dv.setBirthPlace(employeeInfoDto.getBirthAddress());
        dv.setHometown(employeeInfoDto.getNativeAddress());
        dv.setPermanentResidence(employeeInfoDto.getPermanentResidenceAddress());
        dv.setTemporaryResidence(employeeInfoDto.getTempResidenceAddress());

        try {
            dvService.save(dv);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void writeLog(String action, User userNew, User userOld, IAMRegisterRequest registerRequest) {
        if (EApiLogType.UPDATE.getValue().equals(action)) {
            userLogService.handlerWriteLogUpdate(userOld, userNew);
        }else {
            List<IAMRegisterRequest> iamRegisterRequests = new ArrayList<>();
            iamRegisterRequests.add(registerRequest);
            authenticationLogService.writeLogRegister(iamRegisterRequests);
        }
    }


    private void assignRole(String id, List<String> roles) {
        UserRoleRequest request = UserRoleRequest.builder()
                .userId(id)
                .roleIds(roles)
                .build();
        try {
            userRoleService.assignUserRole(request);
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
