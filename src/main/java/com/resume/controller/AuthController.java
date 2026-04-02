package com.resume.controller;

import com.resume.config.SecurityUtils;
import com.resume.dto.ApiResponse;
import com.resume.dto.request.LoginRequest;
import com.resume.dto.request.RegisterRequest;
import com.resume.dto.response.LoginResponse;
import com.resume.service.AuthService;
import com.resume.service.impl.GitHubOAuthServiceImpl;
import com.resume.service.impl.RefreshTokenServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
    private final RefreshTokenServiceImpl refreshTokenService;
    private final com.resume.config.JwtUtil jwtUtil;

    @Value("${github.oauth.client-id}")
    private String githubClientId;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        LoginResponse response = authService.login(request, clientIp);
        return ApiResponse.success(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
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

    @PostMapping("/refresh")
    @Operation(summary = "用 refresh token 换新的 access token")
    public ApiResponse<LoginResponse> refresh(@RequestParam String refreshToken) {
        Long userId = refreshTokenService.verifyAndGetUserId(refreshToken);
        // 旧 refresh token 作废，签发新的（rotation 策略）
        refreshTokenService.revokeByToken(refreshToken);
        String newAccessToken = jwtUtil.generateTokenForUser(userId);
        String newRefreshToken = refreshTokenService.createRefreshToken(userId);
        LoginResponse resp = new LoginResponse(newAccessToken, newRefreshToken, null);
        return ApiResponse.success(resp);
    }

    @PostMapping("/logout")
    @Operation(summary = "登出，吊销 refresh token")
    public ApiResponse<Void> logout(@RequestParam(required = false) String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.revokeByToken(refreshToken);
        }
        return ApiResponse.success(null);
    }
}
