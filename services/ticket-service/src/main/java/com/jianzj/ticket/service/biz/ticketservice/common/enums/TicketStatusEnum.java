package com.jianzj.ticket.service.biz.ticketservice.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @Author JianZJ
 * @Date 2025/2/22 16:26
 */

/**
 * 车票状态枚举
 */
@RequiredArgsConstructor
public enum TicketStatusEnum {

    /**
     * 未支付
     */
    UNPAID(0),

    /**
     * 已支付
     */
    PAID(1),

    /**
     * 已进站
     */
    BOARDED(2),

    /**
     * 改签
     */
    CHANGED(3),

    /**
     * 退票
     */
    REFUNDED(4),

    /**
     * 已取消
     */
    CLOSED(5);

    @Getter
    private final Integer code;
}
