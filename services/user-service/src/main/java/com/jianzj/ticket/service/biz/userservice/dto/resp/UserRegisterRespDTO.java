package com.jianzj.ticket.service.biz.userservice.dto.resp;

/**
 * @Author JianZJ
 * @Date 2025/2/21 9:35
 */

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jianzj.ticket.service.biz.userservice.serialize.IdCardDesensitizationSerializer;
import com.jianzj.ticket.service.biz.userservice.serialize.PhoneDesensitizationSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 用户注册相应参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class UserRegisterRespDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 国家/地区
     */
    private String region;

    /**
     * 证件类型
     */
    private Integer idType;

    /**
     * 证件号
     */
    @JsonSerialize(using = IdCardDesensitizationSerializer.class)
    private String idCard;

    /**
     * 手机号
     */
    @JsonSerialize(using = PhoneDesensitizationSerializer.class)
    private String phone;

    /**
     * 固定电话
     */
    private String telephone;

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 旅客类型
     */
    private Integer userType;

    /**
     * 审核状态
     */
    private Integer verifyStatus;

    /**
     * 邮编
     */
    private String postCode;

    /**
     * 地址
     */
    private String address;

}
