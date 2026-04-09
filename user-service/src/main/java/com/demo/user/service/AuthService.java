package com.demo.user.service;

import com.demo.user.dto.LoginRequest;
import com.demo.user.dto.LoginResponse;
import com.demo.user.dto.RegisterRequest;
import com.demo.user.dto.ValidateTokenResponse;
import com.demo.user.entity.UserAccount;

public interface AuthService {

    UserAccount register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    ValidateTokenResponse validate(String token);

    UserAccount currentUser(String token);
}
