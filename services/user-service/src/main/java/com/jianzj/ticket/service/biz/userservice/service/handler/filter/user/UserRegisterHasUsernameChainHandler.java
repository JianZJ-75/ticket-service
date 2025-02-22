package com.jianzj.ticket.service.biz.userservice.service.handler.filter.user;

import com.jianzj.ticket.service.biz.userservice.common.enums.UserRegisterErrorCodeEnum;
import com.jianzj.ticket.service.biz.userservice.dto.req.UserRegisterReqDTO;
import com.jianzj.ticket.service.biz.userservice.service.UserLoginService;
import com.jianzj.ticket.service.frameworks.starter.convention.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @Author JianZJ
 * @Date 2025/2/21 17:03
 */

/**
 * 用户注册用户名唯一检验
 */
@Component
@RequiredArgsConstructor
public final class UserRegisterHasUsernameChainHandler implements UserRegisterChainFilter<UserRegisterReqDTO> {

    private final UserLoginService userLoginService;

    @Override
    public void handler(UserRegisterReqDTO requestParam) {
        if (!userLoginService.hasUsername(requestParam.getUsername())) {
            throw new ClientException(UserRegisterErrorCodeEnum.HAS_USERNAME_NOTNULL);
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
