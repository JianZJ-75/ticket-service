package com.jianzj.ticket.service.frameworks.starter.designpattern.strategy;

/**
 * @Author JianZJ
 * @Date 2025/2/13 3:22
 */

/**
 * 抽象执行策略
 * @param <REQUEST>
 * @param <RESPONSE>
 */
public interface AbstractExecuteStrategy<REQUEST, RESPONSE> {

    /**
     * 执行策略标识
     * @return
     */
    default String mark() {
        return null;
    }

    /**
     * 执行策略范匹配标识
     * @return
     */
    default String patternMatchMark() {
        return null;
    }

    /**
     * 执行策略
     * @param request
     */
    default void execute(REQUEST request) {

    }

    /**
     * 执行策略 有返回
     * @param request
     * @return
     */
    default RESPONSE executeResp(REQUEST request) {
        return null;
    }

}