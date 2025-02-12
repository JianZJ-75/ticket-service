package com.jianzj.ticket.service.frameworks.starter.base.safe;

import org.springframework.beans.factory.InitializingBean;

/**
 * @Author JianZJ
 * @Date 2025/2/12 2:57
 */

/**
 * 开启fastjson的安全模式
 */
public class FastJsonSafeMode implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        System.setProperty("fastjson2.parser.safeMode", "true");
    }
}