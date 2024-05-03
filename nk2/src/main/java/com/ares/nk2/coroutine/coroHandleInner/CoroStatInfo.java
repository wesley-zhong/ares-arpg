package com.ares.nk2.coroutine.coroHandleInner;

import com.ares.nk2.coroutine.CoroHandle;
import com.ares.nk2.tool.DateUtils;
import com.ares.nk2.tool.NKStringFormater;

public class CoroStatInfo {
    private final CoroHandle coroHandle;

    private final long createTime = DateUtils.currentTimeMillis();
    public long totalCostNs = 0;
    public long cpuCostNs = 0;
    public long startRunTime = 0;
    public int parkCount = 0;
    public long enQueueTime = 0;

    public CoroStatInfo(CoroHandle coroHandle) {
        this.coroHandle = coroHandle;
    }

    public long getTotalCostNs() {
        return totalCostNs;
    }

    public long getCpuCostNs() {
        return cpuCostNs;
    }

    public long getTotalCostMs() {
        return nsToMs(getTotalCostNs());
    }

    public long getCpuCostMs() {
        return nsToMs(getCpuCostNs());
    }

    static long initTimeNs = System.nanoTime();
    static final long thresholdMs = 5 * 60 * 1000;//5min
    private static final long msToNs = 1000_000;
    private static final long usToNs = 1000;
    private static final long ignoreNs = 200 * msToNs;
    private static final long warnNs = 1000 * msToNs;

    public void addTotalCostNs(long ns) {
        totalCostNs += ns;
    }

    public void addCpuCostNs(long ns) {
        cpuCostNs += ns;
        if (ns < ignoreNs) {
            return;
        }
        if (System.nanoTime() - initTimeNs < thresholdMs * msToNs) {
            return;
        }
        if ("frameworkDoCmd".equals(coroHandle.coroBaseInfo.jobName)) {
            return;
        }
        String jobName = coroHandle.getContainer().getContainerName() + "::" + coroHandle.coroBaseInfo.jobName;
        if (ns > warnNs) {
            CoroHandle.LOGGER.error(NKStringFormater.format("cpucost>1000! {} unpark running for {}ms", jobName, nsToMs(ns)));
        } else {
            CoroHandle.LOGGER.warn(NKStringFormater.format("{} unpark running for {}ms", jobName, nsToMs(ns)));
        }
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setEnQueueTime() {
        enQueueTime = DateUtils.currentTimeMillis();
    }

    public int getParkCount() {
        return parkCount;
    }

    public long getEnQueueTime() {
        return enQueueTime;
    }

    public long getStartRunTime() {
        return startRunTime;
    }

    public static long nsToMs(long durationNs) {
        return durationNs / CoroStatInfo.msToNs;
    }

    public static long nsToUs(long durationNs) {
        return durationNs / CoroStatInfo.usToNs;
    }
}
