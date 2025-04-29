package com.agribank.qldv_api.jwt;

import com.agribank.qldv_api.gateway.IAMClient;
import com.agribank.qldv_api.gateway.UserClient;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.user.UserIamResponse;
import com.agribank.qldv_api.service.role.RoleService;
import com.agribank.qldvutils.entity.Role;
import com.agribank.qldvutils.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String token;
        if(!hasAuthorizationBearer(request)){
            filterChain.doFilter(request, response);
            return;
        }
        token = getAccessToken(request);
        UserIamResponse userIamResponse = iamClient.verifyToken("Bearer " + token).getData();

        if (Objects.isNull(userIamResponse)){
            filterChain.doFilter(request, response);
            return;
        }

        User user = userClient.getUserByEmail(userIamResponse.getEmail()).getData();

        if (Objects.isNull(user) && userIamResponse.getUsername().equals("admin")) {
            user = generateAdminAccount(userIamResponse);
        }

        List<Role> roles = roleService.getRoleByUserId(user.getId());
        List<GrantedAuthority> roleNames = new ArrayList<>();
        if (!roles.isEmpty()){
            roleNames = roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
        }

        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setId(user.getId());
        userDetails.setDvCode(user.getDvCode());
        userDetails.setUsername(userIamResponse.getUsername());
        userDetails.setEmail(userIamResponse.getEmail());
        userDetails.setBrcd(user.getBrcd());
        userDetails.setAuthorities(userDetails.getAuthorities());
        userDetails.setIdIam(userIamResponse.getId());
        userDetails.setDepId(user.getDepId());
        userDetails.setFullName(user.getFullName());
        userDetails.setAuthorities(roleNames);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "" ,userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private boolean hasAuthorizationBearer(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        if (ObjectUtils.isEmpty(header) || !header.startsWith("Bearer")){
            return false;
        }
        return true;
    }

    private String getAccessToken(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        String token = header.split(" ")[1];
        return token;
    }

    private User generateAdminAccount(UserIamResponse userIamResponse) {
        User admin = User.builder()
                .idIam(userIamResponse.getId())
                .username(userIamResponse.getUsername())
                .email(userIamResponse.getEmail())
                .fullName(userIamResponse.getFullName())
                .brcd(userIamResponse.getBrcd())
                .depId(userIamResponse.getDepartment().getId())
                .vneid(userIamResponse.getVneid())
                .build();

        DefaultResponse<User> response = userClient.save(admin);
        return response.getData();
    }
}
