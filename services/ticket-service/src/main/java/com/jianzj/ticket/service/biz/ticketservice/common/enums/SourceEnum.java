package com.jianzj.ticket.service.biz.ticketservice.common.enums;

/**
 * @Author JianZJ
 * @Date 2025/2/22 16:27
 */

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 购票来源
 */
@RequiredArgsConstructor
public enum SourceEnum {

    /**
     * 互联网购票
     */
    INTERNET(0),

    /**
     * 线下窗口购票
     */
    OFFLINE(1);

    @Getter
    private final Integer code;
}
