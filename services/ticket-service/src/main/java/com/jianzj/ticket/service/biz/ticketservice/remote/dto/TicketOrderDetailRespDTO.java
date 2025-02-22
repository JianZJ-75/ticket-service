package com.jianzj.ticket.service.biz.ticketservice.remote.dto;

/**
 * @Author JianZJ
 * @Date 2025/2/22 9:38
 */

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 车票订单详情响应参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class TicketOrderDetailRespDTO {

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 列车 ID
     */
    private Long trainId;

    /**
     * 出发站点
     */
    private String departure;

    /**
     * 到达站点
     */
    private String arrival;

    /**
     * 乘车日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date ridingDate;

    /**
     * 订票日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date orderTime;

    /**
     * 列车车次
     */
    private String trainNumber;

    /**
     * 出发时间
     */
    @JsonFormat(pattern = "HH:mm", timezone = "GMT+8")
    private Date departureTime;

    /**
     * 到达时间
     */
    @JsonFormat(pattern = "HH:mm", timezone = "GMT+8")
    private Date arrivalTime;

    /**
     * 乘车人订单详情
     */
    private List<TicketOrderPassengerDetailRespDTO> passengerDetails;
}
