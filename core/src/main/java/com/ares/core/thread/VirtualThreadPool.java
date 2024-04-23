package com.ares.core.thread;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VirtualThreadPool extends AresThreadPool {
    private IMessageExecutor[] iMessageExecutors;

    public static VirtualThreadPool create(int logicAsynThreadCount) {
        return new VirtualThreadPool(logicAsynThreadCount);
    }

    public VirtualThreadPool(int logicAysnThreadCount) {
        super(Thread.ofVirtual().name("customer-thread_%d").factory(), logicAysnThreadCount);
    }

}
