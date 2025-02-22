package com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.dto;

import com.jianzj.ticket.service.biz.ticketservice.dto.domain.PurchaseTicketPassengerDetailDTO;
import com.jianzj.ticket.service.biz.ticketservice.dto.req.PurchaseTicketReqDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Author JianZJ
 * @Date 2025/2/22 16:21
 */

/**
 * 选择座位实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class SelectSeatDTO {

    /**
     * 座位类型
     */
    private Integer seatType;

    /**
     * 座位对应的乘车人集合
     */
    private List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails;

    /**
     * 购票原始入参
     */
    private PurchaseTicketReqDTO requestParam;

}
