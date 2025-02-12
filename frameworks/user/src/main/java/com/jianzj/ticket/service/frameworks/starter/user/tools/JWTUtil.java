package com.jianzj.ticket.service.frameworks.starter.user.tools;

/**
 * @Author JianZJ
 * @Date 2025/2/13 0:56
 */

import com.alibaba.fastjson2.JSON;
import com.jianzj.ticket.service.frameworks.starter.base.constant.UserConstant;
import com.jianzj.ticket.service.frameworks.starter.user.core.UserInfoDTO;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.jianzj.ticket.service.frameworks.starter.base.constant.UserConstant.*;

/**
 * JWT工具类
 */
@Slf4j
public final class JWTUtil {

    /**
     * 过期时间
     */
    private static final long EXPIRATION = 24 * 60 * 60L;
    /**
     * 请求头前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    /**
     * 标签
     */
    public static final String ISS = "ticket-service";
    /**
     * 密钥
     */
    public static final String SECRET = "SecretKey039245678901232039487623456783092349288901402967890140939827";

    /**
     * 生成JWT
     * @param userInfoDTO
     * @return
     */
    public static String generateAccessToken(UserInfoDTO userInfoDTO) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(UserConstant.USER_ID_KEY, userInfoDTO.getUserId());
        claims.put(UserConstant.USER_NAME_KEY, userInfoDTO.getUsername());
        claims.put(UserConstant.REAL_NAME_KEY, userInfoDTO.getRealName());
        String token = Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .setIssuedAt(new Date())
                .setIssuer(ISS)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION * 1000))
                .setSubject(JSON.toJSONString(claims))
                .compact();
        return TOKEN_PREFIX + token;
    }

    /**
     * 解析token
     * @param token
     * @return
     */
    public static UserInfoDTO parseToken(String token) {
        if (StringUtils.hasText(token)) {
            String actualToken = token.replace(TOKEN_PREFIX, "");
            try {
                Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(actualToken).getBody();
                Date expiration = claims.getExpiration();
                // 判断是否过期
                if (expiration.after(new Date())) {
                    String subject = claims.getSubject();
                    return JSON.parseObject(subject, UserInfoDTO.class);
                }
            } catch (ExpiredJwtException ignored) {
            } catch (Exception ex) {
                log.error("token解析失败...", ex);
            }
        }
        return null;
    }

}