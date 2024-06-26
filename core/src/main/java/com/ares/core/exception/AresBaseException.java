package com.ares.core.exception;


public class AresBaseException extends RuntimeException {

    public AresBaseException(int errorCode) {
        super("");
        this.errorCode = errorCode;
    }


    public AresBaseException(int errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }

    public AresBaseException(int errorCode, String msg, Throwable throwable) {
        super(msg, throwable);
        this.errorCode = errorCode;
    }

    public AresBaseException(int errorCode, String msg, Object data) {
        this(errorCode, msg);
        this.data = data;
    }

    @Override
    public String toString() {
        return "----- status = " + errorCode + ", message = " + this.getMessage();
    }

    private int errorCode;
    private Object data;

    public int getErrorCode() {
        return errorCode;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

}
