package com.jianzj.ticket.service.frameworks.starter.distributedid.core.serviceid;

import com.jianzj.ticket.service.frameworks.starter.distributedid.core.IdGenerator;
import com.jianzj.ticket.service.frameworks.starter.distributedid.core.snowflake.SnowflakeIdInfo;

/**
 * @Author JianZJ
 * @Date 2025/2/17 3:12
 */

/**
 * 业务id生成器接口
 */
public interface ServiceIdGenerator extends IdGenerator {

    /**
     * 根据 {@param serviceId} 生成雪花算法 ID
     */
    default long nextId(long serviceId) {
        return 0L;
    }

    /**
     * 根据 {@param serviceId} 生成雪花算法 ID
     */
    default long nextId(String serviceId) {
        return 0L;
    }

    /**
     * 根据 {@param serviceId} 生成字符串类型雪花算法 ID
     */
    default String nextIdStr(long serviceId) {
        return null;
    }

    /**
     * 根据 {@param serviceId} 生成字符串类型雪花算法 ID
     */
    default String nextIdStr(String serviceId) {
        return null;
    }

    /**
     * 解析雪花算法
     */
    SnowflakeIdInfo parseSnowflakeId(long snowflakeId);
}