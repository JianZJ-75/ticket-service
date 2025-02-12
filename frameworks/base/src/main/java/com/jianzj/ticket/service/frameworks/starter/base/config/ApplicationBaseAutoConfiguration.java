package com.jianzj.ticket.service.frameworks.starter.base.config;

import com.jianzj.ticket.service.frameworks.starter.base.ApplicationContextHolder;
import com.jianzj.ticket.service.frameworks.starter.base.init.ApplicationContextPostProcessor;
import com.jianzj.ticket.service.frameworks.starter.base.safe.FastJsonSafeMode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * @Author JianZJ
 * @Date 2025/2/12 3:00
 */

/**
 * base组件自动装配
 */
public class ApplicationBaseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "fastjson2.parser.safeMode", havingValue = "true")
    public FastJsonSafeMode fastJsonSafeMode() {
        return new FastJsonSafeMode();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationContextPostProcessor applicationContextPostProcessor(ApplicationContext applicationContext) {
        return new ApplicationContextPostProcessor(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationContextHolder applicationContextHolder() {
        return new ApplicationContextHolder();
    }

}