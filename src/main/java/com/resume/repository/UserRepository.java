package com.resume.repository;

import com.resume.entity.User;
import com.resume.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

    private final UserMapper userMapper;

    public UserRepository(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Optional<User> findByEmail(String email) {
        User user = userMapper.findByEmail(email);
        return Optional.ofNullable(user);
    }

    public Optional<User> findByGithubId(String githubId) {
        User user = userMapper.findByGithubId(githubId);
        return Optional.ofNullable(user);
    }    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userMapper.existsByUsername(username);
    }

    public User save(User user) {
        if (user.getId() == null) {
            userMapper.insert(user);
        } else {
            userMapper.update(user);
        }
        return user;
    }
}


