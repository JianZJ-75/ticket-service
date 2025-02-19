package com.jianzj.ticket.service.frameworks.starter.idempotent.core.spel;

import com.jianzj.ticket.service.frameworks.starter.convention.exception.ClientException;
import com.jianzj.ticket.service.frameworks.starter.idempotent.annotation.Idempotent;
import com.jianzj.ticket.service.frameworks.starter.idempotent.core.AbstractIdempotentExecuteHandler;
import com.jianzj.ticket.service.frameworks.starter.idempotent.core.IdempotentAspect;
import com.jianzj.ticket.service.frameworks.starter.idempotent.core.IdempotentContext;
import com.jianzj.ticket.service.frameworks.starter.idempotent.core.IdempotentParamWrapper;
import com.jianzj.ticket.service.frameworks.starter.idempotent.tools.SpELUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * @Author JianZJ
 * @Date 2025/2/19 16:48
 */

/**
 * 分布式锁
 * SpEL幂等执行处理器 适用RestAPI
 */
@RequiredArgsConstructor
public final class IdempotentSpELByRestAPIExecuteHandler extends AbstractIdempotentExecuteHandler implements IdempotentSpELService {

    private final RedissonClient redissonClient;

    /**
     * 幂等上下文存储键
     */
    private final static String LOCK = "lock:spEL:restAPI";

    @SneakyThrows
    @Override
    protected IdempotentParamWrapper buildWrapper(ProceedingJoinPoint joinPoint) {
        Idempotent idempotent = IdempotentAspect.getIdempotent(joinPoint);
        String key = (String) SpELUtil.parseKey(idempotent.key(), ((MethodSignature) joinPoint.getSignature()).getMethod(), joinPoint.getArgs());
        return IdempotentParamWrapper.builder().lockKey(key).joinPoint(joinPoint).build();
    }

    @Override
    public void handler(IdempotentParamWrapper wrapper) {
        String uniqueKey = wrapper.getIdempotent().uniqueKeyPrefix() + wrapper.getLockKey();
        RLock lock = redissonClient.getLock(uniqueKey);
        if (!lock.tryLock()) {
            throw new ClientException(wrapper.getIdempotent().message());
        }
        IdempotentContext.put(LOCK, lock);
    }

    @Override
    public void postProcessing() {
        RLock lock = null;
        try {
            lock = (RLock) IdempotentContext.getKey(LOCK);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    @Override
    public void exceptionProcessing() {
        postProcessing();
    }
}
