package com.agribank.qldv_api.jwt;

import com.agribank.qldv_api.gateway.IAMClient;
import com.agribank.qldv_api.gateway.UserClient;
import com.agribank.qldv_api.response.user.UserIamResponse;
import com.agribank.qldv_api.service.role.RoleService;
import com.agribank.qldvutils.dto.UserDto;
import com.agribank.qldvutils.entity.Role;
import com.agribank.qldvutils.entity.User;
import com.agribank.qldvutils.exception.CommonException;
import com.agribank.qldvutils.response.BaseResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter{
    final IAMClient iamClient;
    final UserClient userClient;
    final RoleService roleService;
    final ModelMapper modelMapper;
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            if (!hasAuthorizationBearer(request)) {
                filterChain.doFilter(request, response);
                return;
            }
            UserIamResponse userIamResponse = iamClient.verifyToken().getData();

            if (Objects.isNull(userIamResponse)) {
                filterChain.doFilter(request, response);
                return;
            }

            User user = userClient.findByUsername(userIamResponse.getUsername()).getData();

//            if (Objects.isNull(user) && userIamResponse.getUsername().equals("admin")) {
//                User admin = generateAdminAccount(userIamResponse);
//                user = modelMapper.map(admin, UserDto.class);
//            }

            List<Role> roles = roleService.getRoleByUserId(user.getId());
            List<GrantedAuthority> roleNames = new ArrayList<>();
            if (!roles.isEmpty()) {
                roleNames = roles.stream().map(role -> new SimpleGrantedAuthority(role.getRoleCode())).collect(Collectors.toList());
            }
            List<String> roleCodes = roles.stream().map(Role::getRoleCode).filter(Objects::nonNull).collect(Collectors.toList());

            UserDetailsImpl userDetails = new UserDetailsImpl();
            userDetails.setId(user.getId());
            userDetails.setUsername(userIamResponse.getUsername());
            userDetails.setEmail(userIamResponse.getEmail());
            userDetails.setBrcd(user.getBrcd());
            userDetails.setIdIam(userIamResponse.getId());
            userDetails.setDepId(user.getDepId());
            userDetails.setFullName(user.getFullName());
            userDetails.setRoleCodes(roleCodes);
            userDetails.setPartyOrganizationId(user.getPartyOrganizationId());
            userDetails.setAuthorities(roleNames);

            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            throw new CommonException("Token khong hop le.");
        }
    }

    private boolean hasAuthorizationBearer(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        if (ObjectUtils.isEmpty(header) || !header.startsWith("Bearer")){
            return false;
        }
        return true;
    }

    private User generateAdminAccount(UserIamResponse userIamResponse) {
        User admin = User.builder()
                .idIam(userIamResponse.getId())
                .username(userIamResponse.getUsername())
                .fullName(userIamResponse.getFullName())
                .brcd(userIamResponse.getBrcd())
                .depId(userIamResponse.getDepartment().getId())
                .build();

        BaseResponse<User> response = userClient.save(admin);
        return response.getData();
    }
}
