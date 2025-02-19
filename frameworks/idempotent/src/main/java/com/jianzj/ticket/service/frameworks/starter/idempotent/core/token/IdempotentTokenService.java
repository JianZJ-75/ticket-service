package com.jianzj.ticket.service.frameworks.starter.idempotent.core.token;

import com.jianzj.ticket.service.frameworks.starter.idempotent.core.IdempotentExecuteHandler;

/**
 * @Author JianZJ
 * @Date 2025/2/19 16:18
 */

/**
 * token方式幂等实现接口
 */
public interface IdempotentTokenService extends IdempotentExecuteHandler {

    /**
     * 创建幂等验证Token
     */
    String createToken();
}
