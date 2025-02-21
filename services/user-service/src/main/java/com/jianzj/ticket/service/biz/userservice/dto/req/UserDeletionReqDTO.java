package com.jianzj.ticket.service.biz.userservice.dto.req;

/**
 * @Author JianZJ
 * @Date 2025/2/21 9:52
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注销请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDeletionReqDTO {

    /**
     * 用户名
     */
    private String username;

}
