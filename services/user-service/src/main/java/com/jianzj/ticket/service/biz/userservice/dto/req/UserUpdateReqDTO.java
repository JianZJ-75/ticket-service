package com.jianzj.ticket.service.biz.userservice.dto.req;

/**
 * @Author JianZJ
 * @Date 2025/2/21 10:09
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户修改请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateReqDTO {

    /**
     * 用户ID
     */
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 旅客类型
     */
    private Integer userType;

    /**
     * 邮编
     */
    private String postCode;

    /**
     * 地址
     */
    private String address;

}
