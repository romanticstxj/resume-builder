package com.resume.mapper;

import com.resume.entity.RefreshToken;
import org.apache.ibatis.annotations.*;

@Mapper
public interface RefreshTokenMapper {

    @Insert("INSERT INTO refresh_tokens (user_id, token, expires_at, created_at) " +
            "VALUES (#{userId}, #{token}, #{expiresAt}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RefreshToken token);

    @Select("SELECT id, user_id, token, expires_at, created_at FROM refresh_tokens WHERE token = #{token}")
    RefreshToken findByToken(@Param("token") String token);

    @Delete("DELETE FROM refresh_tokens WHERE token = #{token}")
    void deleteByToken(@Param("token") String token);

    @Delete("DELETE FROM refresh_tokens WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM refresh_tokens WHERE expires_at < now()")
    void deleteExpired();
}
