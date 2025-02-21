package com.jianzj.ticket.service.biz.ticketservice.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @Author JianZJ
 * @Date 2025/2/21 21:10
 */

/**
 * 座位状态枚举
 */
@RequiredArgsConstructor
public enum SeatStatusEnum {

    /**
     * 可售
     */
    AVAILABLE(0),

    /**
     * 锁定
     */
    LOCKED(1),

    /**
     * 已售
     */
    SOLD(2);

    @Getter
    private final Integer code;

}
