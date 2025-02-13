package com.jianzj.ticket.service.frameworks.starter.common.enums;

/**
 * @Author JianZJ
 * @Date 2025/2/14 0:54
 */

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * 删除状态枚举
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DelEnum {

    /**
     * 正常状态
     */
    NORMAL(0),

    /**
     * 删除状态
     */
    DELETE(1);

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