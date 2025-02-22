package com.jianzj.ticket.service.biz.ticketservice.remote;

import com.jianzj.ticket.service.biz.ticketservice.remote.dto.PassengerRespDTO;
import com.jianzj.ticket.service.frameworks.starter.convention.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Author JianZJ
 * @Date 2025/2/22 16:18
 */

/**
 * 用户远程服务调用
 */
@FeignClient(value = "ticket-service-user${unique-name:}-service", url = "http://127.0.0.1:${server.port}")
public interface UserRemoteService {

    /**
     * 根据乘车人 ID 集合查询乘车人列表
     */
    @GetMapping("/api/user-service/inner/passenger/actual/query/ids")
    Result<List<PassengerRespDTO>> listPassengerQueryByIds(@RequestParam("username") String username, @RequestParam("ids") List<String> ids);

}
