package com.jianzj.ticket.service.frameworks.starter.distributedid.config;

/**
 * @Author JianZJ
 * @Date 2025/2/16 3:56
 */

import com.jianzj.ticket.service.frameworks.starter.distributedid.core.snowflake.LocalRedisWorkIdChoose;
import com.jianzj.ticket.service.frameworks.starter.distributedid.core.snowflake.RandomWorkIdChoose;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * distributedid组件自动装配
 */

/**
 * distributedid组件自动装配
 */
public class ApplicationDistributedIdAutoConfiguration {

    /**
     * 本地 Redis 构建雪花 WorkId 选择器
     */
    @Bean
    @ConditionalOnProperty("spring.data.redis.host")
    public LocalRedisWorkIdChoose redisWorkIdChoose() {
        return new LocalRedisWorkIdChoose();
    }

    /**
     * 随机数构建雪花 WorkId 选择器。如果项目未使用 Redis，使用该选择器
     */
    @Bean
    @ConditionalOnMissingBean(LocalRedisWorkIdChoose.class)
    public RandomWorkIdChoose randomWorkIdChoose() {
        return new RandomWorkIdChoose();
    }

}