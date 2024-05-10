package com.ares.nk2.coroutine.exception;

public class CoroCheckedException extends Exception {
    public CoroCheckedException(Throwable cause) {
        super("unknown", cause);
    }

    public CoroCheckedException(String s) {
        super(s, null);
    }

    public CoroCheckedException(String s, Throwable cause) {
        super(s, cause);
    }
}
