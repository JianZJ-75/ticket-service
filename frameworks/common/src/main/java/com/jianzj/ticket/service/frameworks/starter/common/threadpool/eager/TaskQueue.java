package com.jianzj.ticket.service.frameworks.starter.common.threadpool.eager;

import lombok.Setter;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @Author JianZJ
 * @Date 2025/2/14 1:31
 */

/**
 * 快速消费阻塞队列
 * @param <R>
 */
public class TaskQueue<R extends Runnable> extends LinkedBlockingQueue<Runnable> {

    @Setter
    private EagerThreadPoolExecutor executor;

    public TaskQueue(int capacity) {
        super(capacity);
    }

    @Override
    public boolean offer(Runnable runnable) {
        // 获取线程池的线程数
        int currentPoolThreadSize = executor.getPoolSize();
        // 如果有线程处于空闲 将任务加入阻塞队列
        if (executor.getSubmittedTaskCount() < currentPoolThreadSize) {
            super.offer(runnable);
        }
        // 如果线程数小于最大线程数 创建非核心线程 (源码是阻塞队列满才创非核心)
        if (currentPoolThreadSize < executor.getMaximumPoolSize()) {
            return false;
        }
        // 非核心线程也满了 加阻塞队列
        return super.offer(runnable);
    }

    public boolean retryOffer(Runnable runnable, long timeout, TimeUnit unit) throws InterruptedException {
        // 在超时等待过程中, 线程池可能关闭, 需要进行判断
        if (executor.isShutdown()) {
            throw new RejectedExecutionException("Executor is shutdown!");
        }
        return super.offer(runnable, timeout, unit);
    }

}