package com.ares.core.thread;

import com.ares.core.bean.AresMsgIdMethod;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.thread.task.EventBiFunction;
import com.ares.core.thread.task.EventCommBiFunction;
import com.ares.core.thread.task.EventFunction;
import com.ares.core.thread.task.EventThFunction;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;

@Slf4j
public abstract class AresThreadPool {
    private final int threadCount;
    private final IMessageExecutor[] iMessageExecutors;

    public AresThreadPool(ThreadFactory threadFactory, int threadCount) {
        iMessageExecutors = new IMessageExecutor[threadCount];
        for (int i = 0; i < threadCount; ++i) {
            iMessageExecutors[i] = new DisruptorSingleExecutor(threadFactory);
            iMessageExecutors[i].start();
        }
        this.threadCount = threadCount;
    }

    public void execute(long hashCode, AresTKcpContext aresTKcpContext, AresMsgIdMethod method, long p1, Object p2) {
        try {
            IMessageExecutor iMessageExecutor = getChannelIMessageExecutor(hashCode);
            iMessageExecutor.execute(aresTKcpContext, method, p1, p2);
        } catch (Exception e) {
            log.error("---error-- ", e);
        }
    }

    public void execute(AresTKcpContext aresTKcpContext, AresMsgIdMethod method, long p1, Object p2) {
        try {
            IMessageExecutor iMessageExecutor = getChannelIMessageExecutor(0);
            iMessageExecutor.execute(aresTKcpContext, method, p1, p2);
        } catch (Exception e) {
            log.error("---error-- ", e);
        }
    }


    public <T1, T2, T3> void execute(long hashCode, T1 p1, T2 p2, T3 p3, EventThFunction<T1, T2, T3> method) {
        try {
            IMessageExecutor iMessageExecutor = getChannelIMessageExecutor(hashCode);
            iMessageExecutor.execute(p1, p2, p3, method);
        } catch (Exception e) {
            log.error("---error-- ", e);
        }
    }

    public <T> void execute(long hashCode, long p1, T p2, EventBiFunction<T> method) {
        try {
            IMessageExecutor iMessageExecutor = getChannelIMessageExecutor(hashCode);
            iMessageExecutor.execute(p1, p2, method);
        } catch (Exception e) {
            log.error("---error-- ", e);
        }
    }

    public <T> void execute(long hashCode, T p, EventFunction<T> method) {
        try {
            IMessageExecutor iMessageExecutor = getChannelIMessageExecutor(hashCode);
            iMessageExecutor.execute(p, method);
        } catch (Exception e) {
            log.error("---error-- ", e);
        }
    }


    public <T> void execute(long hashCode, EventBiFunction<T> method, long p1, T p2) {
        try {
            IMessageExecutor iMessageExecutor = getChannelIMessageExecutor(hashCode);
            iMessageExecutor.execute(p1, p2, method);
        } catch (Exception e) {
            log.error("---error-- ", e);
        }
    }


    public <T1, T2> void execute(long hashCode, EventCommBiFunction<T1, T2> method, T1 p1, T2 p2) {
        try {
            IMessageExecutor iMessageExecutor = getChannelIMessageExecutor(hashCode);
            iMessageExecutor.execute(p1, p2, method);
        } catch (Exception e) {
            log.error("---error-- ", e);
        }
    }


    public void shutDown() {
        if (iMessageExecutors == null) {
            return;
        }
        for (IMessageExecutor iMessageExecutor : iMessageExecutors) {
            iMessageExecutor.stop();
        }
    }

    private IMessageExecutor getChannelIMessageExecutor(long hashCode) {
        return iMessageExecutors[(int) (hashCode % threadCount)];
    }
}
