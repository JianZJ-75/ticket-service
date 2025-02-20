package com.jianzj.ticket.service.biz.gatewayservice.filter;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.jianzj.ticket.service.biz.gatewayservice.config.FilterConfig;
import com.jianzj.ticket.service.frameworks.starter.base.constant.UserConstant;
import com.jianzj.ticket.service.frameworks.starter.user.core.UserInfoDTO;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import com.jianzj.ticket.service.frameworks.starter.user.tools.JWTUtil;
import org.springframework.stereotype.Component;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * @Author JianZJ
 * @Date 2025/2/20 19:58
 */

/**
 * 鉴权过滤器工厂
 */
@Component
public class TokenValidateGatewayFilterFactory extends AbstractGatewayFilterFactory<FilterConfig> {

    public TokenValidateGatewayFilterFactory() {
        super(FilterConfig.class);
    }

    /**
     * 注销用户时需要传递 Token
     */
    public static final String DELETION_PATH = "/api/user-service/deletion";

    @Override
    public GatewayFilter apply(FilterConfig config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            // 获取请求路径
            String requestPath = request.getPath().toString();
            // 判断是否在黑名单前缀列表中
            if (isPathInBlackPreList(requestPath, config.getBlackPathPre())) {
                // 获取令牌
                String token = request.getHeaders().getFirst("Authorization");
                // 令牌验证
                UserInfoDTO userInfo = JWTUtil.parseToken(token);
                // 令牌无效响应401
                if (!validateToken(userInfo)) {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }
                // 如果令牌有效 对请求头修改并添加用户信息
                ServerHttpRequest.Builder builder = exchange.getRequest().mutate().headers(httpHeaders -> {
                    httpHeaders.set(UserConstant.USER_ID_KEY, userInfo.getUserId());
                    httpHeaders.set(UserConstant.USER_NAME_KEY, userInfo.getUsername());
                    httpHeaders.set(UserConstant.REAL_NAME_KEY, URLEncoder.encode(userInfo.getRealName(), StandardCharsets.UTF_8));
                    // 如果是注销 还需添加用户令牌信息
                    if (Objects.equals(requestPath, DELETION_PATH)) {
                        httpHeaders.set(UserConstant.USER_TOKEN_KEY, token);
                    }
                });
                return chain.filter(exchange.mutate().request(builder.build()).build());
            }
            return chain.filter(exchange);
        };
    }

    private boolean isPathInBlackPreList(String requestPath, List<String> blackPathPre) {
        if (CollectionUtils.isEmpty(blackPathPre)) {
            return false;
        }
        return blackPathPre.stream().anyMatch(requestPath::startsWith);
    }

    private boolean validateToken(UserInfoDTO userInfo) {
        return userInfo != null;
    }
}
