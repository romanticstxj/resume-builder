package com.resume.config;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 从 SecurityContext 解析当前登录用户 ID。
 * JWT subject 格式统一为 "user:{id}"。
 */
public class SecurityUtils {

    private SecurityUtils() {}

    /**
     * 获取当前登录用户 ID，解析失败抛出异常（不降级到 1L）。
     */
    public static Long getCurrentUserId() {
        String subject = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        if (subject != null && subject.startsWith("user:")) {
            try {
                return Long.parseLong(subject.substring(5));
            } catch (NumberFormatException ignored) {}
        }
        throw new RuntimeException("无法解析当前用户身份，请重新登录");
    }
}
