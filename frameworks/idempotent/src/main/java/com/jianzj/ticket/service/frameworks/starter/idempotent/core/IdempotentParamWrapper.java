package com.jianzj.ticket.service.frameworks.starter.idempotent.core;

import com.jianzj.ticket.service.frameworks.starter.idempotent.annotation.Idempotent;
import com.jianzj.ticket.service.frameworks.starter.idempotent.enums.IdempotentTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @Author JianZJ
 * @Date 2025/2/19 16:24
 */

/**
 * 幂等参数包装器
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public final class IdempotentParamWrapper {

    /**
     * 幂等注解
     */
    private Idempotent idempotent;

    /**
     * AOP 处理连接点
     */
    private ProceedingJoinPoint joinPoint;

    /**
     * 锁标识，{@link IdempotentTypeEnum#PARAM}
     */
    private String lockKey;
}
