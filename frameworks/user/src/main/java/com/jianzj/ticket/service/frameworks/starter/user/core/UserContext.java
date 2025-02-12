package com.jianzj.ticket.service.frameworks.starter.user.core;

/**
 * @Author JianZJ
 * @Date 2025/2/13 1:21
 */

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Optional;

/**
 * 用户上下文
 */
public final class UserContext {

    private static final ThreadLocal<UserInfoDTO> USER_THREAD_LOCAL = new TransmittableThreadLocal<>();

    /**
     * 设置用户信息
     * @param user
     */
    public static void setUser(UserInfoDTO user) {
        USER_THREAD_LOCAL.set(user);
    }

    /**
     * 获取用户id
     * @return
     */
    public static String getUserId() {
        UserInfoDTO user = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(user).map(UserInfoDTO::getUserId).orElse(null);
    }

    /**
     * 获取用户名
     * @return
     */
    public static String getUsername() {
        UserInfoDTO user = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(user).map(UserInfoDTO::getUsername).orElse(null);
    }

    /**
     * 获取用户真实姓名
     * @return
     */
    public static String getRealName() {
        UserInfoDTO user = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(user).map(UserInfoDTO::getRealName).orElse(null);
    }

    /**
     * 获取用户token
     * @return
     */
    public static String getToken() {
        UserInfoDTO user = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(user).map(UserInfoDTO::getToken).orElse(null);
    }

    /**
     * 清理用户上下文
     */
    public static void removeUser() {
        USER_THREAD_LOCAL.remove();
    }

}