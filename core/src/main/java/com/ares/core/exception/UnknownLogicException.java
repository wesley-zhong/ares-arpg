package com.ares.core.exception;

import com.game.protoGen.ProtoErrorCode;

public class UnknownLogicException extends FyLogicException {
    public UnknownLogicException(String message) {
        super(ProtoErrorCode.ErrCode.UNKNOWN_VALUE, message);
    }

    public UnknownLogicException(String message, Throwable cause) {
        super(ProtoErrorCode.ErrCode.UNKNOWN_VALUE, message, cause);
    }
}
