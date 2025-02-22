package com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.filter.purchase;

import com.jianzj.ticket.service.biz.ticketservice.common.enums.TicketChainMarkEnum;
import com.jianzj.ticket.service.biz.ticketservice.dto.req.PurchaseTicketReqDTO;
import com.jianzj.ticket.service.frameworks.starter.designpattern.chain.AbstractChainHandler;

/**
 * @Author JianZJ
 * @Date 2025/2/22 14:45
 */

/**
 * 购票过滤器
 */
public interface TrainPurchaseTicketChainFilter<T extends PurchaseTicketReqDTO> extends AbstractChainHandler<PurchaseTicketReqDTO> {

    @Override
    default String mark() {
        return TicketChainMarkEnum.TRAIN_PURCHASE_TICKET_FILTER.name();
    }

}
