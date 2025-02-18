package com.jianzj.ticket.service.frameworks.starter.log.core;

import lombok.Data;

/**
 * @Author JianZJ
 * @Date 2025/2/18 20:17
 */

/**
 * 日志打印实体
 */
@Data
public class ILogPrintDTO {

    /**
     * 开始时间
     */
    private String beginTime;

    /**
     * 入参
     */
    private Object[] inputParams;

    /**
     * 返回结果
     */
    private Object outputParams;

}
