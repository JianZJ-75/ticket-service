package com.jianzj.ticket.service.frameworks.starter.base;

/**
 * @Author JianZJ
 * @Date 2025/2/12 3:43
 */

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 单例对象池
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SinglePool {

    private static final ConcurrentHashMap<String, Object> SINGLE_OBJECT_POOL = new ConcurrentHashMap<String, Object>();

    /**
     * 根据key获取单例对象
     * @param key
     * @return
     * @param <T>
     */
    public static <T> T get(String key) {
        Object result = SINGLE_OBJECT_POOL.get(key);
        return result == null ? null : (T) result;
    }

    /**
     * 根据key获取单例对象 若不存在则用Supplier生成
     * @param key
     * @param supplier
     * @return
     * @param <T>
     */
    public static <T> T get(String key, Supplier<T> supplier) {
        Object result = SINGLE_OBJECT_POOL.get(key);
        if (result == null && (result = supplier.get()) != null) {
            SINGLE_OBJECT_POOL.put(key, result);
        }
        return result == null ? null : (T) result;
    }

    /**
     * 存对象
     * @param key
     * @param value
     */
    public static void put(String key, Object value) {
        SINGLE_OBJECT_POOL.put(key, value);
    }

    /**
     * 存对象
     * @param value
     */
    public static void put(Object value) {
        put(value.getClass().getName(), value);
    }

}