package com.jianzj.ticket.service.frameworks.starter.convention.exception;

import com.jianzj.ticket.service.frameworks.starter.convention.errorcode.BaseErrorCode;
import com.jianzj.ticket.service.frameworks.starter.convention.errorcode.IErrorCode;

/**
 * @Author JianZJ
 * @Date 2025/2/12 23:10
 */

/**
 * 客户端异常
 */
public class ClientException extends AbstractException {

    public ClientException(IErrorCode errorCode) {
        this(null, null, errorCode);
    }

    public ClientException(String message) {
        this(message, null, BaseErrorCode.CLIENT_ERROR);
    }

    public ClientException(String message, IErrorCode errorCode) {
        this(message, null, errorCode);
    }

    public ClientException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }

    @Override
    public String toString() {
        return "ClientException{" +
                "code='" + errorCode + "'," +
                "message='" + errorMessage + "'" +
                '}';
    }

}