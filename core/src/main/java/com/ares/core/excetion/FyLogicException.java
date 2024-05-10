package com.ares.core.excetion;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FyLogicException extends RuntimeException {
    private int errCode;
    private String message;

    public FyLogicException(int errCode) {
        super("err_code:" + errCode);
        this.errCode = errCode;
    }

    public FyLogicException(int errCode, String message) {
        super(message);
        this.errCode = errCode;
        this.message = message;
    }

    public FyLogicException(int errCode, String message, Throwable cause) {
        super(message, cause);
        this.errCode = errCode;
        this.message = message;
    }

    @Override
    public String toString() {
        return "LogicException:[code]=" + errCode + "[msg]=" + message;
    }
}
