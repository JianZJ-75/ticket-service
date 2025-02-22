package com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.filter.query;

/**
 * @Author JianZJ
 * @Date 2025/2/22 10:08
 */

import com.jianzj.ticket.service.biz.ticketservice.common.enums.TicketChainMarkEnum;
import com.jianzj.ticket.service.biz.ticketservice.dto.req.TicketPageQueryReqDTO;
import com.jianzj.ticket.service.frameworks.starter.designpattern.chain.AbstractChainHandler;

/**
 * 车票查询过滤器
 */
public interface TrainTicketQueryChainFilter<T extends TicketPageQueryReqDTO> extends AbstractChainHandler<TicketPageQueryReqDTO> {

    @Override
    default String mark() {
        return TicketChainMarkEnum.TRAIN_QUERY_FILTER.name();
    }

}
