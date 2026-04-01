package com.resume.controller;

import com.resume.dto.ApiResponse;
import com.resume.dto.request.LoginRequest;
import com.resume.dto.request.RegisterRequest;
import com.resume.dto.response.LoginResponse;
import com.resume.service.AuthService;
import com.resume.service.impl.GitHubOAuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "认证接口", description = "用户登录注册相关接口")
public class AuthController {

    private final AuthService authService;
    private final GitHubOAuthServiceImpl gitHubOAuthService;

    @Value("${github.oauth.client-id}")
    private String githubClientId;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success("注册成功", null);
    }

    @GetMapping("/github/client-id")
    @Operation(summary = "获取 GitHub OAuth Client ID")
    public ApiResponse<String> getGithubClientId() {
        return ApiResponse.success(githubClientId);
    }

    @PostMapping("/github/callback")
    @Operation(summary = "GitHub OAuth 回调，用 code 换 JWT")
    public ApiResponse<LoginResponse> githubCallback(@RequestParam String code) {
        LoginResponse response = gitHubOAuthService.handleCallback(code);
        return ApiResponse.success(response);
    }
}
