package com.jianzj.ticket.service.frameworks.starter.cache.core;/**
 * @Author JianZJ
 * @Date 2025/2/14 3:32
 */

/**
 * 缓存查询为空
 * @param <T>
 */
@FunctionalInterface
public interface CacheGetIfAbsent<T> {

    /**
     * 如果查询结果为空，执行逻辑
     */
    void execute(T param);
}
