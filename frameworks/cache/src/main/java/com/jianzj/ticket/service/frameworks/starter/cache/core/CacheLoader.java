package com.jianzj.ticket.service.frameworks.starter.cache.core;/**
 * @Author JianZJ
 * @Date 2025/2/14 3:33
 */

/**
 * 缓存加载器
 * @param <T>
 */
@FunctionalInterface
public interface CacheLoader<T> {

    /**
     * 加载缓存
     */
    T load();
}