package com.ares.nk2.coroutine.coroHandleInner;

import com.ares.nk2.coroutine.CoroHandle;
import com.ares.nk2.coroutine.CoroutineMgr;
import com.ares.nk2.tool.DateUtils;

import java.util.concurrent.atomic.AtomicInteger;

public class CoroBaseInfo<V> {
    private final CoroHandle coroHandle;
    public volatile int status = CoroHandle.CoroJobStatus.INIT;
    public V result = null;

    private volatile long coroHandleId = CoroutineMgr.getInstance().genAsyncId();
    public Throwable callException = null;
    private long coroutineId = 0;
    public long reentrantTraceId = 0;
    public String createThreadName = "";
    private long createReentrantTraceId = 0;
    public String jobName = "";
    static AtomicInteger reentrantSeed = new AtomicInteger(0);

    public CoroBaseInfo(CoroHandle coroHandle) {
        this.coroHandle = coroHandle;
    }

    public void reset() {
        coroHandleId = CoroutineMgr.getInstance().genAsyncId();
        status = CoroHandle.CoroJobStatus.INIT;
        result = null;
        callException = null;
    }

    public void init(String jobName) {
        this.jobName = jobName;
    }

    public String getJobName() {
        return jobName;
    }

    public final long getCreateReentrantTraceId() {
        return createReentrantTraceId;
    }

    public final void setCreateReentrantTraceId(long reentrantTraceId) {
        createReentrantTraceId = reentrantTraceId;
    }

    public final String getCreateThreadName() {
        return createThreadName;
    }

    public long getReentrantTraceId() {
        return reentrantTraceId;
    }

    public final void setReentrantTraceId(long reentrantTraceId) {
        this.reentrantTraceId = reentrantTraceId;
    }

    public final void resetTraceId() {
        reentrantTraceId = ((DateUtils.currentTimeMillis() / 1000 / 60 << 32)) + reentrantSeed.incrementAndGet();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public final void setCoroutineId(long id) {
        coroutineId = id;
    }

    public final long coroutineId() {
        return coroutineId;
    }

    public final long coroHandleId() {
        return coroHandleId;
    }

}
