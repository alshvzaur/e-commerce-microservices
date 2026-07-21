package com.zaur.user_service.controller;


import com.zaur.user_service.dto.AuthResponse;
import com.zaur.user_service.dto.LoginRequest;
import com.zaur.user_service.dto.RegisterRequest;
import com.zaur.user_service.model.Role;
import com.zaur.user_service.model.User;
import com.zaur.user_service.security.JwtUtil;
import com.zaur.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    AuthenticationManager authenticationManager;
    PasswordEncoder passwordEncoder;
    JwtUtil jwtUtil;
    UserService userService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest registerRequest) {
        User user = User.builder()
                .name(registerRequest.getName())
                .surname(registerRequest.getSurname())
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .role(Role.USER)
                .build();

        User registeredUser = userService.saveUser(user);
        AuthResponse authResponse = new AuthResponse(jwtUtil.generateToken(registeredUser.getUsername(), registeredUser.getRole().name(), registeredUser.getId()));

        return authResponse;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        ));

        User user = userService.findByUsername(loginRequest.getUsername());

        AuthResponse authResponse = new AuthResponse(jwtUtil.generateToken(user.getUsername(), user.getRole().name(),  user.getId()));

        return authResponse;
    }



}
