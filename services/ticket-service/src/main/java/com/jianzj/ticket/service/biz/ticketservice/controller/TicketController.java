package com.jianzj.ticket.service.biz.ticketservice.controller;

/**
 * @Author JianZJ
 * @Date 2025/2/21 21:50
 */

import com.jianzj.ticket.service.biz.ticketservice.dto.req.PurchaseTicketReqDTO;
import com.jianzj.ticket.service.biz.ticketservice.dto.req.TicketPageQueryReqDTO;
import com.jianzj.ticket.service.biz.ticketservice.dto.resp.TicketPageQueryRespDTO;
import com.jianzj.ticket.service.biz.ticketservice.dto.resp.TicketPurchaseRespDTO;
import com.jianzj.ticket.service.biz.ticketservice.remote.dto.PayInfoRespDTO;
import com.jianzj.ticket.service.biz.ticketservice.service.TicketService;
import com.jianzj.ticket.service.frameworks.starter.convention.result.Result;
import com.jianzj.ticket.service.frameworks.starter.web.Results;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 车票控制层
 */
@RestController
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * 根据条件查询车票
     */
    @GetMapping("/api/ticket-service/ticket/query")
    public Result<TicketPageQueryRespDTO> pageListTicketQuery(TicketPageQueryReqDTO requestParam) {
        return Results.success(ticketService.pageListTicketQueryV1(requestParam));
    }

    /**
     * 购买车票
     */
    @PostMapping("/api/ticket-service/ticket/purchase")
    public Result<TicketPurchaseRespDTO> purchaseTickets(@RequestBody PurchaseTicketReqDTO requestParam) {
        return Results.success(ticketService.purchaseTicketsV1(requestParam));
    }

    /**
     * 购买车票v2
     */
    @PostMapping("/api/ticket-service/ticket/purchase/v2")
    public Result<TicketPurchaseRespDTO> purchaseTicketsV2(@RequestBody PurchaseTicketReqDTO requestParam) {
        return Results.success(ticketService.purchaseTicketsV2(requestParam));
    }

    /**
     * 取消车票订单
     */
    @PostMapping("/api/ticket-service/ticket/cancel")
    public Result<Void> cancelTicketOrder(@RequestBody CancelTicketOrderReqDTO requestParam) {
        ticketService.cancelTicketOrder(requestParam);
        return Results.success();
    }

    /**
     * 支付单详情查询
     */
    @GetMapping("/api/ticket-service/ticket/pay/query")
    public Result<PayInfoRespDTO> getPayInfo(@RequestParam(value = "orderSn") String orderSn) {
        return Results.success(ticketService.getPayInfo(orderSn));
    }

    /**
     * 公共退款接口
     */
    @PostMapping("/api/ticket-service/ticket/refund")
    public Result<RefundTicketRespDTO> commonTicketRefund(@RequestBody RefundTicketReqDTO requestParam) {
        return Results.success(ticketService.commonTicketRefund(requestParam));
    }

}
