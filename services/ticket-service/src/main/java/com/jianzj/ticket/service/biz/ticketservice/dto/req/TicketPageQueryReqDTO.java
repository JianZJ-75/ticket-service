package com.jianzj.ticket.service.biz.ticketservice.dto.req;

/**
 * @Author JianZJ
 * @Date 2025/2/22 9:52
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 车票分页查询请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketPageQueryReqDTO {

    /**
     * 出发地 Code
     */
    private String fromStation;

    /**
     * 目的地 Code
     */
    private String toStation;

    /**
     * 出发日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date departureDate;

    /**
     * 出发站点
     */
    private String departure;

    /**
     * 到达站点
     */
    private String arrival;

}
