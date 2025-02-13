package com.jianzj.ticket.service.frameworks.starter.common.tools;

import lombok.SneakyThrows;

/**
 * @Author JianZJ
 * @Date 2025/2/14 2:47
 */

/**
 * 线程工具类
 */
public final class ThreadUtil {

    /**
     * 睡眠当前线程
     * @param millis
     */
    @SneakyThrows(value = InterruptedException.class)
    public static void sleep(long millis) {
        Thread.sleep(millis);
    }

}