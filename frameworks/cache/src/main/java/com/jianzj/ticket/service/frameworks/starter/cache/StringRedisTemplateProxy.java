package com.jianzj.ticket.service.frameworks.starter.cache;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.jianzj.ticket.service.frameworks.starter.base.SinglePool;
import com.jianzj.ticket.service.frameworks.starter.cache.config.RedisDistributedProperties;
import com.jianzj.ticket.service.frameworks.starter.cache.core.CacheGetFilter;
import com.jianzj.ticket.service.frameworks.starter.cache.core.CacheGetIfAbsent;
import com.jianzj.ticket.service.frameworks.starter.cache.core.CacheLoader;
import com.jianzj.ticket.service.frameworks.starter.cache.tool.CacheUtil;
import com.jianzj.ticket.service.frameworks.starter.cache.tool.FastJson2Util;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @Author JianZJ
 * @Date 2025/2/14 3:31
 */

/**
 * 分布式缓存之操作 Redis 模版代理
 */
@RequiredArgsConstructor
public class StringRedisTemplateProxy implements DistributedCache {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisDistributedProperties redisProperties;
    private final RedissonClient redissonClient;

    private static final String LUA_PUT_IF_ALL_ABSENT_SCRIPT_PATH = "lua/putIfAllAbsent.lua";
    private static final String SAFE_GET_DISTRIBUTED_LOCK_KEY_PREFIX = "safe_get_distributed_lock_get:";

    @Override
    public <T> T get(String key, Class<T> clazz) {
        String value = stringRedisTemplate.opsForValue().get(key);
        // 判断目标类型是不是String 不是则要进行反序列化
        if (String.class.isAssignableFrom(clazz)) {
            return (T) value;
        }
        return JSON.parseObject(value, FastJson2Util.buildType(clazz));
    }

    @Override
    public void put(String key, Object value) {
        put(key, value, redisProperties.getValueTimeout());
    }

