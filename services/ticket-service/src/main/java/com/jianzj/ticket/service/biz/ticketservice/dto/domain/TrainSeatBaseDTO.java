package com.jianzj.ticket.service.biz.ticketservice.dto.domain;

/**
 * @Author JianZJ
 * @Date 2025/2/22 20:16
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 高铁座位基础信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class TrainSeatBaseDTO {

    /**
     * 高铁列车 ID
     */
    private String trainId;

    /**
     * 列车起始站点
     */
    private String departure;

    /**
     * 列车到达站点
     */
    private String arrival;

    /**
     * 乘客信息
     */
    private List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails;

    /**
     * 选择座位信息
     */
    private List<String> chooseSeatList;
}
