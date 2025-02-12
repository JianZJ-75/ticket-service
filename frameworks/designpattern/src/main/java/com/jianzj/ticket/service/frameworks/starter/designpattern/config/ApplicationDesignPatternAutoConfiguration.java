package com.jianzj.ticket.service.frameworks.starter.designpattern.config;

/**
 * @Author JianZJ
 * @Date 2025/2/13 2:43
 */

import com.jianzj.ticket.service.frameworks.starter.designpattern.chain.AbstractChainContext;
import com.jianzj.ticket.service.frameworks.starter.designpattern.strategy.AbstractStrategyChoose;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * design pattern组件自动装配
 */
public class ApplicationDesignPatternAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AbstractChainContext abstractChainContext() {
        return new AbstractChainContext();
    }

    @Bean
    @ConditionalOnMissingBean
    public AbstractStrategyChoose abstractStrategyChoose() {
        return new AbstractStrategyChoose();
    }

}