package com.jianzj.ticket.service.frameworks.starter.database.config;

/**
 * @Author JianZJ
 * @Date 2025/2/16 3:08
 */

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.jianzj.ticket.service.frameworks.starter.database.handler.CustomSnowflakeIdGenerator;
import com.jianzj.ticket.service.frameworks.starter.database.handler.MyMetaObjectHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * database组件自动装配
 */
public class ApplicationDatabaseAutoConfiguration {

    /**
     * 分页插件
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public MyMetaObjectHandler myMetaObjectHandler() {
        return new MyMetaObjectHandler();
    }

    @Bean
    @Primary
    public CustomSnowflakeIdGenerator customSnowflakeIdGenerator() {
        return new CustomSnowflakeIdGenerator();
    }

}