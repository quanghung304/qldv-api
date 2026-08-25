package com.agribank.qldv_api.service;


import com.agribank.qldv_api.enums.Constants;
import com.agribank.qldv_api.enums.EUserStatus;
import com.agribank.qldv_api.gateway.*;
import com.agribank.qldv_api.jwt.UserDetailsImpl;
import com.agribank.qldv_api.request.role.UserRoleRequest;
import com.agribank.qldv_api.request.user.*;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.apiLog.UserSearchIamResponse;
import com.agribank.qldv_api.response.branch.BranchChildResponse;
import com.agribank.qldv_api.response.branch.BranchResponse;
import com.agribank.qldv_api.response.role.RoleDtoResponse;
import com.agribank.qldv_api.response.role.RoleResponse;
import com.agribank.qldv_api.response.user.UserListResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import com.agribank.qldv_api.service.log.UserLogService;
import com.agribank.qldv_api.service.role.UserRoleService;
import com.agribank.qldv_api.utils.CommonUtils;
import com.agribank.qldvutils.dto.UserDto;
import com.agribank.qldvutils.entity.Role;
import com.agribank.qldvutils.entity.Staff;
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


@Service
@RequiredArgsConstructor
public class UserService {
    private final RoleClient roleClient;
    @Value("${qldv.app.id}")
    private Integer QLDV_APP_ID;
    private final IAMClient iamClient;
    private final UserClient userClient;
    private final StaffClient staffClient;
    private final OrganizationClient organizationClient;
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
        request.validate();
        if(!request.getPrntbrcd().isBlank() && BRANCH_CODE_HEAD_QUARTER >= Integer.parseInt(request.getPrntbrcd())){
            request.setBrcd("");
            request.setPrntbrcd("");
        }

