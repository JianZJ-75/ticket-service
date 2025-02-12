package com.jianzj.ticket.service.frameworks.starter.base.init;

import org.springframework.context.ApplicationEvent;

/**
 * @Author JianZJ
 * @Date 2025/2/12 3:23
 */

/**
 * 应用初始化事件
 */
public class ApplicationInitializingEvent extends ApplicationEvent {

    public ApplicationInitializingEvent(Object source) {
        super(source);
    }

}