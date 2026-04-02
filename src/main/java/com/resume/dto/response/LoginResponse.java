package com.resume.dto.response;

public class LoginResponse {
    private String token;
    private String refreshToken;
    private UserInfo userInfo;

    public LoginResponse() {}

    public LoginResponse(String token, String refreshToken, UserInfo userInfo) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userInfo = userInfo;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String avatarUrl;

        public UserInfo() {
        }

        public UserInfo(Long id, String username, String email, String avatarUrl) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.avatarUrl = avatarUrl;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
}

