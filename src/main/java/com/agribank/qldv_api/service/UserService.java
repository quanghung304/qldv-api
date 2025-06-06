package com.agribank.qldv_api.service;

import com.agribank.qldv_api.enums.EUserStatus;
import com.agribank.qldv_api.gateway.*;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.role.UserRoleRequest;
import com.agribank.qldv_api.request.user.*;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.apiLog.UserSearchResponse;
import com.agribank.qldv_api.response.branch.BranchChildResponse;
import com.agribank.qldv_api.response.branch.BranchResponse;
import com.agribank.qldv_api.response.role.RoleDtoResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import com.agribank.qldv_api.service.log.UserLogService;
import com.agribank.qldv_api.service.role.UserRoleService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.dto.UserDto;
import com.agribank.qldvutils.entity.DV;
import com.agribank.qldvutils.entity.Organization;
import com.agribank.qldvutils.entity.Role;
import com.agribank.qldvutils.entity.User;
import com.agribank.qldvutils.exception.CommonException;
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

import java.util.*;

import static com.agribank.qldv_api.enums.Constants.BRANCH_CODE_HEAD_QUARTER;


@Service
@RequiredArgsConstructor
public class UserService {
    private final RoleClient roleClient;
    @Value("${qldv.app.id}")
    private Integer QLDV_APP_ID;
    private final IAMClient iamClient;
    private final UserClient userClient;
    private final ModelMapper modelMapper;
    private final UserLogService userLogService;
    private final BranchService branchService;
    private final OrganizationClient organizationClient;
    private final UserRoleService userRoleService;
    private final DVService dvService;

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
        //Kiểm tra quyền search user cho Chi nhánh
        checkPermissionSearchUser(request);

        PageResponse<User> userPageResponse = userClient.search(request).getData();
        PageResponse<UserResponse> response = new PageResponse<>();
        if (Objects.isNull(userPageResponse)) {
            return response;
        }
        response.setTotalPages(userPageResponse.getTotalPages());
        response.setCurrentPage(userPageResponse.getCurrentPage());
        response.setTotalItems(userPageResponse.getTotalItems());

        if (Objects.isNull(userPageResponse.getData())) {
          return response;
        }

        List<UserResponse> userResponses = userPageResponse.getData().stream()
                .map( user -> modelMapper.map(user, UserResponse.class)
                ).toList();

        List<Integer> brcds = new ArrayList<>();
        List<String> userIds = new ArrayList<>();
        userResponses.forEach(u -> {
            brcds.add(u.getBrcd());
            userIds.add(u.getId());
        });

        //lấy tên chi nhánh
        Map<Integer, BranchResponse> branchResponseMap = getBranchInfo(brcds);

        //Lấy role
        Map<String, List<String>> roleMap = getUserRole(userIds);

        for (UserResponse userResponse : userResponses) {
            BranchResponse branchResponse = branchResponseMap.getOrDefault(userResponse.getBrcd(), null);
            if (Objects.nonNull(branchResponse)) {
                userResponse.setBranchName(branchResponse.getLclbrnm());
            }

            List<String> roleName = roleMap.getOrDefault(userResponse.getId(), new ArrayList<>());
            userResponse.setRoles(roleName);
        }

