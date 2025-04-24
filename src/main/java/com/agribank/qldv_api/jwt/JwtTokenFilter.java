package com.agribank.qldv_api.jwt;

import com.agribank.qldv_api.gateway.IAMClient;
import com.agribank.qldv_api.gateway.UserClient;
import com.agribank.qldv_api.response.DefaultResponse;
import com.agribank.qldv_api.response.user.UserResponse;
import com.agribank.qldvutils.entity.User;
import com.agribank.qldvutils.enums.TrangThai;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter{
    final IAMClient iamClient;
    final UserClient userClient;
    
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
        UserResponse userResponse = iamClient.verifyToken("Bearer " + token).getData();

        if (Objects.isNull(userResponse)){
            filterChain.doFilter(request, response);
            return;
        }

        User user = userClient.getUserByEmail(userResponse.getEmail()).getData();

        if (Objects.isNull(user) && userResponse.getUsername().equals("admin")) {
            user = generateAdminAccount(userResponse);
        }

        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setId(user.getId());
        userDetails.setDvCode(user.getDvCode());
        userDetails.setUsername(userResponse.getUsername());
        userDetails.setEmail(userResponse.getEmail());
        userDetails.setRoleId(user.getRoleId());
        userDetails.setBrcd(user.getBrcd());

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", null);
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

    private User generateAdminAccount(UserResponse userResponse) {
        User admin = User.builder()
                .idIam(userResponse.getId())
                .username(userResponse.getUsername())
                .roleId(userResponse.getId())
                .email(userResponse.getEmail())
                .fullName(userResponse.getFullName())
                .brcd(userResponse.getBrcd())
                .depId(userResponse.getDepartment().getId())
                .vneid(userResponse.getVneid())
                .build();
        DefaultResponse<User> response = userClient.save(admin);
        return response.getData();
    }
}
