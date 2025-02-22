package com.jianzj.ticket.service.biz.userservice.service.handler.filter.user;

import com.jianzj.ticket.service.biz.userservice.dto.req.UserRegisterReqDTO;
import com.jianzj.ticket.service.biz.userservice.service.UserService;
import com.jianzj.ticket.service.frameworks.starter.convention.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @Author JianZJ
 * @Date 2025/2/21 17:03
 */

/**
 * 用户注册检查证件号是否多次注销
 */
@Component
@RequiredArgsConstructor
public final class UserRegisterCheckDeletionChainHandler implements UserRegisterChainFilter<UserRegisterReqDTO> {

    private final UserService userService;

    @Override
    public void handler(UserRegisterReqDTO requestParam) {
        Integer userDeletionNum = userService.queryUserDeletionNum(requestParam.getIdType(), requestParam.getIdCard());
        if (userDeletionNum >= 5) {
            throw new ClientException("证件号多次注销账号已被加入黑名单");
        }
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
