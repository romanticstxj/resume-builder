package com.resume.mapper;

import com.resume.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT id, username, email, password, status, created_at FROM users WHERE email = #{email}")
    User findByEmail(@Param("email") String email);

    @Select("SELECT COUNT(*) FROM users WHERE email = #{email}")
    boolean existsByEmail(@Param("email") String email);

    @Insert("INSERT INTO users (username, email, password, status, created_at) " +
            "VALUES (#{username}, #{email}, #{password}, #{status}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);
}
