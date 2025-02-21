package com.jianzj.ticket.service.biz.userservice.dto.req;

/**
 * @Author JianZJ
 * @Date 2025/2/20 22:06
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginReqDTO {

    /**
     * 用户名
     */
    private String usernameOrMailOrPhone;

    /**
     * 密码
     */
    private String password;

}
