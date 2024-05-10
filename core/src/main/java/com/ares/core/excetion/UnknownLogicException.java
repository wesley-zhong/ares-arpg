package com.ares.core.excetion;

import com.game.protoGen.ProtoCommon;

public class UnknownLogicException extends FyLogicException {
    public UnknownLogicException(String message) {
        super(ProtoCommon.ErrCode.UNKNOWN_VALUE, message);
    }

    public UnknownLogicException(String message, Throwable cause) {
        super(ProtoCommon.ErrCode.UNKNOWN_VALUE, message, cause);
    }
}
