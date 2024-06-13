package com.ares.core.timer;


import com.ares.core.thread.task.EventFunction;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;


@Slf4j
public class AresTimerTask<T> implements TimerTask {
    //    private static final Recycler<AresTimerTask> RECYCLER = new Recycler<>(1 << 16) {
//        @Override
//        protected AresTimerTask newObject(Handle<AresTimerTask> handler) {
//            return new AresTimerTask(handler);
//        }
//    };
    private final static AtomicLong taskIdGen = new AtomicLong(0);
    // private final Recycler.Handle<AresTimerTask> recyclerHandler;
    @Getter
    protected long taskId;
    @Setter
    protected volatile Timeout timeout;

    @Getter
    private T extData;
    @Setter
    @Getter
    private long executeHashCode;

    @Setter
    @Getter
    private EventFunction<T> call;

    @Setter
    private Consumer<AresTimerTask<T>> aresTimerTaskConsumer;

    private int maxCount;
    private int curCount;
    @Getter
    @Setter
    private long timeOut;
    @Getter
    @Setter
    private TimeUnit timeUnit;

    public static <T> AresTimerTask<T> NewTimerTask(T extData, EventFunction<T> eventBiFunction) {
        AresTimerTask<T> aresTimerTask = new AresTimerTask<>();//RECYCLER.get();
        aresTimerTask.taskId = taskIdGen.incrementAndGet();
        aresTimerTask.call = eventBiFunction;
        aresTimerTask.extData = extData;
        aresTimerTask.curCount = 0;
        aresTimerTask.maxCount = 1;
        return aresTimerTask;
    }

    public static <T> AresTimerTask<T> NewTimerTask(T extData, EventFunction<T> eventBiFunction, int maxCount) {
        AresTimerTask<T> aresTimerTask = new AresTimerTask<>();//RECYCLER.get();
        aresTimerTask.taskId = taskIdGen.incrementAndGet();
        aresTimerTask.call = eventBiFunction;
        aresTimerTask.extData = extData;
        aresTimerTask.curCount = 0;
        aresTimerTask.maxCount = maxCount;
        return aresTimerTask;
    }


    @Override
    public String toString() {
        return "buffTimerTaskId=" + taskId;
    }

    @Override
    public void run(Timeout timeout) {
        aresTimerTaskConsumer.accept(this);
    }

    public boolean reachMaxTimes() {
        return curCount >= maxCount;
    }

    public int increaseCurCount() {
        curCount++;
        return curCount;
    }


    public boolean cancel() {
        if (timeout == null) {
            return false;
        }
        boolean ret = timeout.cancel();
        clear();
        return ret;
    }

    public boolean isValid() {
        return timeout != null;
    }

    public void clear() {
        timeout = null;
        call = null;
        extData = null;
        maxCount = 0;
    }

    @Override
    public boolean equals(Object target) {
        if (target == null) {
            return false;
        }
        if (this == target) {
            return true;
        }
        if (target instanceof AresTimerTask) {
            AresTimerTask buffTarget = (AresTimerTask) target;
            return this.getTaskId() == buffTarget.getTaskId();
        }
        return false;
    }
}
