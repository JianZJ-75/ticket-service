package com.jianzj.ticket.service.biz.ticketservice.remote;

/**
 * @Author JianZJ
 * @Date 2025/2/22 9:46
 */

import com.jianzj.ticket.service.biz.ticketservice.remote.dto.PayInfoRespDTO;
import com.jianzj.ticket.service.biz.ticketservice.remote.dto.RefundReqDTO;
import com.jianzj.ticket.service.biz.ticketservice.remote.dto.RefundRespDTO;
import com.jianzj.ticket.service.frameworks.starter.convention.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 支付远程服务调用
 */
@FeignClient(value = "ticket-service-pay${unique-name:}-service", url = "http://127.0.0.1:${server.port}")
public interface PayRemoteService {

    /**
     * 支付单详情查询
     */
    @GetMapping("/api/pay-service/pay/query")
    Result<PayInfoRespDTO> getPayInfo(@RequestParam(value = "orderSn") String orderSn);

    /**
     * 公共退款接口
     */
    @PostMapping("/api/pay-service/common/refund")
    Result<RefundRespDTO> commonRefund(@RequestBody RefundReqDTO requestParam);

}
