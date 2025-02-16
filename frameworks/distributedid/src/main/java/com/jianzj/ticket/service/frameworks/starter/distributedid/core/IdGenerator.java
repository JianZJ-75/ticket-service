package com.jianzj.ticket.service.frameworks.starter.distributedid.core;

/**
 * @Author JianZJ
 * @Date 2025/2/16 3:59
 */

/**
 * id生成器
 */
public interface IdGenerator {

    /**
     * 下一个id
     * @return
     */
    default long nextId() {
        return 0L;
    }

    /**
     * 下一个字符串id
     * @return
     */
    default String nextIdStr() {
        return "";
    }

}