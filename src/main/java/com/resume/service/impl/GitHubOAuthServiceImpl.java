package com.resume.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.config.JwtUtil;
import com.resume.dto.response.LoginResponse;
import com.resume.entity.User;
import com.resume.repository.UserRepository;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubOAuthServiceImpl {

    @Value("${github.oauth.client-id}")
    private String clientId;

    @Value("${github.oauth.client-secret}")
    private String clientSecret;

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${proxy.host:}")
    private String proxyHost;

    @Value("${proxy.port:0}")
    private int proxyPort;

    @Value("${github.oauth.skip-ssl-verify:false}")
    private boolean skipSslVerify;

    private RestTemplate buildRestTemplate() {
        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

            if (skipSslVerify) {
                // 本地开发：跳过 SSL 验证（代理 MITM 场景）
                TrustManager[] trustAll = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                        public void checkClientTrusted(X509Certificate[] c, String a) {}
                        public void checkServerTrusted(X509Certificate[] c, String a) {}
                    }
                };
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAll, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            }

            if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
                factory.setProxy(new java.net.Proxy(
                    java.net.Proxy.Type.HTTP,
                    new java.net.InetSocketAddress(proxyHost, proxyPort)
                ));
            }
            factory.setConnectTimeout(15000);
            factory.setReadTimeout(20000);
            return new RestTemplate(factory);
        } catch (Exception e) {
            throw new RuntimeException("初始化 RestTemplate 失败", e);
        }
    }

    public LoginResponse handleCallback(String code) {
        // 1. 用 code 换 access_token
        String accessToken = exchangeCodeForToken(code);

        // 2. 用 access_token 获取 GitHub 用户信息
        JsonNode githubUser = fetchGitHubUser(accessToken);

        String githubId = githubUser.get("id").asText();
        String login = githubUser.get("login").asText();
        String avatarUrl = githubUser.has("avatar_url") ? githubUser.get("avatar_url").asText() : null;
        String email = githubUser.has("email") && !githubUser.get("email").isNull()
                ? githubUser.get("email").asText() : null;

        // 3. 查找或创建用户
        // 优先按 githubId 查找，其次按 email 绑定已有账号，最后创建新用户
        User user = userRepository.findByGithubId(githubId).orElseGet(() -> {
            // 若 email 不为空，尝试绑定已有账号
            if (email != null && !email.isEmpty()) {
                Optional<User> existingUser = userRepository.findByEmail(email);
                if (existingUser.isPresent()) {
                    User existing = existingUser.get();
                    existing.setGithubId(githubId);
                    if (existing.getOauthProvider() == null) {
                        existing.setOauthProvider("github");
                    }
                    log.info("GitHub OAuth: 绑定已有账号 email={}, userId={}", email, existing.getId());
                    return userRepository.save(existing);
                }
            }
            // 创建新用户，username 冲突时加 githubId 后缀
            String username = userRepository.existsByUsername(login)
                    ? login + "_" + githubId
                    : login;
            User newUser = new User();
            newUser.setGithubId(githubId);
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setAvatarUrl(avatarUrl);
            newUser.setOauthProvider("github");
            newUser.setStatus(1);
            newUser.setCreatedAt(LocalDateTime.now());
            return userRepository.save(newUser);
        });

        // 更新头像（每次登录同步）
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        // 4. 生成 JWT，subject = "user:{id}"，统一格式
        String token = jwtUtil.generateTokenForUser(user.getId());
        String refreshToken = refreshTokenService.createRefreshToken(user.getId());

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());

        return new LoginResponse(token, refreshToken, userInfo);
    }

    private String exchangeCodeForToken(String code) {
        RestTemplate restTemplate = buildRestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");

        Map<String, String> body = new HashMap<>();
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("code", code);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "https://github.com/login/oauth/access_token", request, JsonNode.class);

        JsonNode responseBody = response.getBody();
        if (responseBody == null || !responseBody.has("access_token")) {
            throw new RuntimeException("GitHub OAuth 失败：无法获取 access_token");
        }
        return responseBody.get("access_token").asText();
    }

    private JsonNode fetchGitHubUser(String accessToken) {
        RestTemplate restTemplate = buildRestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github+json");

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "https://api.github.com/user", HttpMethod.GET, request, JsonNode.class);

        if (response.getBody() == null) {
            throw new RuntimeException("无法获取 GitHub 用户信息");
        }
        return response.getBody();
    }
}
