package com.jianzj.ticket.service.frameworks.starter.cache.core;/**
 * @Author JianZJ
 * @Date 2025/2/14 3:32
 */

/**
 * 缓存过滤
 * @param <T>
 */
@FunctionalInterface
public interface CacheGetFilter<T> {

    /**
     * 缓存过滤
     *
     * @param param 输出参数
     * @return {@code true} 如果输入参数匹配，否则 {@link Boolean#TRUE}
     */
    boolean filter(T param);
}