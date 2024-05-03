package com.ares.core.utils;


import com.ares.core.tcp.AresTKcpContext;
import io.netty.util.concurrent.FastThreadLocal;

public class AresContextThreadLocal {
    private static final FastThreadLocal<AresTKcpContext> aresTKcpContextThreadLocal = new FastThreadLocal<>();

    public static void cache(AresTKcpContext aresTKcpContext) {
        aresTKcpContextThreadLocal.set(aresTKcpContext);
    }


    public static AresTKcpContext get() {
        return aresTKcpContextThreadLocal.get();
    }

    public static void clear() {
        aresTKcpContextThreadLocal.remove();
    }
}
