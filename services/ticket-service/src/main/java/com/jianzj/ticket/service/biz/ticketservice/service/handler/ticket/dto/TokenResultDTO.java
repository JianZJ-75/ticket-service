package com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.dto;

/**
 * @Author JianZJ
 * @Date 2025/2/23 4:38
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 令牌扣减返回参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResultDTO {

    /**
     * Token 为空
     */
    private Boolean tokenIsNull;

    /**
     * 获取 Token 为空站点座位类型和数量
     */
    private List<String> tokenIsNullSeatTypeCounts;
}