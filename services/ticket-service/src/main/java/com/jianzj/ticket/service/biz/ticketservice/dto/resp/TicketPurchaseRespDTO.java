package com.jianzj.ticket.service.biz.ticketservice.dto.resp;

/**
 * @Author JianZJ
 * @Date 2025/2/22 13:44
 */

import com.jianzj.ticket.service.biz.ticketservice.remote.dto.TicketOrderDetailRespDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 车票购买响应参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class TicketPurchaseRespDTO {

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 订单详情
     */
    private List<TicketOrderDetailRespDTO> ticketOrderDetails;

}
