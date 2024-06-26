package com.ares.core.timer;

import com.ares.core.thread.AresThreadFactory;
import com.ares.core.thread.task.EventFunction;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ScheduleService {
    private final static HashedWheelTimer HASHED_WHEEL_TIMER = new HashedWheelTimer(new AresThreadFactory("a-timer"), 1, TimeUnit.MILLISECONDS);
    public static ScheduleService INSTANCE;

    private Consumer<AresTimerTask<?>> aresTimerTaskConsumer;

    public ScheduleService(Consumer<AresTimerTask<?>> aresTimerTaskConsumer) {
        this.aresTimerTaskConsumer = aresTimerTaskConsumer;
        INSTANCE = this;
    }

    public <T> AresTimerTask<?> executeTimerTaskWithMS(long hashCode, EventFunction<T> function, T extraData, long timeOut) {
        return executeTimerTask(hashCode, function, extraData, timeOut, TimeUnit.MILLISECONDS, 1);
    }

    public <T> AresTimerTask<?> executeTimerTaskWithMS(long hashCode, EventFunction<T> function, T extraData, long timeOut, int maxCount) {
        return executeTimerTask(hashCode, function, extraData, timeOut, TimeUnit.MILLISECONDS, maxCount);
    }

    public <T> AresTimerTask<?> executeTimerTaskWithMS(EventFunction<T> function, T extraData, long timeOut) {
        return executeTimerTask(0, function, extraData, timeOut, TimeUnit.MILLISECONDS, 1);
    }

    public <T> AresTimerTask<?> executeTimerTask(long hashCode, EventFunction<T> function, T extraData, long timeOut, TimeUnit timeUnit, int exeCount) {
        AresTimerTask aresTimerTask = AresTimerTask.NewTimerTask(extraData, function);
        Timeout timeout = HASHED_WHEEL_TIMER.newTimeout(aresTimerTask, timeOut, timeUnit);
        aresTimerTask.setAresTimerTaskConsumer(aresTimerTaskConsumer);
        aresTimerTask.setTimeout(timeout);
        aresTimerTask.setExecuteHashCode(hashCode);
        //log.info("--add  timer task ={}  timer task now count ={}", timerTask, HASHED_WHEEL_TIMER.pendingTimeouts());
        return aresTimerTask;
    }

    public <T> void tryExecuteTimerTaskForNext(AresTimerTask<T> aresTimerTask) {
        if (aresTimerTask.reachMaxTimes()) {
            return;
        }
        //was canceled by player
        if (!aresTimerTask.isValid()) {
            return;
        }
        aresTimerTask.increaseCurCount();
        Timeout timeout = HASHED_WHEEL_TIMER.newTimeout(aresTimerTask, aresTimerTask.getTimeOut(), aresTimerTask.getTimeUnit());
        aresTimerTask.setTimeout(timeout);
    }
}
