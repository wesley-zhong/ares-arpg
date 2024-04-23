package com.ares.team.utils;

import java.util.concurrent.atomic.AtomicLong;

public class IdGenUtil {
    private static AtomicLong atomicLong = new AtomicLong(System.currentTimeMillis());

    public static long genId() {
        return atomicLong.incrementAndGet();
    }
}
