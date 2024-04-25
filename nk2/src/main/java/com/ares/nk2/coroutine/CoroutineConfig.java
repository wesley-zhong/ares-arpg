package com.ares.nk2.coroutine;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class CoroutineConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoroutineConfig.class);

    private static int CoroutinePoolCnt = 0;
    private static int MaxCoroutinePoolCnt = 0;
    static int MaxStartNewJobOneCycleInCoroSchedule = 0;
    public static int MillSecToCheckTimeoutJob = 0;
    public static int MaxRunReadyJobOneCycleInCoroSchedule = 0;
    static int MaxRunTimeoutJobOneCycleInCoroSchedule = 0;
    public static int DefaultQueueConcurrentRunningJob = 0;
    public static int DefaultQueueJobsOneCycle = 0;
    static int ExecutorServiceHeapCapacity = 0;
    static CoroutineMgr CoroMgr = null;
    public static int SleepWhenCannotProcCnt = 0;
    static int HostProcId = 0;
    static int CoroThreadCount = 1;
    public static int leastWorkerForStoppingJob = 3;
    static int maxMsUntimedCoroutinePark = 60000;

    static void initConfig() {
        CoroutinePoolCnt = 32;
        MaxCoroutinePoolCnt = 99999;
        MaxStartNewJobOneCycleInCoroSchedule = 100;
        MillSecToCheckTimeoutJob = 20;
        MaxRunReadyJobOneCycleInCoroSchedule = 100;
        MaxRunTimeoutJobOneCycleInCoroSchedule = 20;
        DefaultQueueConcurrentRunningJob = MaxCoroutinePoolCnt - 256;
        DefaultQueueJobsOneCycle = 5000;
        ExecutorServiceHeapCapacity = 1000000;
        SleepWhenCannotProcCnt = 1;
        HostProcId = 1;
    }

    static void generateConfig() {
        initConfig();
        initCoroMgr();
    }


    static public void setMaxMsUntimedCoroutinePark(int value) {
        maxMsUntimedCoroutinePark = value;
    }

    static public int getMaxMsUntimedCoroutinePark() {
        return maxMsUntimedCoroutinePark;
    }

    static private void initCoroMgr() {
        CoroMgr = new CoroutineMgr();
        CoroMgr.init();
    }


    static public int getCoroThreadCount() {
        return CoroThreadCount;
    }

    static public int getMaxPoolCnt() {
        return MaxCoroutinePoolCnt;
    }

    static public int getPoolCnt() {
        return CoroutinePoolCnt;
    }

    static CoroutineMgr getMgr() {
        return CoroMgr;
    }


}
