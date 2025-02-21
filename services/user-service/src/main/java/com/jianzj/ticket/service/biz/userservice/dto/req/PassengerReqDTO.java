package com.jianzj.ticket.service.biz.userservice.dto.req;

/**
 * @Author JianZJ
 * @Date 2025/2/21 17:41
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 乘车人添加&修改请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassengerReqDTO {

    /**
     * 乘车人id
     */
    private String id;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 证件类型
     */
    private Integer idType;

    /**
     * 证件号码
     */
    private String idCard;

    /**
     * 优惠类型
     */
    private Integer discountType;

    /**
     * 手机号
     */
    private String phone;

}
