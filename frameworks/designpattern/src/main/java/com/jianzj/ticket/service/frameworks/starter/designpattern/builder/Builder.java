package com.jianzj.ticket.service.frameworks.starter.designpattern.builder;

import java.io.Serializable;

/**
 * @Author JianZJ
 * @Date 2025/2/13 2:43
 */

/**
 * Builder模式抽象
 * @param <T>
 */
public interface Builder<T> extends Serializable {

    /**
     * 构建方法
     * @return
     */
    T build();

}