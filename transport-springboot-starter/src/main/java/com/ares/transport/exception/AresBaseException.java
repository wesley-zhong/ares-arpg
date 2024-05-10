package com.ares.transport.exception;

public class AresBaseException  extends  RuntimeException{
    private int code;
    private String msg;
    public AresBaseException(int code, String msg){
        super(msg);
        this.code = code;
    }
}
