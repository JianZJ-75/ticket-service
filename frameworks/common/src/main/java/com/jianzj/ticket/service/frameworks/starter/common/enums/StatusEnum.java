package com.jianzj.ticket.service.frameworks.starter.common.enums;

/**
 * @Author JianZJ
 * @Date 2025/2/14 0:55
 */

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * 状态枚举
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum StatusEnum {

    /**
     * 成功
     */
    SUCCESS(0),

    /**
     * 失败
     */
    FAIL(1);

    private final Integer statusCode;

    public Integer code() {
        return statusCode;
    }

    public String strCode() {
        return String.valueOf(statusCode);
    }

    @Override
    public String toString() {
        return strCode();
    }
}