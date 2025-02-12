package com.jianzj.ticket.service.frameworks.starter.user.config;

/**
 * @Author JianZJ
 * @Date 2025/2/13 1:44
 */

import com.jianzj.ticket.service.frameworks.starter.base.constant.FilterOrderConstant;
import com.jianzj.ticket.service.frameworks.starter.user.core.UserTransmitFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * user组件自动装配
 */
public class ApplicationUserAutoConfiguration {

    /**
     * 用户信息传递过滤器
     */
    @Bean
    public FilterRegistrationBean<UserTransmitFilter> globalUserTransmitFilter() {
        FilterRegistrationBean<UserTransmitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new UserTransmitFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(FilterOrderConstant.USER_TRANSMIT_FILTER_ORDER);
        return registration;
    }

}