package com.ares.core.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FyLogicException extends AresBaseException {
    private int errCode;

    public FyLogicException(int errCode) {
        super(errCode);
        this.errCode = errCode;
    }

    public FyLogicException(int errCode, String message) {
        super(errCode, message);
        this.errCode = errCode;
    }

    public FyLogicException(int errCode, String message, Object data) {
        super(errCode, message, data);
        this.errCode = errCode;
    }


    public FyLogicException(int errCode, String message, Throwable cause) {
        super(errCode, message, cause);
        this.errCode = errCode;
    }

    @Override
    public String toString() {
        return "LogicException:[code]=" + errCode + "[msg]=" + this.getMessage();
    }
}
