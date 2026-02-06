package com.resume.service;

import com.resume.dto.request.LoginRequest;
import com.resume.dto.request.RegisterRequest;
import com.resume.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void register(RegisterRequest request);
}
