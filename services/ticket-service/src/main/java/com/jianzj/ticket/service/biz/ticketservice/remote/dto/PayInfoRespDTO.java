package com.jianzj.ticket.service.biz.ticketservice.remote.dto;

/**
 * @Author JianZJ
 * @Date 2025/2/22 9:47
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 支付单详情响应参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class PayInfoRespDTO {

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 支付总金额
     */
    private Integer totalAmount;

    /**
     * 支付状态
     */
    private Integer status;

    /**
     * 支付时间
     */
    private Date gmtPayment;

}
