package com.resume.mapper;

import com.resume.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT id, username, email, password, github_id, avatar_url, oauth_provider, status, created_at FROM users WHERE email = #{email}")
    User findByEmail(@Param("email") String email);

    @Select("SELECT id, username, email, password, github_id, avatar_url, oauth_provider, status, created_at FROM users WHERE github_id = #{githubId}")
    User findByGithubId(@Param("githubId") String githubId);

    @Select("SELECT COUNT(*) FROM users WHERE email = #{email}")
    boolean existsByEmail(@Param("email") String email);

    @Select("SELECT COUNT(*) FROM users WHERE username = #{username}")
    boolean existsByUsername(@Param("username") String username);

    @Insert("INSERT INTO users (username, email, password, github_id, avatar_url, oauth_provider, status, created_at) " +
            "VALUES (#{username}, #{email}, #{password}, #{githubId}, #{avatarUrl}, #{oauthProvider}, #{status}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Update("UPDATE users SET username=#{username}, avatar_url=#{avatarUrl} WHERE id=#{id}")
    void update(User user);
}
