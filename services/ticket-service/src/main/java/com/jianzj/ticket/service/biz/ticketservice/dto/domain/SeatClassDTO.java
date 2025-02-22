package com.jianzj.ticket.service.biz.ticketservice.dto.domain;

/**
 * @Author JianZJ
 * @Date 2025/2/21 21:58
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 座位类型实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class SeatClassDTO {

    /**
     * 座位类型
     */
    private Integer type;

    /**
     * 座位数量
     */
    private Integer quantity;

    /**
     * 座位价格
     */
    private BigDecimal price;

    /**
     * 座位候补标识
     */
    private Boolean candidate;

}
