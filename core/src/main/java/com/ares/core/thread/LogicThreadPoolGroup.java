package com.ares.core.thread;

import lombok.extern.slf4j.Slf4j;

// 1 epoll event thread 1  ServerRpcProcessThreadPoolGroup
@Slf4j
public class LogicThreadPoolGroup {
    public static LogicThreadPoolGroup INSTANCE;
    private AresThreadPool[] aresThreadPools;
    private AresThreadPool[] aresVirtualThreadPools;

    public LogicThreadPoolGroup(int threadPoolTypeCount) {
        aresThreadPools = new LogicProcessThreadPool[threadPoolTypeCount];
        INSTANCE = this;
    }

    public LogicThreadPoolGroup(int threadTypeCount, int virtualThreadCount) {
        aresThreadPools = new LogicProcessThreadPool[threadTypeCount];
        aresVirtualThreadPools = new VirtualThreadPool[virtualThreadCount];
        INSTANCE = this;
    }

    public void createThreadPool(int threadPoolType, int logicAysnThreadCount) {
        aresThreadPools[threadPoolType] = LogicProcessThreadPool.create(logicAysnThreadCount);
    }

    public void createVirtualThreadPool(int threadPoolType, int threadCount) {
        aresVirtualThreadPools[threadPoolType] = VirtualThreadPool.create(threadCount);
    }

    public AresThreadPool selectThreadPool(int threadPoolType) {
        return aresThreadPools[threadPoolType];
    }

    public AresThreadPool selectVirtualThreadPool(int threadPoolType) {
        return aresVirtualThreadPools[threadPoolType];
    }


    public void shutDown() {
        if (aresThreadPools != null) {
            for (AresThreadPool aresThreadPool : aresThreadPools) {
                if (aresThreadPool != null) {
                    aresThreadPool.shutDown();
                }
            }
            aresThreadPools = null;
        }
        if (aresVirtualThreadPools != null) {
            for (AresThreadPool aresThreadPool : aresVirtualThreadPools) {
                if (aresThreadPool != null) {
                    aresThreadPool.shutDown();
                }
            }
            aresVirtualThreadPools = null;
        }
    }
}
