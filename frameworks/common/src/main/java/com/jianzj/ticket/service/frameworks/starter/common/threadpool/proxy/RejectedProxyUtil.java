package com.jianzj.ticket.service.frameworks.starter.common.threadpool.proxy;

/**
 * @Author JianZJ
 * @Date 2025/2/14 2:30
 */

import com.jianzj.ticket.service.frameworks.starter.common.tools.ThreadUtil;

import java.lang.reflect.Proxy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 拒绝策略代理工具类
 */
public final class RejectedProxyUtil {

    /**
     * 创建策略代理对象
     * @param rejectedExecutionHandler 真正的执行器
     * @param rejectCount              拒绝统计数
     * @return 代理执行器
     */
    public static RejectedExecutionHandler createProxy(RejectedExecutionHandler rejectedExecutionHandler, AtomicLong rejectCount) {
        return (RejectedExecutionHandler) Proxy.newProxyInstance(
                RejectedExecutionHandler.class.getClassLoader(),
                new Class[]{RejectedExecutionHandler.class},
                new RejectedProxyInvocationHandler(rejectedExecutionHandler, rejectCount)
        );
    }

    /**
     * 测试线程池拒绝策略动态代理程序
     */
    public static void main(String[] args) {
        ThreadPoolExecutor threadPoolExecutor =
                new ThreadPoolExecutor(1, 3, 1024, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1));
        ThreadPoolExecutor.AbortPolicy abortPolicy = new ThreadPoolExecutor.AbortPolicy();
        AtomicLong rejectedNum = new AtomicLong();
        RejectedExecutionHandler proxyRejectedExecutionHandler = RejectedProxyUtil.createProxy(abortPolicy, rejectedNum);
        threadPoolExecutor.setRejectedExecutionHandler(proxyRejectedExecutionHandler);
        for (int i = 0; i < 5; i++) {
            try {
                threadPoolExecutor.execute(() -> ThreadUtil.sleep(100000L));
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        System.out.println("================ 线程池拒绝策略执行次数: " + rejectedNum.get());
    }

}