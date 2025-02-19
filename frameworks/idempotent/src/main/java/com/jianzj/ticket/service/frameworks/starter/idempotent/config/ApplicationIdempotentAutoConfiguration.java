package com.jianzj.ticket.service.frameworks.starter.idempotent.config;

/**
 * @Author JianZJ
 * @Date 2025/2/19 10:29
 */

import com.jianzj.ticket.service.frameworks.starter.cache.DistributedCache;
import com.jianzj.ticket.service.frameworks.starter.idempotent.core.IdempotentAspect;
import com.jianzj.ticket.service.frameworks.starter.idempotent.core.param.IdempotentParamExecuteHandler;
import com.jianzj.ticket.service.frameworks.starter.idempotent.core.spel.IdempotentSpELByMQExecuteHandler;
import com.jianzj.ticket.service.frameworks.starter.idempotent.core.spel.IdempotentSpELByRestAPIExecuteHandler;
import com.jianzj.ticket.service.frameworks.starter.idempotent.core.token.IdempotentTokenController;
import com.jianzj.ticket.service.frameworks.starter.idempotent.core.token.IdempotentTokenExecuteHandler;
import com.jianzj.ticket.service.frameworks.starter.idempotent.core.token.IdempotentTokenService;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * idempotent组件自动装配
 */
@EnableConfigurationProperties(IdempotentProperties.class)
public class ApplicationIdempotentAutoConfiguration {

    /**
     * 幂等切面
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentAspect idempotentAspect() {
        return new IdempotentAspect();
    }

    /**
     * 参数幂等执行 RestAPI
     * @param redissonClient
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentParamExecuteHandler idempotentParamExecuteHandler(RedissonClient redissonClient) {
        return new IdempotentParamExecuteHandler(redissonClient);
    }

    /**
     * SpEL幂等执行 RestAPI
     * @param redissonClient
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentSpELByRestAPIExecuteHandler idempotentSpELByRestAPIExecuteHandler(RedissonClient redissonClient) {
        return new IdempotentSpELByRestAPIExecuteHandler(redissonClient);
    }

    /**
     * SpEL幂等执行 MQ
     * @param distributedCache
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentSpELByMQExecuteHandler idempotentSpELByMQExecuteHandler(DistributedCache distributedCache) {
        return new IdempotentSpELByMQExecuteHandler(distributedCache);
    }

    /**
     * token幂等控制器
     * @param idempotentTokenService
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentTokenController idempotentTokenController(IdempotentTokenService idempotentTokenService) {
        return new IdempotentTokenController(idempotentTokenService);
    }

    /**
     * token幂等执行 RestAPI
     * @param distributedCache
     * @param idempotentProperties
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentTokenExecuteHandler idempotentTokenExecuteHandler(DistributedCache distributedCache, IdempotentProperties idempotentProperties) {
        return new IdempotentTokenExecuteHandler(distributedCache, idempotentProperties);
    }

}
