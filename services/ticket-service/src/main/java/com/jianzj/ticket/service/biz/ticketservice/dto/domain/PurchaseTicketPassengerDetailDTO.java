package com.jianzj.ticket.service.biz.ticketservice.dto.domain;

/**
 * @Author JianZJ
 * @Date 2025/2/22 13:47
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 购票乘车人详情实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class PurchaseTicketPassengerDetailDTO {

    /**
     * 乘车人 ID
     */
    private String passengerId;

    /**
     * 座位类型
     */
    private Integer seatType;

}
