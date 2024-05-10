package com.ares.nk2.coroutine.exception;


public class CoroExecutionException extends RuntimeException {
    public CoroExecutionException(Throwable cause) {
        this("unknown", cause);
    }

    public CoroExecutionException(String s) {
        this(s, null);
    }

    public CoroExecutionException(String s, Throwable cause) {
        super(s, cause);
    }

}
