package com.jianzj.ticket.service.frameworks.starter.idempotent.core;

/**
 * @Author JianZJ
 * @Date 2025/2/19 16:04
 */

import com.jianzj.ticket.service.frameworks.starter.base.ApplicationContextHolder;
import com.jianzj.ticket.service.frameworks.starter.idempotent.core.param.IdempotentParamExecuteHandler;
import com.jianzj.ticket.service.frameworks.starter.idempotent.core.spel.IdempotentSpELByMQExecuteHandler;
import com.jianzj.ticket.service.frameworks.starter.idempotent.core.spel.IdempotentSpELByRestAPIExecuteHandler;
import com.jianzj.ticket.service.frameworks.starter.idempotent.core.token.IdempotentTokenExecuteHandler;
import com.jianzj.ticket.service.frameworks.starter.idempotent.enums.IdempotentSceneEnum;
import com.jianzj.ticket.service.frameworks.starter.idempotent.enums.IdempotentTypeEnum;

/**
 * 幂等执行处理器工厂
 */
public final class IdempotentExecuteHandlerFactory {

    /**
     * 获取幂等执行处理器
     * @param scene 指定幂等验证场景类型
     * @param type  指定幂等处理类型
     * @return 幂等执行处理器
     */
    public static IdempotentExecuteHandler getInstance(IdempotentSceneEnum scene, IdempotentTypeEnum type) {
        IdempotentExecuteHandler result = null;
        switch (scene) {
            case RESTAPI -> {
                switch (type) {
                    case PARAM -> result = ApplicationContextHolder.getBean(IdempotentParamExecuteHandler.class);
                    case TOKEN -> result = ApplicationContextHolder.getBean(IdempotentTokenExecuteHandler.class);
                    case SPEL -> result = ApplicationContextHolder.getBean(IdempotentSpELByRestAPIExecuteHandler.class);
                    default -> {
                    }
                }
            }
            case MQ -> result = ApplicationContextHolder.getBean(IdempotentSpELByMQExecuteHandler.class);
            default -> {
            }
        }
        return result;
    }

}
