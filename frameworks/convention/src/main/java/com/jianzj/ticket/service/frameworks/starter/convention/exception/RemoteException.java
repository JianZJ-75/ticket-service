package com.jianzj.ticket.service.frameworks.starter.convention.exception;

/**
 * @Author JianZJ
 * @Date 2025/2/12 23:47
 */

import com.jianzj.ticket.service.frameworks.starter.convention.errorcode.BaseErrorCode;
import com.jianzj.ticket.service.frameworks.starter.convention.errorcode.IErrorCode;

/**
 * 第三方调用异常
 */
public class RemoteException extends AbstractException {

    public RemoteException(IErrorCode errorCode) {
        this(null, null, errorCode);
    }

    public RemoteException(String message) {
        this(message, null, BaseErrorCode.CLIENT_ERROR);
    }

    public RemoteException(String message, IErrorCode errorCode) {
        this(message, null, errorCode);
    }

    public RemoteException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }

    @Override
    public String toString() {
        return "RemoteException{" +
                "code='" + errorCode + "'," +
                "message='" + errorMessage + "'" +
                '}';
    }

}