        return iamClient.search(QLDV_APP_ID,
                request.getBrcd(),
                request.getName(),
                request.getPrntbrcd(),
                request.getPage(),
                request.getPageSize()
                ).getData();
    }

    public PageResponse<UserSearchResponse> searchQLDV(SearchUserRequest request){
        //Kiểm tra quyền search user cho Chi nhánh
        if (Objects.isNull(request.getOrderBy())){
            request.setOrderBy("createdAt");
        }

        PageResponse<UserSearchResponse> response = userClient.search(request).getData();
        if(Objects.isNull(response.getData()) || response.getData().isEmpty()){
            return response;
        }

        List<UserSearchResponse> userResponses = response.getData();

        List<Integer> brcds = new ArrayList<>();
        userResponses.forEach(u -> {
            if (Objects.nonNull(u.getBrcd())) {
                brcds.add(u.getBrcd());
            }
        });

        //lấy tên chi nhánh
        Map<Integer, BranchResponse> branchResponseMap = getBranchInfo(brcds);

        for (UserSearchResponse userResponse : userResponses) {
            BranchResponse branchResponse = branchResponseMap.getOrDefault(userResponse.getBrcd(), null);
            if (Objects.nonNull(branchResponse)) {
                userResponse.setBranchName(branchResponse.getLclbrnm());
            }
        }

        response.setData(userResponses);
        return response;
    }

    public PageResponse<UserListResponse> searchUsers(UserSearchRequest request) {
        SearchUserRequest searchUserRequest = modelMapper.map(request, SearchUserRequest.class);
        searchUserRequest.setBrcds(checkValidateAndGetBrcds(request.getBrcd()));
        PageResponse<UserSearchResponse> response = searchQLDV(searchUserRequest);
        List<UserSearchResponse> userResponses = response.getData();

        List<UserListResponse> items;
        if (Objects.isNull(userResponses) || userResponses.isEmpty()) {
            items = new ArrayList<>();
        } else {
            List<String> userIds = userResponses.stream().map(UserSearchResponse::getId).toList();
            //Lấy toàn bộ role của từng user (1 user có thể có nhiều role)
            Map<String, List<RoleDtoResponse>> roleMap = getUserRoleDetails(userIds);
            items = userResponses.stream()
                    .map(user -> toUserListResponse(user, roleMap.getOrDefault(user.getId(), new ArrayList<>())))
                    .toList();
        }

        PageResponse<UserListResponse> responsePageResponse = new PageResponse<>();
        responsePageResponse.setData(items);
        responsePageResponse.setCurrentPage(response.getCurrentPage());
        responsePageResponse.setTotalPages(response.getTotalPages());
        responsePageResponse.setTotalItems(response.getTotalItems());

        return responsePageResponse;
    }

    public List<Integer> checkValidateAndGetBrcds(Integer brcd) {
        UserDetailsImpl userRequested = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (Objects.isNull(brcd) && userRequested.getBrcd() > Constants.FIRST_LV1_BRCD) {
            brcd = userRequested.getBrcd();
        }

        if (Objects.isNull(brcd)) {
            return null;
        }

        List<BranchResponse> branchUserRequested = getBranchResponse(userRequested.getBrcd());
        List<BranchResponse> branchIAMResponses = getBranchResponse(brcd);

        if (branchIAMResponses.isEmpty() && userRequested.getBrcd().equals(brcd)) {
            return List.of(brcd);
        }

        List<Integer> brcdOfUserRequested = new ArrayList<>();
        if(!branchUserRequested.isEmpty()){
            brcdOfUserRequested = branchUserRequested.stream().map(BranchResponse::getBrcd).toList();
        }else {
            brcdOfUserRequested = List.of(userRequested.getBrcd());
        }

        if (!brcdOfUserRequested.contains(brcd) && !isHeadOffice(userRequested.getBrcd())) {
            throw new CommonException("Bạn không có quyền truy cập chi nhánh " + brcd);
        }

        if (branchIAMResponses.isEmpty()){
            return List.of(brcd);
        }

        return  branchIAMResponses.stream().map(BranchResponse::getBrcd).toList();
    }

    public boolean isHeadOffice(Integer brcd) {
        return Constants.HEAD_OFFICE_BRCD <= brcd && brcd < Constants.FIRST_LV1_BRCD;
    }

    public List<BranchResponse> getBranchResponse(Integer brcd){
        List<BranchChildResponse> branchIAMResponse = iamClient.getBranchChildInfo(List.of(brcd)).getData();

        List<BranchResponse> response = new ArrayList<>();

        if (Objects.nonNull(branchIAMResponse) && !branchIAMResponse.isEmpty()) {
            BranchResponse parentBranch = new BranchResponse();
            parentBranch.setBrcd(branchIAMResponse.get(0).getBrcd());
            parentBranch.setEngbrnm(branchIAMResponse.get(0).getEngbrnm());
            parentBranch.setEngbrshrtnm(branchIAMResponse.get(0).getEngbrshrtnm());
            parentBranch.setLclbrnm(branchIAMResponse.get(0).getLclbrnm());
            parentBranch.setLclbrshrtnm(branchIAMResponse.get(0).getLclbrshrtnm());


            response.add(parentBranch);
            response.addAll(branchIAMResponse.get(0).getBranchChild());
        }

        return response;
    }

    private UserListResponse toUserListResponse(UserSearchResponse user, List<RoleDtoResponse> roles) {
        return UserListResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .brcd(user.getBrcd())
                .branchName(user.getBranchName())
                .roles(roles.stream().map(this::toRoleResponse).toList())
                .accountStatus(user.getAccountStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private RoleResponse toRoleResponse(RoleDtoResponse role) {
        RoleResponse roleResponse = new RoleResponse();
        roleResponse.setId(getRoleIdentifier(role));
        roleResponse.setName(role.getRoleName());
        return roleResponse;
    }

    private boolean hasAuthority(UserDetailsImpl user, String authority) {
        return Objects.nonNull(user.getAuthorities())
                && user.getAuthorities().stream().anyMatch(a -> authority.equals(a.getAuthority()));
    }

    private String getAuthorHeader() {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return "Bearer " + CommonUtils.getAccessToken(servletRequest);
    }



    private Map<Integer, BranchResponse> getBranchInfo(List<Integer> brcds){
        if (brcds.isEmpty()){
            return new HashMap<>();
        }
        brcds = brcds.stream().distinct().toList();

        List<BranchResponse> branchResponses = new ArrayList<>();
        try {
            branchResponses = iamClient.getBranchInfo(brcds).getData();
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

    private String getRoleIdentifier(RoleDtoResponse role) {
        if (Objects.nonNull(role.getRoleCode()) && !role.getRoleCode().isBlank()) {
            return role.getRoleCode();
        }
        return role.getRoleId();
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
        return iamClient.changePassword(request).getData();
    }

    public String resetPassword(ResetPasswordRequest request){
        User user = userClient.findByUsername(request.getUsername()).getData();
        if(Objects.isNull(user)){
            throw new CommonException("Kiểm tra lại username!");
        }

        request.setPassword(CommonUtils.handleEncryptPassword(request.getPassword(), publicKeyPath));
        return iamClient.resetPassword(request).getMessage();
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

        if (Objects.nonNull(userUpdateRequest.getOrganizationId())){
            organizationClient.findById(userUpdateRequest.getOrganizationId()).getData().orElseThrow(() -> new CommonException("Kiểm tra lại Tổ chức Đảng"));
        }

        Staff staff = Objects.nonNull(user.getStaffCode())
                ? staffClient.findByStaffCode(user.getStaffCode()).getData()
                : null;

        if (Objects.nonNull(staff)){
            staff.setFullName(userUpdateRequest.getFullName());
            staff.setBrcd(userUpdateRequest.getBrcd());
            if (Objects.nonNull(userUpdateRequest.getOrganizationId())){
                staff.setOrganizationId(userUpdateRequest.getOrganizationId());
            }
        }

        try {
            iamClient.updateUserIAM(getAuthorHeader(), userIAMUpdate);

            userClient.save(user);

            if (!userUpdateRequest.getRoleIds().isEmpty()){
                UserRoleRequest userRoleRequest = UserRoleRequest.builder()
                        .userId(userUpdateRequest.getId())
                        .roleIds(userUpdateRequest.getRoleIds())
                        .build();
                userRoleService.assignUserRole(userRoleRequest);
            }

            if (Objects.nonNull(staff)){
                staffClient.save(staff);
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

        boolean isActive = Objects.equals(EUserStatus.ACTIVE.getId(), userNew.getAccountStatus());
        String iamStatusType = isActive ? EUserStatus.INACTIVE.name() : EUserStatus.ACTIVE.name();
        Integer newAccountStatus = isActive ? EUserStatus.INACTIVE.getId() : EUserStatus.ACTIVE.getId();
        ActiveUserIAMRequest activeUserIAMRequest = ActiveUserIAMRequest.builder()
                .appId(QLDV_APP_ID)
                .userId(userNew.getIdIam())
                .type(iamStatusType)
                .build();

        try {
            DefaultResponse<String> response = iamClient.active(activeUserIAMRequest);
            User userOld = (User) CommonUtils.handleCloneObject(userNew);

            userNew.setAccountStatus(newAccountStatus);
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

        Staff staff =  null;
        if (Objects.nonNull(user.getStaffCode())){
            staff = staffClient.findByStaffCode(user.getStaffCode()).getData();
        }

        if (Objects.nonNull(staff)) {
            userResponse.setOrganizationId(staff.getOrganizationId());
        }

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

        iamClient.userUpdate(request);
        userClient.save(user);

        return "Cập nhật thông tin thành công";
    }

    public UserDto findByStaffCodeAndOrganizationCode(String staffCode, String organizationCode){
        return userClient.userOrganization(staffCode, organizationCode).getData();
    }
}
