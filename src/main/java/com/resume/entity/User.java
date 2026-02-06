package com.resume.entity;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 用户实体（MyBatis 版本）
 */
@Getter
@Setter
public class User {

    private Long id;

    private String username;

    private String email;

    private String password;

    private Integer status = 1;  // 1: active, 0: inactive

    private LocalDateTime createdAt;
}
