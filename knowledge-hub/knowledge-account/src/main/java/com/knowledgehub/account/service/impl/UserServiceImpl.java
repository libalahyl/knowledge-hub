package com.knowledgehub.account.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.knowledgehub.account.entity.User;
import com.knowledgehub.account.mapper.UserMapper;
import com.knowledgehub.account.service.UserService;
import com.knowledgehub.common.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
    }

    @Override
    public User register(String username, String password, String nickname) {
        if (getByUsername(username) != null) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname == null || nickname.isBlank() ? username : nickname);
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
        return user;
    }
}
