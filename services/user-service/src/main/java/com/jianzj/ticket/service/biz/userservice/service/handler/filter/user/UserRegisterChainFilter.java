package com.jianzj.ticket.service.biz.userservice.service.handler.filter.user;

/**
 * @Author JianZJ
 * @Date 2025/2/21 16:56
 */

import com.jianzj.ticket.service.biz.userservice.common.enums.UserChainMarkEnum;
import com.jianzj.ticket.service.biz.userservice.dto.req.UserRegisterReqDTO;
import com.jianzj.ticket.service.frameworks.starter.designpattern.chain.AbstractChainHandler;

/**
 * 用户注册责任链过滤器
 */
public interface UserRegisterChainFilter<T extends UserRegisterReqDTO> extends AbstractChainHandler<UserRegisterReqDTO> {

    @Override
    default String mark() {
        return UserChainMarkEnum.USER_REGISTER_FILTER.name();
    }

}
