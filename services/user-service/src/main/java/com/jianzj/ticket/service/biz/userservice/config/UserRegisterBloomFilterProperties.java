package com.jianzj.ticket.service.biz.userservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author JianZJ
 * @Date 2025/2/21 16:34
 */

/**
 * 用户注册布隆过滤器属性配置
 */
@Data
@ConfigurationProperties(prefix = UserRegisterBloomFilterProperties.PREFIX)
public final class UserRegisterBloomFilterProperties {

    public static final String PREFIX = "framework.cache.redis.bloom-filter.user-register";

    /**
     * 用户注册布隆过滤器实例名称
     */
    private String name = "user_register_cache_penetration_bloom_filter";

    /**
     * 每个元素的预期插入量
     */
    private Long expectedInsertions = 64L;

    /**
     * 误判率
     */
    private Double falseProbability = 0.03D;

}
