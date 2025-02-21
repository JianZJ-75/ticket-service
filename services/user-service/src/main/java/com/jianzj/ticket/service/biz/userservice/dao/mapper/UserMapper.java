package com.jianzj.ticket.service.biz.userservice.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jianzj.ticket.service.biz.userservice.dao.entity.UserDO;

/**
 * @Author JianZJ
 * @Date 2025/2/21 11:12
 */

/**
 * 用户信息表持久层
 */
public interface UserMapper extends BaseMapper<UserDO> {

    /**
     * 注销用户
     *
     * @param userDO 注销用户入参
     */
    void deletionUser(UserDO userDO);

}
