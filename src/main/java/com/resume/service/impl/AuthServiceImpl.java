package com.resume.service.impl;

import com.resume.dto.request.LoginRequest;
import com.resume.dto.request.RegisterRequest;
import com.resume.dto.response.LoginResponse;
import com.resume.entity.User;
import com.resume.repository.UserRepository;
import com.resume.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("邮箱或密码错误"));

        // 简单验证（实际项目应该用 BCrypt 加密）
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("邮箱或密码错误");
        }

        // 生成简单的 token（实际项目应该用 JWT）
        String token = UUID.randomUUID().toString();

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());

        return new LoginResponse(token, userInfo);
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
        user.setPassword(request.getPassword()); // 实际项目应该加密
        user.setStatus(1);
        user.setCreatedAt(java.time.LocalDateTime.now());

        userRepository.save(user);
    }
}

