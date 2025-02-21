package com.jianzj.ticket.service.biz.userservice.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianzj.ticket.service.biz.userservice.dao.entity.UserMailDO;

/**
 * @Author JianZJ
 * @Date 2025/2/21 10:47
 */

/**
 * 用户邮箱表持久层
 */
public interface UserMailMapper extends BaseMapper<UserMailDO> {

    /**
     * 注销用户
     *
     * @param userMailDO 注销用户入参
     */
    void deletionUser(UserMailDO userMailDO);

}
