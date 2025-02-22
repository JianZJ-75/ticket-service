package com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.filter.purchase;

/**
 * @Author JianZJ
 * @Date 2025/2/22 15:01
 */

import com.jianzj.ticket.service.biz.ticketservice.dto.req.PurchaseTicketReqDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 购票流程过滤器之验证乘客是否重复购买
 */
@Component
@RequiredArgsConstructor
public class TrainPurchaseTicketRepeatChainHandler implements TrainPurchaseTicketChainFilter<PurchaseTicketReqDTO> {

    @Override
    public void handler(PurchaseTicketReqDTO requestParam) {
        // TODO 重复购买验证后续实现
    }

    @Override
    public int getOrder() {
        return 30;
    }

}