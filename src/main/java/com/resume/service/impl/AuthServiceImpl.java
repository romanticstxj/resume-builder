package com.resume.service.impl;

import com.resume.config.JwtUtil;
import com.resume.config.LoginRateLimiter;
import com.resume.config.TooManyRequestsException;
import com.resume.dto.request.LoginRequest;
import com.resume.dto.request.RegisterRequest;
import com.resume.dto.response.LoginResponse;
import com.resume.entity.User;
import com.resume.repository.UserRepository;
import com.resume.service.impl.RefreshTokenServiceImpl;
import com.resume.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final LoginRateLimiter loginRateLimiter;

    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {
        String email = request.getEmail();

        // 防暴力破解：检查是否已被锁定
        if (loginRateLimiter.isBlocked(clientIp, email)) {
            throw new TooManyRequestsException("登录尝试过多，请稍后再试");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    loginRateLimiter.recordFailure(clientIp, email);
                    return new RuntimeException("邮箱或密码错误");
                });

        // BCrypt 密码验证
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginRateLimiter.recordFailure(clientIp, email);
            throw new RuntimeException("邮箱或密码错误");
        }

        // 登录成功，清除失败计数
        loginRateLimiter.clearFailures(clientIp, email);

        // 生成 JWT token（subject = "user:{id}"）
        String token = jwtUtil.generateTokenForUser(user.getId());
        String refreshToken = refreshTokenService.createRefreshToken(user.getId());

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());

        return new LoginResponse(token, refreshToken, userInfo);
    }

    @Override
    public void register(RegisterRequest request) {
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(1);
        user.setCreatedAt(java.time.LocalDateTime.now());

        userRepository.save(user);
    }
}

