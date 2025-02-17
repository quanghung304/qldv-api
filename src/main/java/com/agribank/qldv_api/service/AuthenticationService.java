package com.agribank.qldv_api.service;

import com.agribank.qldv_api.config.security.JwtService;
import com.agribank.qldv_api.enums.ERole;
import com.agribank.qldv_api.models.Role;
import com.agribank.qldv_api.models.User;
import com.agribank.qldv_api.repository.RoleRepository;
import com.agribank.qldv_api.repository.UserRepository;
import com.agribank.qldv_api.request.AuthenticationRequest;
import com.agribank.qldv_api.request.RegisterRequest;
import com.agribank.qldv_api.response.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;

    public AuthenticationResponse register(RegisterRequest request) {
        List<Role> roleList = new ArrayList<>();
        Role role = roleRepository.findRoleByName(ERole.XDCB_TELLER.name());
        if (Objects.isNull(role)){
            role = new Role();
            role.setName(ERole.XDCB_TELLER.name());
            role.setId(ERole.XDCB_TELLER.getId());
            role.setDescription("teller");
        }
        roleList.add(role);
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .brcd(request.getBrcd())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roleList)
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
