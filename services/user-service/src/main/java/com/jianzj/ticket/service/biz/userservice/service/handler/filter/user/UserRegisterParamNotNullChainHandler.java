package com.jianzj.ticket.service.biz.userservice.service.handler.filter.user;

import com.jianzj.ticket.service.biz.userservice.common.enums.UserRegisterErrorCodeEnum;
import com.jianzj.ticket.service.biz.userservice.dto.req.UserRegisterReqDTO;
import com.jianzj.ticket.service.frameworks.starter.convention.exception.ClientException;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Author JianZJ
 * @Date 2025/2/21 17:03
 */

/**
 * 用户注册参数必填检验
 */
@Component
public final class UserRegisterParamNotNullChainHandler implements UserRegisterChainFilter<UserRegisterReqDTO> {

    @Override
    public void handler(UserRegisterReqDTO requestParam) {
        if (Objects.isNull(requestParam.getUsername())) {
            throw new ClientException(UserRegisterErrorCodeEnum.USER_NAME_NOTNULL);
        } else if (Objects.isNull(requestParam.getPassword())) {
            throw new ClientException(UserRegisterErrorCodeEnum.PASSWORD_NOTNULL);
        } else if (Objects.isNull(requestParam.getPhone())) {
            throw new ClientException(UserRegisterErrorCodeEnum.PHONE_NOTNULL);
        } else if (Objects.isNull(requestParam.getIdType())) {
            throw new ClientException(UserRegisterErrorCodeEnum.ID_TYPE_NOTNULL);
        } else if (Objects.isNull(requestParam.getIdCard())) {
            throw new ClientException(UserRegisterErrorCodeEnum.ID_CARD_NOTNULL);
        } else if (Objects.isNull(requestParam.getMail())) {
            throw new ClientException(UserRegisterErrorCodeEnum.MAIL_NOTNULL);
        } else if (Objects.isNull(requestParam.getRealName())) {
            throw new ClientException(UserRegisterErrorCodeEnum.REAL_NAME_NOTNULL);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
