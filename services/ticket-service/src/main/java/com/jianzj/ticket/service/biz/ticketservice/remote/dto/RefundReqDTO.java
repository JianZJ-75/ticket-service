package com.jianzj.ticket.service.biz.ticketservice.remote.dto;

/**
 * @Author JianZJ
 * @Date 2025/2/22 9:49
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 退款请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class RefundReqDTO {

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 退款类型枚举
     */
    private RefundTypeEnum refundTypeEnum;

    /**
     * 退款金额
     */
    private Integer refundAmount;

    /**
     * 部分退款车票详情集合
     */
    private List<TicketOrderPassengerDetailRespDTO> refundDetailReqDTOList;

}
