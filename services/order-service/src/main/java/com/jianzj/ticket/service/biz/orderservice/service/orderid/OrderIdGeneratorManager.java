package com.jianzj.ticket.service.biz.orderservice.service.orderid;

/**
 * @Author JianZJ
 * @Date 2025/2/23 0:34
 */

import com.jianzj.ticket.service.frameworks.starter.cache.DistributedCache;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 订单 ID 全局唯一生成器管理
 */
@Component
@RequiredArgsConstructor
public final class OrderIdGeneratorManager implements InitializingBean {

    private final RedissonClient redissonClient;
    private final DistributedCache distributedCache;
    private static DistributedIdGenerator DISTRIBUTED_ID_GENERATOR;

    /**
     * 生成订单全局唯一 ID
     * @param userId 用户名
     * @return 订单 ID
     */
    public static String generateId(long userId) {
        return DISTRIBUTED_ID_GENERATOR.generateId() + String.valueOf(userId % 1000000);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 定义 Redis 分布式锁的键名
        String LOCK_KEY = "distributed_id_generator_lock_key";
        RLock lock = redissonClient.getLock(LOCK_KEY);
        lock.lock();
        try {
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            // 定义存储分布式 ID 生成器配置的 Redis 键名
            String DISTRIBUTED_ID_GENERATOR_KEY = "distributed_id_generator_config";
            // 对 Redis 中指定键的值进行自增操作，并使用 Optional 处理可能的空值情况
            // 如果键不存在，初始值为 0，自增后返回 1
            // 类似于雪花算法的centerId wordId
            long incremented = Optional.ofNullable(instance.opsForValue().increment(DISTRIBUTED_ID_GENERATOR_KEY)).orElse(0L);
            // 注意：这里只是提供一种分库分表基因法的实现思路，所以将标识位定义 32。其次，如果对比 TB 网站订单号，应该不是在应用内生成，而是有一个全局服务调用获取
            int NODE_MAX = 32;
            if (incremented > NODE_MAX) {
                incremented = 0;
                instance.opsForValue().set(DISTRIBUTED_ID_GENERATOR_KEY, "0");
            }
            DISTRIBUTED_ID_GENERATOR = new DistributedIdGenerator(incremented);
        } finally {
            lock.unlock();
        }
    }
}