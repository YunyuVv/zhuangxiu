package com.biliww.zhuangxiu.security;

import com.biliww.zhuangxiu.common.BusinessException;
import com.biliww.zhuangxiu.common.ErrorCode;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 获取当前登录用户 id（从 SecurityContext 中读取 JWT 解析出的 userId）。
 */
public class SecurityUtil {

    public static Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }
}