    @Override
    public Boolean putIfAllAbsent(@NotNull Collection<String> keys) {
        DefaultRedisScript<Boolean> actual = SinglePool.get(LUA_PUT_IF_ALL_ABSENT_SCRIPT_PATH, () -> {
            DefaultRedisScript redisScript = new DefaultRedisScript();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(LUA_PUT_IF_ALL_ABSENT_SCRIPT_PATH)));
            redisScript.setResultType(Boolean.class);
            return redisScript;
        });
        Boolean result = stringRedisTemplate.execute(actual, Lists.newArrayList(keys), redisProperties.getValueTimeout().toString());
        return result != null && result;
    }

    @Override
    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    @Override
    public Long delete(Collection<String> keys) {
        return stringRedisTemplate.delete(keys);
    }

    @Override
    public <T> T get(@NotBlank String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout) {
        return get(key, clazz, cacheLoader, timeout, redisProperties.getValueTimeUnit());
    }

    @Override
    public <T> T get(@NotBlank String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout, TimeUnit timeUnit) {
        T result = get(key, clazz);
        if (!CacheUtil.isNullOrBlank(result)) {
            return result;
        }
        return loadAndSet(key, cacheLoader, timeout, timeUnit, false, null);
    }

    @Override
    public <T> T safeGet(@NotBlank String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout) {
        return safeGet(key, clazz, cacheLoader, timeout, redisProperties.getValueTimeUnit());
    }

    @Override
    public <T> T safeGet(@NotBlank String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout, TimeUnit timeUnit) {
        return safeGet(key, clazz, cacheLoader, timeout, timeUnit, null);
    }

    @Override
    public <T> T safeGet(@NotBlank String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout, RBloomFilter<String> bloomFilter) {
        return safeGet(key, clazz, cacheLoader, timeout, bloomFilter, null, null);
    }

    @Override
    public <T> T safeGet(@NotBlank String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout, TimeUnit timeUnit, RBloomFilter<String> bloomFilter) {
        return safeGet(key, clazz, cacheLoader, timeout, timeUnit, bloomFilter, null, null);
    }

    @Override
    public <T> T safeGet(String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout, RBloomFilter<String> bloomFilter, CacheGetFilter<String> cacheCheckFilter) {
        return safeGet(key, clazz, cacheLoader, timeout, redisProperties.getValueTimeUnit(), bloomFilter, cacheCheckFilter, null);
    }

    @Override
    public <T> T safeGet(String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout, TimeUnit timeUnit, RBloomFilter<String> bloomFilter, CacheGetFilter<String> cacheCheckFilter) {
        return safeGet(key, clazz, cacheLoader, timeout, timeUnit, bloomFilter, cacheCheckFilter, null);
    }

    @Override
    public <T> T safeGet(String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout,
                         RBloomFilter<String> bloomFilter, CacheGetFilter<String> cacheGetFilter, CacheGetIfAbsent<String> cacheGetIfAbsent) {
        return safeGet(key, clazz, cacheLoader, timeout, redisProperties.getValueTimeUnit(), bloomFilter, cacheGetFilter, cacheGetIfAbsent);
    }

    @Override
    public <T> T safeGet(String key, Class<T> clazz, CacheLoader<T> cacheLoader, long timeout, TimeUnit timeUnit,
                         RBloomFilter<String> bloomFilter, CacheGetFilter<String> cacheGetFilter, CacheGetIfAbsent<String> cacheGetIfAbsent) {
        T result = get(key, clazz);
        // 缓存存在或者布隆过滤器中不存在直接返回
        if (!CacheUtil.isNullOrBlank(result)
                || Optional.ofNullable(cacheGetFilter).map(each -> each.filter(key)).orElse(false)
                || Optional.ofNullable(bloomFilter).map(each -> !each.contains(key)).orElse(false)) {
            return result;
        }
        // 缓存不存在 布隆存在 使用分布式锁去加载缓存
        RLock lock = redissonClient.getLock(SAFE_GET_DISTRIBUTED_LOCK_KEY_PREFIX + key);
        lock.lock();
        try {
            // 双重判定锁，减轻获得分布式锁后线程访问数据库压力
            // 避免在等待锁的过程中其他线程已加载缓存
            if (CacheUtil.isNullOrBlank(result = get(key, clazz))) {
                // 如果访问 cacheLoader 加载数据为空，执行后置函数操作
                if (CacheUtil.isNullOrBlank(result = loadAndSet(key, cacheLoader, timeout, timeUnit, true, bloomFilter))) {
                    Optional.ofNullable(cacheGetIfAbsent).ifPresent(each -> each.execute(key));
                }
            }
        } finally {
            lock.unlock();
        }
        return result;
    }

    @Override
    public void put(String key, Object value, long timeout) {
        put(key, value, timeout, redisProperties.getValueTimeUnit());
    }

    @Override
    public void put(String key, Object value, long timeout, TimeUnit timeUnit) {
        String actual = value instanceof String ? (String) value : JSON.toJSONString(value);
        stringRedisTemplate.opsForValue().set(key, actual, timeout, timeUnit);
    }

    @Override
    public void safePut(String key, Object value, long timeout, RBloomFilter<String> bloomFilter) {
        safePut(key, value, timeout, redisProperties.getValueTimeUnit(), bloomFilter);
    }

    @Override
    public void safePut(String key, Object value, long timeout, TimeUnit timeUnit, RBloomFilter<String> bloomFilter) {
        put(key, value, timeout, timeUnit);
        if (bloomFilter != null) {
            bloomFilter.add(key);
        }
    }

    @Override
    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    @Override
    public Object getInstance() {
        return stringRedisTemplate;
    }

    @Override
    public Long countExistingKeys(String... keys) {
        return stringRedisTemplate.countExistingKeys(Lists.newArrayList(keys));
    }

    private <T> T loadAndSet(String key, CacheLoader<T> cacheLoader, long timeout, TimeUnit timeUnit, boolean safeFlag, RBloomFilter<String> bloomFilter) {
        T result = cacheLoader.load();
        if (CacheUtil.isNullOrBlank(result)) {
            return result;
        }
        if (safeFlag) {
            safePut(key, result, timeout, timeUnit, bloomFilter);
        } else {
            put(key, result, timeout, timeUnit);
        }
        return result;
    }
}