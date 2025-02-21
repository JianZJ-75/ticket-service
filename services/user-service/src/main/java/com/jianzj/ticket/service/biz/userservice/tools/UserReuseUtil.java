package com.jianzj.ticket.service.biz.userservice.tools;

/**
 * @Author JianZJ
 * @Date 2025/2/21 16:27
 */

import com.jianzj.ticket.service.biz.userservice.common.constant.UsernameRegisterConstant;

/**
 * 用户名复用工具类
 */
public final class UserReuseUtil {

    /**
     * 计算分片位置
     * @param username
     * @return
     */
    public static int hashShardingIdx(String username) {
        return Math.abs(username.hashCode() % UsernameRegisterConstant.USER_REGISTER_REUSE_SHARDING_COUNT);
    }

}
