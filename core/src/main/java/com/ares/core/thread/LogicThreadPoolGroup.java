package com.ares.core.thread;

import lombok.extern.slf4j.Slf4j;

// 1 epoll event thread 1  ServerRpcProcessThreadPoolGroup
@Slf4j
public class LogicThreadPoolGroup {
    public static LogicThreadPoolGroup INSTANCE;
    private LogicProcessThreadPool[] serverRpcProcessThreadPools;

    public LogicThreadPoolGroup(int threadPoolTypeCount) {
        serverRpcProcessThreadPools = new LogicProcessThreadPool[threadPoolTypeCount];
        INSTANCE = this;
    }

    public void createThreadPool(int threadPoolType, int logicAysnThreadCount) {
        serverRpcProcessThreadPools[threadPoolType] = LogicProcessThreadPool.create(logicAysnThreadCount);
    }

    public LogicProcessThreadPool selectThreadPool(int threadPoolType) {
        return serverRpcProcessThreadPools[threadPoolType];
    }


    public void shutDown() {
        for (LogicProcessThreadPool serverRpcProcessThreadPool : serverRpcProcessThreadPools) {
            if (serverRpcProcessThreadPool != null) {
                serverRpcProcessThreadPool.shutDown();
            }
        }
    }
}
