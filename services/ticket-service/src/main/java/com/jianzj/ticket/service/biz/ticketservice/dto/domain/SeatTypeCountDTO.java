package com.jianzj.ticket.service.biz.ticketservice.dto.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author JianZJ
 * @Date 2025/2/21 21:16
 */

/**
 * 座位类型&对应座位数量实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class SeatTypeCountDTO {

    /**
     * 座位类型
     */
    private Integer seatType;

    /**
     * 座位类型 - 对应数量
     */
    private Integer seatCount;

}
