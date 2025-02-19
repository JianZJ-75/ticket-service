package com.jianzj.ticket.service.frameworks.starter.idempotent.core.spel;

import com.jianzj.ticket.service.frameworks.starter.cache.DistributedCache;
import com.jianzj.ticket.service.frameworks.starter.idempotent.annotation.Idempotent;
import com.jianzj.ticket.service.frameworks.starter.idempotent.core.*;
import com.jianzj.ticket.service.frameworks.starter.idempotent.enums.IdempotentMQConsumeStatusEnum;
import com.jianzj.ticket.service.frameworks.starter.idempotent.tools.LogUtil;
import com.jianzj.ticket.service.frameworks.starter.idempotent.tools.SpELUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author JianZJ
 * @Date 2025/2/19 16:45
 */

/**
 * 去重表
 * SpEL幂等执行处理器 适用MQ
 */
@RequiredArgsConstructor
public final class IdempotentSpELByMQExecuteHandler extends AbstractIdempotentExecuteHandler implements IdempotentSpELService {

    /**
     * 过期时间
     */
    private final static int TIMEOUT = 600;

    /**
     * 幂等上下文存储的键
     */
    private final static String WRAPPER = "wrapper:spEL:MQ";

    /**
     * lua脚本路径
     */
    private final static String LUA_SCRIPT_SET_IF_ABSENT_AND_GET_PATH = "lua/set_if_absent_and_get.lua";

    /**
     * 分布式缓存
     */
    private final DistributedCache distributedCache;

    @SneakyThrows
    @Override
    protected IdempotentParamWrapper buildWrapper(ProceedingJoinPoint joinPoint) {
        Idempotent idempotent = IdempotentAspect.getIdempotent(joinPoint);
        // 进行SpEL表达式的解析生成key
        String key = (String) SpELUtil.parseKey(idempotent.key(), ((MethodSignature) joinPoint.getSignature()).getMethod(), joinPoint.getArgs());
        return IdempotentParamWrapper.builder().lockKey(key).joinPoint(joinPoint).build();
    }

    @Override
    public void handler(IdempotentParamWrapper wrapper) {
        // 生成唯一缓存键
        String uniqueKey = wrapper.getIdempotent().uniqueKeyPrefix() + wrapper.getLockKey();
        // 获取key值
        String absentAndGet = this.setIfAbsentAndGet(uniqueKey, IdempotentMQConsumeStatusEnum.CONSUMING.getCode(), TIMEOUT, TimeUnit.SECONDS);
        // 如果非空
        if (Objects.nonNull(absentAndGet)) {
            // 判断是否存在错误
            boolean error = IdempotentMQConsumeStatusEnum.isError(absentAndGet);
            // 错误 延迟消费
            // 反之 已经完成
            LogUtil.getLog(wrapper.getJoinPoint()).warn("[{}] MQ repeated consumption, {}.", uniqueKey, error ? "Wait for the client to delay consumption" : "Status is completed");
            throw new RepeatConsumptionException(error);
        }
        IdempotentContext.put(WRAPPER, wrapper);
    }

    public String setIfAbsentAndGet(String key, String value, long timeout, TimeUnit timeUnit) {
        // 创建redis脚本对象
        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
        // 加载脚本资源
        ClassPathResource resource = new ClassPathResource(LUA_SCRIPT_SET_IF_ABSENT_AND_GET_PATH);
        // 设置脚本源
        redisScript.setScriptSource(new ResourceScriptSource(resource));
        // 设置结果类型
        redisScript.setResultType(String.class);
        // 将超时时间转换为毫秒
        long millis = timeUnit.toMillis(timeout);
        // 执行lua脚本
        return ((StringRedisTemplate) distributedCache.getInstance()).execute(redisScript, List.of(key), value, String.valueOf(millis));
    }

    @Override
    public void exceptionProcessing() {
        IdempotentParamWrapper wrapper = (IdempotentParamWrapper) IdempotentContext.getKey(WRAPPER);
        // 若存在 删除键
        if (wrapper != null) {
            Idempotent idempotent = wrapper.getIdempotent();
            String uniqueKey = idempotent.uniqueKeyPrefix() + wrapper.getLockKey();
            try {
                distributedCache.delete(uniqueKey);
            } catch (Throwable ex) {
                LogUtil.getLog(wrapper.getJoinPoint()).error("[{}] Failed to del MQ anti-heavy token.", uniqueKey);
            }
        }
    }

    @Override
    public void postProcessing() {
        IdempotentParamWrapper wrapper = (IdempotentParamWrapper) IdempotentContext.getKey(WRAPPER);
        if (wrapper != null) {
            Idempotent idempotent = wrapper.getIdempotent();
            String uniqueKey = idempotent.uniqueKeyPrefix() + wrapper.getLockKey();
            try {
                distributedCache.put(uniqueKey, IdempotentMQConsumeStatusEnum.CONSUMED.getCode(), idempotent.keyTimeout(), TimeUnit.SECONDS);
            } catch (Throwable ex) {
                LogUtil.getLog(wrapper.getJoinPoint()).error("[{}] Failed to set MQ anti-heavy token.", uniqueKey);
            }
        }
    }
}
