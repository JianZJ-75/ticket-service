package com.jianzj.ticket.service.frameworks.starter.idempotent.enums;

/**
 * @Author JianZJ
 * @Date 2025/2/19 13:19
 */

/**
 * 幂等实现类型枚举
 *      分布式锁 PARAM + SPEL
 *      TOKEN令牌 TOKEN
 *      去重表 SPEL
 */
public enum IdempotentTypeEnum {

    /**
     * 基于token验证
     */
    TOKEN,

    /**
     * 基于方法参数验证
     */
    PARAM,

    /**
     * 基于SpEL表达式验证
     */
    SPEL

}
