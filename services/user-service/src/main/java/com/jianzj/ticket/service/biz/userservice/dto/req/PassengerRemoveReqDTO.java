package com.jianzj.ticket.service.biz.userservice.dto.req;

/**
 * @Author JianZJ
 * @Date 2025/2/21 17:42
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 乘车人移除请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassengerRemoveReqDTO {

    /**
     * 乘车人id
     */
    private String id;

}
