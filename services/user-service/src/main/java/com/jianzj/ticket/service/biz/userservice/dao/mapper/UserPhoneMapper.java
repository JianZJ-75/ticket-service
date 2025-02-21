package com.jianzj.ticket.service.biz.userservice.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianzj.ticket.service.biz.userservice.dao.entity.UserPhoneDO;

/**
 * @Author JianZJ
 * @Date 2025/2/21 11:03
 */

/**
 * 用户手机号表持久层
 */
public interface UserPhoneMapper extends BaseMapper<UserPhoneDO> {

    /**
     * 注销用户
     *
     * @param userPhoneDO 注销用户入参
     */
    void deletionUser(UserPhoneDO userPhoneDO);

}
