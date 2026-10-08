package com.knowledgehub.account.controller;

import com.knowledgehub.account.entity.User;
import com.knowledgehub.account.service.UserService;
import com.knowledgehub.account.vo.UserVO;
import com.knowledgehub.common.exception.BusinessException;
import com.knowledgehub.common.result.Result;
import com.knowledgehub.framework.context.UserContext;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/info")
    public Result<UserVO> info() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        return Result.success(vo);
    }
}
