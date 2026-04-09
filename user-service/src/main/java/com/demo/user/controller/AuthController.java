package com.demo.user.controller;

import com.demo.common.api.ApiResponse;
import com.demo.user.dto.LoginRequest;
import com.demo.user.dto.LoginResponse;
import com.demo.user.dto.RegisterRequest;
import com.demo.user.dto.ValidateTokenResponse;
import com.demo.user.entity.UserAccount;
import com.demo.user.service.AuthService;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<UserAccount> register(@Valid @RequestBody RegisterRequest request) {
        UserAccount account = authService.register(request);
        account.setPasswordHash(null);
        return ApiResponse.ok("Register success", account);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Login success", authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserAccount> me(@RequestHeader("Authorization") String authorization) {
        String token = parseBearer(authorization);
        UserAccount account = authService.currentUser(token);
        account.setPasswordHash(null);
        return ApiResponse.ok(account);
    }

    @GetMapping("/internal/validate")
    public ValidateTokenResponse validate(@RequestParam String token) {
        return authService.validate(token);
    }

    private String parseBearer(String authorization) {
        if (authorization == null) {
            return null;
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}
