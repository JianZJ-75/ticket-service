package com.jianzj.ticket.service.frameworks.starter.user.core;

import com.jianzj.ticket.service.frameworks.starter.base.constant.UserConstant;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLDecoder;

/**
 * @Author JianZJ
 * @Date 2025/2/13 1:30
 */

/**
 * 用户上下文过滤器
 */
public class UserTransmitFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        // TODO 为什么不是通过解析token再处理
        String userId = httpRequest.getHeader(UserConstant.USER_ID_KEY);
        String username = httpRequest.getHeader(UserConstant.USER_NAME_KEY);
        String realName = httpRequest.getHeader(UserConstant.REAL_NAME_KEY);
        String token = httpRequest.getHeader(UserConstant.USER_TOKEN_KEY);
        if (StringUtils.hasText(realName)) {
            realName = URLDecoder.decode(realName, "UTF-8");
        }
        UserInfoDTO userInfoDTO = UserInfoDTO.builder()
                .userId(userId)
                .username(username)
                .realName(realName)
                .token(token)
                .build();
        UserContext.setUser(userInfoDTO);
        try {
            doFilter(httpRequest, response, chain);
        } finally {
            UserContext.removeUser();;
        }
    }
}