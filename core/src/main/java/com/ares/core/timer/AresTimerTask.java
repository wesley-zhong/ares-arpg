package com.ares.core.timer;


import com.ares.core.thread.task.EventBiFunction;
import com.ares.core.thread.task.EventFunction;
import io.netty.util.Recycler;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
    private final static AtomicLong  taskIdGen = new AtomicLong(0);
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

    public static <T>  AresTimerTask<T> NewTimerTask(T extData, EventFunction<T> eventBiFunction) {
        AresTimerTask<T> aresTimerTask = new AresTimerTask<>() ;//RECYCLER.get();
        aresTimerTask.taskId = taskIdGen.incrementAndGet();
        aresTimerTask.call = eventBiFunction;
        aresTimerTask.extData = extData;
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


    public boolean cancel() {
        if (timeout == null) {
            return false;
        }
        return timeout.cancel();
    }

    public boolean isValid() {
        return timeout != null;
    }

    public boolean release() {
        if (cancel()) {
            /**
             * release should be called when canceled success or task time expired
             */
            //log.info("cancelTask ={}  success", this);
            clear();
         //   this.recyclerHandler.recycle(this);
            return true;
        }
        //  log.info("cancelTask={} failed", this);
        clear();
        return false;
    }

    private void forceRelease() {
        // log.info("{}, forceRelease  ", this);
        clear();
      //  this.recyclerHandler.recycle(this);
    }


    public void clear() {
        timeout = null;
        call = null;
        extData = null;
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

//    private AresTimerTask(Recycler.Handle<AresTimerTask> handler) {
//        this.recyclerHandler = handler;
//    }
}
