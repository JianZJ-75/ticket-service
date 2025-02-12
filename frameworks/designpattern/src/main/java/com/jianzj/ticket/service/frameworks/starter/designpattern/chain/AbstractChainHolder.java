package com.jianzj.ticket.service.frameworks.starter.designpattern.chain;

import org.springframework.core.Ordered;

/**
 * @Author JianZJ
 * @Date 2025/2/13 2:52
 */

/**
 * 抽象业务责任链组件
 * @param <T>
 */
public interface AbstractChainHolder<T> extends Ordered {

    /**
     * 执行责任链逻辑
     * @param requestParam
     */
    void handle(T requestParam);

    /**
     * 责任链标识
     * @return
     */
    String mark();

}