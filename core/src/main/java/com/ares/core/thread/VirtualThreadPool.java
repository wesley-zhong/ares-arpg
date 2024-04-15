package com.ares.core.thread;

import com.ares.core.bean.AresMsgIdMethod;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.thread.task.EventBiFunction;
import com.ares.core.thread.task.EventFunction;
import com.ares.core.thread.task.EventThFunction;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;

@Slf4j
public class VirtualThreadPool {

    private int processThreadCount;
    private IMessageExecutor[] iMessageExecutors;

    private volatile long threadId;

    // public static VirtualThreadPool INSTANCE = new VirtualThreadPool(1);


    public VirtualThreadPool(int logicAysnThreadCount) {
        processThreadCount = logicAysnThreadCount;
        iMessageExecutors = new IMessageExecutor[processThreadCount];
        //  AresThreadFactory aresThreadFactory = new AresThreadFactory("a-l-t-P-");
        ThreadFactory factory = Thread.ofVirtual().factory();
        for (int i = 0; i < logicAysnThreadCount; ++i) {
            iMessageExecutors[i] = new DisruptorSingleExecutor(factory);
            iMessageExecutors[i].start();
        }
    }

    public <T1, T2, T3> void execute(long hashCode, T1 p1, T2 p2,T3 p3, EventThFunction<T1, T2, T3> method) {
        try {
            IMessageExecutor iMessageExecutor = getChannelIMessageExecutor();
            iMessageExecutor.execute(p1, p2,p3, method);
        } catch (Exception e) {
            log.error("---error-- ", e);
        }
    }

    public <T> void execute(long hashCode, long p1, T p2, EventBiFunction<T> method) {
        try {
            IMessageExecutor iMessageExecutor = getChannelIMessageExecutor();
            iMessageExecutor.execute(p1, p2, method);
        } catch (Exception e) {
            log.error("---error-- ", e);
        }
    }

    public <T> void execute(long hashCode, T p, EventFunction<T> method) {
        try {
            IMessageExecutor iMessageExecutor = getChannelIMessageExecutor();
            iMessageExecutor.execute(p, method);
        } catch (Exception e) {
            log.error("---error-- ", e);
        }
    }

    private IMessageExecutor getChannelIMessageExecutor() {
//        return iMessageExecutors[(processThreadCount - 1) & hash(aresPacket.getCtx().channel())];
        //only for one thread
        return iMessageExecutors[(processThreadCount - 1)];
    }
}
