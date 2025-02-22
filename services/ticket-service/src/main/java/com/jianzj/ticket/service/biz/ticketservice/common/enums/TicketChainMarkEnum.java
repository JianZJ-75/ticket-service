package com.jianzj.ticket.service.biz.ticketservice.common.enums;

/**
 * @Author JianZJ
 * @Date 2025/2/22 10:00
 */

/**
 * 购票相关责任链 Mark 枚举
 */
public enum TicketChainMarkEnum {

    /**
     * 车票查询过滤器
     */
    TRAIN_QUERY_FILTER,

    /**
     * 车票购买过滤器
     */
    TRAIN_PURCHASE_TICKET_FILTER,

    /**
     * 车票退款过滤器
     */
    TRAIN_REFUND_TICKET_FILTER

}
