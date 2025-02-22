package com.jianzj.ticket.service.biz.ticketservice.remote.dto;

/**
 * @Author JianZJ
 * @Date 2025/2/22 16:25
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 车票订单详情创建请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class TicketOrderItemCreateRemoteReqDTO {

    /**
     * 车厢号
     */
    private String carriageNumber;

    /**
     * 座位类型
     */
    private Integer seatType;

    /**
     * 座位号
     */
    private String seatNumber;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 证件类型
     */
    private Integer idType;

    /**
     * 证件号
     */
    private String idCard;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 订单金额
     */
    private Integer amount;

    /**
     * 车票类型
     */
    private Integer ticketType;
}