package com.jianzj.ticket.service.frameworks.starter.common.enums;

/**
 * @Author JianZJ
 * @Date 2025/2/14 0:55
 */

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * 标识枚举
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FlagEnum {

    /**
     * true
     */
    TRUE(1),

    /**
     * false
     */
    FALSE(0);

    private final Integer flag;

    public Integer code() {
        return flag;
    }

    public String strCode() {
        return String.valueOf(flag);
    }

    @Override
    public String toString() {
        return strCode();
    }

}