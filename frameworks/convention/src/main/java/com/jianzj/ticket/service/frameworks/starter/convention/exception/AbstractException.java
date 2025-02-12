package com.jianzj.ticket.service.frameworks.starter.convention.exception;

/**
 * @Author JianZJ
 * @Date 2025/2/12 22:56
 */

import com.jianzj.ticket.service.frameworks.starter.convention.errorcode.IErrorCode;
import lombok.Getter;

/**
 * 抽象异常
 */
@Getter
public abstract class AbstractException extends RuntimeException {

    public final String errorCode;

    public final String errorMessage;

    public AbstractException(String message, Throwable cause, IErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode.code();
        this.errorMessage = errorCode.message();
    }

}