package com.resume.service.impl;

import com.resume.config.JwtUtil;
import com.resume.entity.RefreshToken;
import com.resume.mapper.RefreshTokenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl {

    private final RefreshTokenMapper refreshTokenMapper;
    private final JwtUtil jwtUtil;

    /** 创建并持久化一个新的 refresh token */
    public String createRefreshToken(Long userId) {
        // 清理该用户旧 token（单设备策略，可按需改为多设备）
        refreshTokenMapper.deleteByUserId(userId);

        RefreshToken rt = new RefreshToken();
        rt.setUserId(userId);
        rt.setToken(jwtUtil.generateRefreshToken());
        rt.setExpiresAt(LocalDateTime.now().plusSeconds(
                jwtUtil.getRefreshTokenExpirationMs() / 1000));
        rt.setCreatedAt(LocalDateTime.now());
        refreshTokenMapper.insert(rt);
        return rt.getToken();
    }

    /**
     * 验证 refresh token，返回对应 userId。
     * 验证失败（不存在或已过期）抛出异常。
     */
    public Long verifyAndGetUserId(String token) {
        RefreshToken rt = refreshTokenMapper.findByToken(token);
        if (rt == null) {
            throw new RuntimeException("Refresh token 不存在或已被使用");
        }
        if (rt.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenMapper.deleteByToken(token);
            throw new RuntimeException("Refresh token 已过期，请重新登录");
        }
        return rt.getUserId();
    }

    /** 登出时删除 refresh token */
    public void revokeByToken(String token) {
        refreshTokenMapper.deleteByToken(token);
    }

    /** 定期清理过期 token（可配合 @Scheduled 调用） */
    public void cleanExpired() {
        refreshTokenMapper.deleteExpired();
    }
}
