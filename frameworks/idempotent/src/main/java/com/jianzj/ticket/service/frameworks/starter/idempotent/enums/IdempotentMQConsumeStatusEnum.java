package com.jianzj.ticket.service.frameworks.starter.idempotent.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * @Author JianZJ
 * @Date 2025/2/19 15:42
 */

/**
 * MQ消费状态枚举类
 */
@RequiredArgsConstructor
public enum IdempotentMQConsumeStatusEnum {

    CONSUMING("0"),

    CONSUMED("1");

    @Getter
    private final String code;

    /**
     * 如果消费状态处于消费中 返回失败
     * @param consumeStatus 消费状态
     * @return 是否消费失败
     */
    public static boolean isError(String consumeStatus) {
        return Objects.equals(CONSUMING.code, consumeStatus);
    }

}
