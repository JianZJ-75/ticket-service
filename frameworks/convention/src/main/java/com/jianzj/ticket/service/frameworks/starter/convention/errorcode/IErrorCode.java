package com.jianzj.ticket.service.frameworks.starter.convention.errorcode;

/**
 * @Author JianZJ
 * @Date 2025/2/12 22:25
 */

/**
 * 错误码抽象
 */
public interface IErrorCode {

    /**
     * 错误码
     * @return
     */
    String code();

    /**
     * 错误信息
     * @return
     */
    String message();

}