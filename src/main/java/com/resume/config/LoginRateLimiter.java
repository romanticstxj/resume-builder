package com.resume.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 登录防暴力破解：同一 IP 或邮箱连续失败 5 次后锁定 15 分钟
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_FAILURES = 5;
    private static final long LOCK_MINUTES = 15;

    // key -> 失败次数，15 分钟无活动后自动过期
    private final Cache<String, AtomicInteger> failureCache = CacheBuilder.newBuilder()
            .expireAfterWrite(LOCK_MINUTES, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    /**
     * 检查是否已被锁定（失败次数 >= MAX_FAILURES）
     */
    public boolean isBlocked(String ip, String email) {
        return isKeyBlocked(ip) || isKeyBlocked(email);
    }

    /**
     * 记录一次登录失败
     */
    public void recordFailure(String ip, String email) {
        increment(ip);
        increment(email);
    }

    /**
     * 登录成功后清除计数
     */
    public void clearFailures(String ip, String email) {
        failureCache.invalidate(ip);
        failureCache.invalidate(email);
    }

    private boolean isKeyBlocked(String key) {
        AtomicInteger count = failureCache.getIfPresent(key);
        return count != null && count.get() >= MAX_FAILURES;
    }

    private void increment(String key) {
        AtomicInteger count = failureCache.getIfPresent(key);
        if (count == null) {
            AtomicInteger newCount = new AtomicInteger(1);
            // putIfAbsent-style: another thread may have inserted between get and put
            failureCache.put(key, newCount);
        } else {
            count.incrementAndGet();
        }
    }
}
