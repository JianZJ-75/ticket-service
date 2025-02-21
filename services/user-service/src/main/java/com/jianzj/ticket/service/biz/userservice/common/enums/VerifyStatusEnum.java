package com.jianzj.ticket.service.biz.userservice.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author JianZJ
 * @Date 2025/2/21 17:54
 */

/**
 * 乘车人信息状态枚举
 */
@AllArgsConstructor
public enum VerifyStatusEnum {

    /**
     * 未审核
     */
    UNREVIEWED(0),

    /**
     * 已审核
     */
    REVIEWED(1);

    @Getter
    private final int code;

}
