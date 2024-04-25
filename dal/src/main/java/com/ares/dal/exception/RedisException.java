package com.ares.dal.exception;


import com.ares.core.constdata.FConst;
import com.ares.core.exception.AresBaseException;

public class RedisException extends AresBaseException {
    public RedisException(String msg) {
        super(FConst.ERROR_CODE_REDIS_CLUSTER, msg);
    }
}
