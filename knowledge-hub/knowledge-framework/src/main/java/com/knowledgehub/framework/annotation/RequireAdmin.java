package com.knowledgehub.framework.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 需要管理员权限
 * <p>标注在 Controller 方法或类上，由 JwtInterceptor 校验当前用户是否为 ADMIN</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAdmin {

    String value() default "需要管理员权限";
}