        response.setData(userResponses);
        return response;
    }

    private void checkPermissionSearchUser(SearchUserRequest request){
        UserDetailsImpl userRequested = getUserRequested();
        //Check user chi nhanh
        BranchChildResponse branch = null;
        if (Objects.nonNull(request.getBrcd())) {
            branch = branchService.getBranchChild(request.getBrcd());
        }
        List<Integer> brcdChild = new ArrayList<>();
        brcdChild.add(userRequested.getBrcd());
        if (Objects.nonNull(branch)) {
            brcdChild = branch.getBranchChild().stream().map(BranchResponse::getBrcd).toList();
        }

        if (userRequested.getBrcd() > BRANCH_CODE_HEAD_QUARTER
                && Objects.nonNull(request.getBrcd()) && !brcdChild.contains(request.getBrcd())
        ){
            throw new CommonException("Bạn không có quyền tìm kiếm User chi nhánh khác");
        }

        if (userRequested.getBrcd() > BRANCH_CODE_HEAD_QUARTER && Objects.isNull(request.getBrcd())){
            request.setBrcd(userRequested.getBrcd());
        }
    }



    private Map<Integer, BranchResponse> getBranchInfo(List<Integer> brcds){
        if (brcds.isEmpty()){
            return null;
        }
        brcds = brcds.stream().distinct().toList();

        List<BranchResponse> branchResponses = new ArrayList<>();
        try {
            branchResponses = iamClient.getBranchInfo(getAuthorHeader(), brcds).getData();
        }catch (Exception e){
            System.out.println("getBranchInfo: " + e.getMessage());
        }

        Map<Integer, BranchResponse> branchResponseMap = new HashMap<>();
        if (!branchResponses.isEmpty()){
            for (BranchResponse branchResponse : branchResponses) {
                branchResponseMap.put(branchResponse.getBrcd(), branchResponse);
            }
        }

        return branchResponseMap;
    }

    private Map<String, List<String>> getUserRole(List<String> userIds){
        List<RoleDtoResponse> roleDtoResponses = new ArrayList<>();
        try {
            roleDtoResponses = roleClient.findByUserIds(userIds).getData();
        }catch (Exception e){
            System.out.println("getUserRole: " + e.getMessage());
        }

        Map<String, List<String>> roleMap = new HashMap<>();
        if (!roleDtoResponses.isEmpty()){
            for (RoleDtoResponse roleDtoResponse : roleDtoResponses) {
                List<String> role = roleMap.getOrDefault(roleDtoResponse.getUserId(), new ArrayList<>());
                role.add(roleDtoResponse.getRoleName());
                roleMap.put(roleDtoResponse.getUserId(), role);
            }
        }
        return roleMap;
    }

    public String changePassword(PasswordRequest request){
        request.setOldPassword(CommonUtils.handleEncryptPassword(request.getOldPassword(), publicKeyPath));
        request.setNewPassword(CommonUtils.handleEncryptPassword(request.getNewPassword(), publicKeyPath));
        return iamClient.changePassword(getAuthorHeader(), request).getData();
    }

    public String resetPassword(ResetPasswordRequest request){
        User user = userClient.findByUsername(request.getUsername()).getData();
        if(Objects.isNull(user)){
            throw new CommonException("Kiểm tra lại username!");
        }

        request.setPassword(CommonUtils.handleEncryptPassword(request.getPassword(), publicKeyPath));
        return iamClient.resetPassword(getAuthorHeader(), request).getMessage();
    }


    public String update(UserUpdateRequest userUpdateRequest){
        User user = findById(userUpdateRequest.getId());
        if(Objects.isNull(user)){
            throw new CommonException("Không tồn tại user vui lòng kiểm tra lại");
        }

        Organization organization = null;
        if (Objects.nonNull(userUpdateRequest.getOrganizationCode())){
            organization = organizationClient.findByCode(userUpdateRequest.getOrganizationCode()).getData();
        }

        if(Objects.nonNull(userUpdateRequest.getOrganizationCode()) && Objects.isNull(organization)){
            throw new CommonException("Kiểm tra lại mã TCD");
        }
        User userOld = (User) CommonUtils.handleCloneObject(user);

        UserIAMUpdate userIAMUpdate = UserIAMUpdate.builder()
                .appId(QLDV_APP_ID)
                .userId(user.getIdIam())
                .brcd(userUpdateRequest.getBrcd())
                .depId(userUpdateRequest.getDepId())
                .fullName(userUpdateRequest.getFullName())
                .build();

        user.setBrcd(userIAMUpdate.getBrcd());
        user.setDepId(userIAMUpdate.getDepId());
        user.setFullName(userIAMUpdate.getFullName());

        try {
            DefaultResponse<String> response = iamClient.updateUserIAM(getAuthorHeader(), userIAMUpdate);

            DV dv = null;
            if (Objects.nonNull(organization)){
                dv = dvService.findByStaffCode(userOld.getStaffCode());
            }

            if (Objects.nonNull(organization) && Objects.nonNull(dv)) {
                dv.setFullName(user.getFullName());
                dv.setOrganizationCode(userUpdateRequest.getOrganizationCode());

                dvService.save(dv);
            }

            userClient.save(user);

            if (!userUpdateRequest.getRoleIds().isEmpty()){
                UserRoleRequest userRoleRequest = UserRoleRequest.builder()
                        .userId(userUpdateRequest.getId())
                        .roleIds(userUpdateRequest.getRoleIds())
                        .build();
                userRoleService.assignUserRole(userRoleRequest);
            }

            userLogService.handlerWriteLogUpdate(userOld, user);
            return response.getMessage();
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    public String active(ActiveUserRequest request){
        User userNew = findById(request.getId());

        if(Objects.isNull(userNew)){
            throw new CommonException("Không tồn tại user vui lòng kiểm tra lại");
        }

        ActiveUserIAMRequest activeUserIAMRequest = ActiveUserIAMRequest.builder()
                .appId(QLDV_APP_ID)
                .userId(userNew.getIdIam())
                .type(request.getType())
                .build();

        try {
            DefaultResponse<String> response = iamClient.active(getAuthorHeader(), activeUserIAMRequest);
            User userOld = (User) CommonUtils.handleCloneObject(userNew);

            userNew.setActive(EUserStatus.getValue(request.getType()));
            userClient.save(userNew);

            userLogService.handlerWriteLogUpdate(userOld, userNew);
            return response.getData();
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    public String delete(String id){
        User user = userClient.findById(id).getData();
        if(Objects.isNull(user)){
            throw new CommonException("Không tồn tại user vui lòng kiểm tra lại");
        }

        try {
            DefaultResponse<String> response = iamClient.delete(getAuthorHeader(), user.getEmail(), QLDV_APP_ID);

            user.setDeleted(1);
            userClient.save(user);

            userLogService.handlerWriteLogDelete(user);
            return response.getMessage();
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    public User findById(String id){
        return userClient.findById(id).getData();
    }

    public UserResponse getUserInfo(String userId){
        if (Objects.isNull(userId)){
            userId = getUserRequested().getId();
        }
        User user = findById(userId);
        if (Objects.isNull(user)) {
            throw new CommonException("Không tìm thấy người dùng. Vui lòng kiểm tra lại!");
        }
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);

        Organization o = organizationClient.findByUserId(userResponse.getId()).getData();
        if (Objects.nonNull(o)) {
            userResponse.setOrganizationCode(o.getCode());
        }

        List<Integer> brcds = new ArrayList<>();
        brcds.add(user.getBrcd());
        Map<Integer, BranchResponse> branchResponseMap = getBranchInfo(brcds);
        BranchResponse branchResponse = branchResponseMap.getOrDefault(user.getBrcd(), null);

        if(Objects.nonNull(branchResponse)){
            userResponse.setBranchName(branchResponse.getLclbrnm());
        }

        List<Role> roles = getRoles(userId);
        if(!roles.isEmpty()){
            List<String> roleNames = new ArrayList<>();
            List<String> roleIds = new ArrayList<>();
            for(Role role : roles){
                roleNames.add(role.getName());
                roleIds.add(role.getId());
            }
            userResponse.setRoles(roleNames);
            userResponse.setRoleIds(roleIds);
        }

        return userResponse;
    }

    private List<Role> getRoles(String userId){
        try {
            return roleClient.getRolesByUserId(userId).getData();
        }catch (Exception e){
            System.out.println("user-service -> getRoles: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public String updateUserRequested(UserRequestedUpdate request){
        UserDetailsImpl userRequested = getUserRequested();

        User user = findById(userRequested.getId());
        if (Objects.isNull(user)) {
            throw new CommonException("Hệ thống đang không tìm thấy User của bạn. Vui lòng thử lại sau");
        }

        user.setVneid(request.getVneid());
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        iamClient.userUpdate(getAuthorHeader(), request);
        userClient.save(user);

        return "Cập nhật thông tin thành công";
    }

    public UserDto findByStaffCodeAndOrganizationCode(String staffCode, String organizationCode){
        return userClient.userOrganization(staffCode, organizationCode).getData();
    }
}
