package com.ares.nk2.timer;

import java.util.concurrent.atomic.AtomicLong;

public class TimerHandle implements Comparable<TimerHandle> {

    protected final TimerManager timerManager;
    private final Runnable action;
    private final long periodTick;
    private final boolean runForever;
    private long expireTick;
    private String debugLogString = null;
    private volatile boolean cancelled = false;
    private final AtomicLong runCount = new AtomicLong(0L);
    protected TimerHandle prev;
    protected TimerHandle next;

    TimerHandle(TimerManager timerManager, Runnable action, long initialDelayTick, long periodTick, boolean runForever) {
        this.action = action;
        this.periodTick = periodTick;
        this.runForever = runForever;
        this.timerManager = timerManager;
        expireTick = timerManager.getCurrentTick() + initialDelayTick;
    }


    TimerHandle() {
        action = ()->{};
        timerManager = null;
        periodTick = 8;
        runForever = false;
    }

    public void cancel() {
        if (cancelled) {
            return;
        }
        cancelled = true;
        timerManager.cancelTimer(this);
    }

    public void runAction() {
        action.run();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    protected long getExpireTick() {
        return expireTick;
    }

    public long getExpireMillis() {
        return TimerUtil.tickToMs(expireTick);
    }

    // 内部接口
    protected void doNotCall_resetExpireTick() {
        expireTick = timerManager.getCurrentTick() + periodTick;
    }

    // 内部接口
    protected void gm_doNotCall_setExpireTick(long setTo) {
        long intervalMs = setTo - timerManager.getCurrentLogicTimeMs();
        long intervalTick = TimerUtil.msToTick(intervalMs);
        expireTick = timerManager.getCurrentTick() + (long) Math.ceil(intervalTick * 1.0d / periodTick) * periodTick;
    }

    public boolean isRunForever() {
        return runForever;
    }

    @Override
    public int compareTo(TimerHandle o) {
        if (expireTick > o.expireTick) {
            return 1;
        } else if (expireTick < o.expireTick) {
            return -1;
        } else {
            return 0;
        }
    }

    TimerManager getTimerManager() {
        return timerManager;
    }

    public String getDebugLogString() {
        return debugLogString;
    }

    public void setDebugLogString(String debugLogString) {
        this.debugLogString = debugLogString;
    }

    public void addRunCount() {
        runCount.incrementAndGet();
    }

    public long getRunCount() {
        return runCount.get();
    }
}
