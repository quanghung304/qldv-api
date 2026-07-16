package com.agribank.qldv_api.service;


import com.agribank.qldv_api.enums.Constants;
import com.agribank.qldv_api.enums.EAuthType;
import com.agribank.qldv_api.enums.EUserStatus;
import com.agribank.qldv_api.gateway.*;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.role.UserRoleRequest;
import com.agribank.qldv_api.request.user.*;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.PageItemsResponse;
import com.agribank.qldv_api.response.apiLog.UserSearchIamResponse;
import com.agribank.qldv_api.response.branch.BranchResponse;
import com.agribank.qldv_api.response.role.RoleDtoResponse;
import com.agribank.qldv_api.response.user.UserListResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import com.agribank.qldv_api.service.log.UserLogService;
import com.agribank.qldv_api.service.role.UserRoleService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.dto.UserDto;
import com.agribank.qldvutils.entity.Role;
import com.agribank.qldvutils.entity.User;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.request.SearchUserRequest;
import com.agribank.qldvutils.response.PageResponse;
import com.agribank.qldvutils.response.user.UserSearchResponse;
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
import static com.agribank.qldv_api.enums.Constants.BTCDU_CODE;


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
    private final UserRoleService userRoleService;

    @Value("${app.service.publicKeyPath}")
    private String publicKeyPath;

    public UserDetailsImpl getUserRequested() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }

        return null;
    }

    public UserSearchIamResponse searchUserIam(SearchUserIAMRequest request){
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

    public PageResponse<UserSearchResponse> searchQLDV(SearchUserRequest request){
        //Kiểm tra quyền search user cho Chi nhánh
        checkPermissionSearchUser(request);
        if (Objects.isNull(request.getOrderBy())){
            request.setOrderBy("createdAt");
        }

        PageResponse<UserSearchResponse> response = userClient.search(request).getData();
        if(Objects.isNull(response.getData()) || response.getData().isEmpty()){
            return response;
        }

        List<UserSearchResponse> userResponses = response.getData();

        List<Integer> brcds = new ArrayList<>();
        List<String> userIds = new ArrayList<>();
        userResponses.forEach(u -> {
            if (Objects.nonNull(u.getBrcd())) {
                brcds.add(u.getBrcd());
            }
            userIds.add(u.getId());
        });

        //lấy tên chi nhánh
        Map<Integer, BranchResponse> branchResponseMap = getBranchInfo(brcds);

        //Lấy role
        Map<String, List<RoleDtoResponse>> roleMap = getUserRoleDetails(userIds);

        for (UserSearchResponse userResponse : userResponses) {
            BranchResponse branchResponse = branchResponseMap.getOrDefault(userResponse.getBrcd(), null);
            if (Objects.nonNull(branchResponse)) {
                userResponse.setBranchName(branchResponse.getLclbrnm());
                userResponse.setUnitName(branchResponse.getLclbrnm());
            }

            List<RoleDtoResponse> roles = roleMap.getOrDefault(userResponse.getId(), new ArrayList<>());
            userResponse.setRoles(roles.stream().map(RoleDtoResponse::getRoleName).toList());
            RoleDtoResponse displayRole = getDisplayRole(roles, request.getRoleId());
            if (Objects.nonNull(displayRole)) {
                userResponse.setRoleId(getRoleIdentifier(displayRole));
                userResponse.setRoleName(displayRole.getRoleName());
            }
            userResponse.setAccountStatus(toContractStatus(userResponse.getAccountStatus()));
        }

        response.setData(userResponses);
        return response;
    }

    public PageItemsResponse<UserListResponse> searchUsers(SearchUserRequest request) {
        PageResponse<UserSearchResponse> response = searchQLDV(request);
        List<UserListResponse> items = Objects.isNull(response.getData())
                ? new ArrayList<>()
                : response.getData().stream().map(this::toUserListResponse).toList();

        return PageItemsResponse.<UserListResponse>builder()
                .items(items)
                .currentPage(response.getCurrentPage())
                .totalItems(response.getTotalItems())
                .totalPages(response.getTotalPages())
                .build();
    }

    private UserListResponse toUserListResponse(UserSearchResponse user) {
        return UserListResponse.builder()
                .userId(Objects.nonNull(user.getUserId()) ? user.getUserId() : user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .unitId(Objects.nonNull(user.getUnitId()) ? user.getUnitId() : user.getBrcd())
                .unitName(Objects.nonNull(user.getUnitName()) ? user.getUnitName() : user.getBranchName())
                .roleId(user.getRoleId())
                .roleName(user.getRoleName())
                .accountStatus(toContractStatus(user.getAccountStatus()))
                .createdAt(user.getCreatedAt())
                .build();
    }

    private void checkPermissionSearchUser(SearchUserRequest request){
        UserDetailsImpl userRequested = getUserRequested();
        if (Objects.isNull(userRequested)) {
            return;
        }

        if (hasAuthority(userRequested, "R-ADM")) {
            return;
        }

        if (hasAuthority(userRequested, "R-QTVCS")) {
            Integer brcd = userRequested.getBrcd();
            if (Objects.isNull(brcd)) {
                throw new CommonException("Khong xac dinh duoc don vi cua nguoi dung");
            }

            if (Objects.nonNull(request.getBrcd()) && !Objects.equals(request.getBrcd(), brcd)) {
                throw new CommonException("Ban khong co quyen tim kiem User don vi khac");
            }

            request.setBrcd(brcd);
            return;
        }

        String userOrganizationCode = userRequested.getOrganizationCode();

        if (!BTCDU_CODE.equals(userOrganizationCode)
                && Objects.nonNull(request.getOrganizationCode()) && !request.getOrganizationCode().contains(userOrganizationCode)
        ){
            throw new CommonException("Bạn không có quyền tìm kiếm User chi, đảng bộ khác");
        }

        if (!BTCDU_CODE.equals(userOrganizationCode) && Objects.isNull(request.getOrganizationCode())){
            request.setOrganizationCode(userOrganizationCode);
        }
    }

    private boolean hasAuthority(UserDetailsImpl user, String authority) {
        return Objects.nonNull(user.getAuthorities())
                && user.getAuthorities().stream().anyMatch(a -> authority.equals(a.getAuthority()));
    }



    private Map<Integer, BranchResponse> getBranchInfo(List<Integer> brcds){
        if (brcds.isEmpty()){
            return new HashMap<>();
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

    private Map<String, List<RoleDtoResponse>> getUserRoleDetails(List<String> userIds){
        List<RoleDtoResponse> roleDtoResponses = new ArrayList<>();
        try {
            roleDtoResponses = roleClient.findByUserIds(userIds).getData();
        }catch (Exception e){
            System.out.println("getUserRole: " + e.getMessage());
        }

        Map<String, List<RoleDtoResponse>> roleMap = new HashMap<>();
        if (!roleDtoResponses.isEmpty()){
            for (RoleDtoResponse roleDtoResponse : roleDtoResponses) {
                List<RoleDtoResponse> roles = roleMap.getOrDefault(roleDtoResponse.getUserId(), new ArrayList<>());
                roles.add(roleDtoResponse);
                roleMap.put(roleDtoResponse.getUserId(), roles);
            }
        }
        return roleMap;
    }

    private RoleDtoResponse getDisplayRole(List<RoleDtoResponse> roles, String roleId) {
        if (roles.isEmpty()) {
            return null;
        }

        if (Objects.nonNull(roleId) && !roleId.isBlank()) {
            return roles.stream()
                    .filter(r -> roleId.equals(r.getRoleId()) || roleId.equals(r.getRoleCode()))
                    .findFirst()
                    .orElse(roles.get(0));
        }

        return roles.get(0);
    }

    private String getRoleIdentifier(RoleDtoResponse role) {
        if (Objects.nonNull(role.getRoleCode()) && !role.getRoleCode().isBlank()) {
            return role.getRoleCode();
        }
        return role.getRoleId();
    }

    private String toContractStatus(String status) {
        if (EUserStatus.INACTIVE.name().equals(status)) {
            return "LOCKED";
        }
        return status;
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

        User userOld = (User) CommonUtils.handleCloneObject(user);

        UserIAMUpdate userIAMUpdate = UserIAMUpdate.builder()
                .appId(QLDV_APP_ID)
                .userId(user.getIdIam())
                .brcd(userUpdateRequest.getBrcd())
                .depId(userUpdateRequest.getDepId())
                .fullName(userUpdateRequest.getFullName())
                .build();

        user.setBrcd(userUpdateRequest.getBrcd());
        user.setDepId(userUpdateRequest.getDepId());
        user.setFullName(userUpdateRequest.getFullName());

        try {
            if (EAuthType.SSO_EMAIL.name().equals(user.getAuthType())){
                DefaultResponse<String> response = iamClient.updateUserIAM(getAuthorHeader(), userIAMUpdate);
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
            return "Thành công";
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    public String active(String id){
        User userNew = findById(id);

        if(Objects.isNull(userNew)){
            throw new CommonException("Không tồn tại user vui lòng kiểm tra lại");
        }

        String accountStatus = EUserStatus.ACTIVE.name().equals(userNew.getAccountStatus())
                ? EUserStatus.INACTIVE.name()
                : EUserStatus.ACTIVE.name();
        ActiveUserIAMRequest activeUserIAMRequest = ActiveUserIAMRequest.builder()
                .appId(QLDV_APP_ID)
                .userId(userNew.getIdIam())
                .type(accountStatus)
                .build();

        try {
            DefaultResponse<String> response = iamClient.active(getAuthorHeader(), activeUserIAMRequest);
            User userOld = (User) CommonUtils.handleCloneObject(userNew);

            userNew.setAccountStatus(accountStatus);
            userClient.save(userNew);

            userLogService.handlerWriteLogUpdate(userOld, userNew);
            return response.getData();
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    public String delete(String id){
        User user = userClient.findById(id).getData().orElse(null);
        if(Objects.isNull(user)){
            throw new CommonException("Không tồn tại user vui lòng kiểm tra lại");
        }

        try {
            DefaultResponse<String> response = iamClient.delete(getAuthorHeader(),
                    user.getUsername() + Constants.EMAIL_DOMAIN, QLDV_APP_ID);

            user.setDeleted(1);
            userClient.save(user);

            userLogService.handlerWriteLogDelete(user);
            return response.getMessage();
        }catch (Exception e){
            throw new CommonException(e.getMessage());
        }
    }

    public User findById(String id){
        return userClient.findById(id).getData().orElse(null);
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
                roleNames.add(role.getRoleName());
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

        user.setFullName(request.getFullName());

        iamClient.userUpdate(getAuthorHeader(), request);
        userClient.save(user);

        return "Cập nhật thông tin thành công";
    }

    public UserDto findByStaffCodeAndOrganizationCode(String staffCode, String organizationCode){
        return userClient.userOrganization(staffCode, organizationCode).getData();
    }
}
