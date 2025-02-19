package com.jianzj.ticket.service.frameworks.starter.idempotent.enums;

/**
 * @Author JianZJ
 * @Date 2025/2/19 13:16
 */

/**
 * 验证幂等场景枚举
 */
public enum IdempotentSceneEnum {

    /**
     * 基于RestAPI场景验证
     */
    RESTAPI,

    /**
     * 基础MQ场景验证
     */
    MQ

}
