package com.knowledgehub.framework.interceptor;

import com.knowledgehub.common.exception.BusinessException;
import com.knowledgehub.common.result.ResultCode;
import com.knowledgehub.framework.annotation.RequireAdmin;
import com.knowledgehub.framework.annotation.RequireLogin;
import com.knowledgehub.framework.context.UserContext;
import com.knowledgehub.framework.jwt.JwtUtil;
import com.knowledgehub.framework.jwt.TokenBlacklistUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 登录校验 + 权限注解校验拦截器
 * <p>token 为可选：有则解析写入 UserContext，无/非法不抛异常，交由 @RequireLogin / @RequireAdmin 决定</p>
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private TokenBlacklistUtil tokenBlacklistUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. 尝试解析 token（可选，失败不抛异常）
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            token = request.getHeader("token");
        }
        if (token != null && !token.isBlank()) {
            // 先查黑名单（只有 token 不为空才查，游客无 token 不查）
            if (tokenBlacklistUtil.isBlacklisted(token)) {
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "登录已失效，请重新登录");
            }
            try {
                Claims claims = jwtUtil.parseToken(token);
                UserContext.setUserId(Long.valueOf(claims.getSubject()));
                UserContext.setUsername(claims.get("username", String.class));
                UserContext.setRole(claims.get("role", String.class));
            } catch (Exception e) {
                // token 非法或过期：不抛异常，交由注解判断
            }
        }

        // 2. 权限注解校验（仅对 Controller 方法生效）
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            // @RequireLogin：需要登录
            RequireLogin requireLogin = handlerMethod.getMethodAnnotation(RequireLogin.class);
            if (requireLogin == null) {
                requireLogin = handlerMethod.getBeanType().getAnnotation(RequireLogin.class);
            }
            if (requireLogin != null && UserContext.getUserId() == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), requireLogin.value());
            }

            // @RequireAdmin：需要管理员
            RequireAdmin requireAdmin = handlerMethod.getMethodAnnotation(RequireAdmin.class);
            if (requireAdmin == null) {
                requireAdmin = handlerMethod.getBeanType().getAnnotation(RequireAdmin.class);
            }
            if (requireAdmin != null && !"ADMIN".equals(UserContext.getRole())) {
                throw new BusinessException(ResultCode.FORBIDDEN.getCode(), requireAdmin.value());
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
