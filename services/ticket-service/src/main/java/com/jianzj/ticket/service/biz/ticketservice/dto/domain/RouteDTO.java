package com.jianzj.ticket.service.biz.ticketservice.dto.domain;

/**
 * @Author JianZJ
 * @Date 2025/2/22 12:49
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 站点路线实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class RouteDTO {

    /**
     * 出发站点
     */
    private String startStation;

    /**
     * 目的站点
     */
    private String endStation;

}
