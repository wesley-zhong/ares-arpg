package com.ares.core.thread;

import com.ares.core.bean.AresMsgIdMethod;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.thread.task.EventBiFunction;
import com.ares.core.thread.task.EventFunction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogicProcessThreadPool {
    private int processThreadCount;
    private IMessageExecutor[] iMessageExecutors;

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    private volatile long threadId;

    public static LogicProcessThreadPool create(int logicAysnThreadCount) {
        return new LogicProcessThreadPool(logicAysnThreadCount);
    }


    public LogicProcessThreadPool(int logicAysnThreadCount) {
        processThreadCount = logicAysnThreadCount;
        iMessageExecutors = new IMessageExecutor[processThreadCount];
        AresThreadFactory aresThreadFactory = new AresThreadFactory("a-l-t-P-");
        for (int i = 0; i < logicAysnThreadCount; ++i) {
            iMessageExecutors[i] = new DisruptorSingleExecutor(aresThreadFactory);
            iMessageExecutors[i].start();
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

    public void execute(long hashCode, AresTKcpContext aresTKcpContext, AresMsgIdMethod method, long p1, Object p2) {
        try {
            IMessageExecutor iMessageExecutor = getChannelIMessageExecutor(hashCode);
            iMessageExecutor.execute(aresTKcpContext, method, p1, p2);
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

    public <T> void execute(long hashCode, T p, EventFunction<T> method) {
        try {
            IMessageExecutor iMessageExecutor = getChannelIMessageExecutor(hashCode);
            iMessageExecutor.execute(p, method);
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
//        return iMessageExecutors[(processThreadCount - 1) & hash(aresPacket.getCtx().channel())];
        //only for one thread
        return iMessageExecutors[(int) (hashCode % processThreadCount)];
    }

    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
}
