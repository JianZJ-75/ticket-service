package com.jianzj.ticket.service.biz.ticketservice.remote.dto;

/**
 * @Author JianZJ
 * @Date 2025/2/22 16:19
 */

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 乘车人响应参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class PassengerRespDTO {

    /**
     * 乘车人id
     */
    private String id;

    /**
     * 用户名
     */
    private String username;

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
     * 优惠类型，同用户类型 儿童、成人、学生、残疾军人
     */
    private Integer discountType;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 添加日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createDate;

    /**
     * 审核状态
     */
    private Integer verifyStatus;

}
