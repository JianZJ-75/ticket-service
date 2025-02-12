package com.jianzj.ticket.service.frameworks.starter.convention.exception;

import com.jianzj.ticket.service.frameworks.starter.convention.errorcode.BaseErrorCode;
import com.jianzj.ticket.service.frameworks.starter.convention.errorcode.IErrorCode;

/**
 * @Author JianZJ
 * @Date 2025/2/12 23:38
 */

/**
 * 服务端异常
 */
public class ServiceException extends AbstractException {

    public ServiceException(IErrorCode errorCode) {
        this(null, null, errorCode);
    }

    public ServiceException(String message) {
        this(message, null, BaseErrorCode.CLIENT_ERROR);
    }

    public ServiceException(String message, IErrorCode errorCode) {
        this(message, null, errorCode);
    }

    public ServiceException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }

    @Override
    public String toString() {
        return "ServiceException{" +
                "code='" + errorCode + "'," +
                "message='" + errorMessage + "'" +
                '}';
    }

}