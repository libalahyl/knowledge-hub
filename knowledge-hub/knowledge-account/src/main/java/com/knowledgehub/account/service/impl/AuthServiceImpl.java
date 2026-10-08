package com.knowledgehub.account.service.impl;

import com.knowledgehub.account.entity.User;
import com.knowledgehub.account.service.AuthService;
import com.knowledgehub.account.service.UserService;
import com.knowledgehub.account.vo.LoginVO;
import com.knowledgehub.account.vo.UserVO;
import com.knowledgehub.common.exception.BusinessException;
import com.knowledgehub.framework.jwt.JwtUtil;
import com.knowledgehub.framework.jwt.TokenBlacklistUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 认证服务实现
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserService userService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private TokenBlacklistUtil tokenBlacklistUtil;

    @Override
    public LoginVO login(String username, String password) {
        User user = userService.getByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(toUserVO(user));
        return vo;
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            Date expiration = jwtUtil.getExpiration(token);
            tokenBlacklistUtil.addToBlacklist(token, expiration);
            log.info("用户登出，token 已加入黑名单");
        } catch (Exception e) {
            log.error("登出失败", e);
        }
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        return vo;
    }
}
