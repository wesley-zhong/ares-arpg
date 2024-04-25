package com.ares.nk2.timer;

import com.ares.nk2.coroutine.CurrentExecutorUtil;
import com.ares.nk2.coroutine.NoAsyncCheck;
import com.ares.nk2.tool.DateUtils;
import com.ares.nk2.tool.StringFormatter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class TimerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimerManager.class);

    //每次最多运行timer时间
    private static final long MAX_RUN_TIMER_MS_PER_TIME = 100;

    public TimerWheel timerWheel;

    private final String name;
    //初始化时间，
    private final long initTimeMs;
    private final long initTick;

    private boolean isDestroyed = false;
    private boolean firstRun = true;
    private volatile boolean isRunning = false;

    private long belongThreadId = 0;

    private long startRunTimerMs = 0;

    // 暂停/加速
    private static final long ACCELERATE_RATE_BASE = 100;
    private static final long ACCELERATE_RATE_MAX = 10000;
    private long accelerateRate = ACCELERATE_RATE_BASE;
    private long statusStartTimeMs = 0;
    private long statusStartTick = 0;
    private long totalOffsetTimeMs = 0;

    /**
     * 战斗模拟器测试使用，快速地tick
     */
    private boolean enableFastRunning = false;

    private TimerHandle curTimerHandle;

    public TimerManager(String name) {
        this(name, TimerUtil.msToTick(getCurrentSystemTimeMs()));
    }

    //如果有加速和暂停需求，请自行存储上次运行的tick数
    public TimerManager(String name, long initTick) {
        this.name = name;
        initTimeMs = getCurrentSystemTimeMs();
        this.initTick = initTick;
        timerWheel = new TimerWheel(initTick);
        timerWheel.init();
        accelerateRate = ACCELERATE_RATE_BASE;
        statusStartTimeMs = initTimeMs;
        statusStartTick = initTick;
    }

    public long getCurrentTick() {
        return timerWheel.getCurrentTick();
    }

    //通过tick数获取当前的系统时间
    public long getTimeMsByTick(long tick) {
        return initTimeMs + TimerUtil.tickToMs(tick - initTick);
    }

    public long getCurrentLogicTimeMs() {
        return getTimeMsByTick(timerWheel.getCurrentTick());
    }

    private long getElapsedTimeMs() {
        long currentTime = getCurrentSystemTimeMs();
        return (statusStartTimeMs - initTimeMs) + totalOffsetTimeMs + accelerateRate * (currentTime - statusStartTimeMs) / ACCELERATE_RATE_BASE;
    }

    public long getRealTimeTick() {
        long elapsedTimeMs = getElapsedTimeMs();
        return initTick + TimerUtil.msToTick(elapsedTimeMs);
    }

    /**
     * 内部使用，下取整
     *
     * @return
     */
    private long getRealTimeTickFloor() {
        long elapsedTimeMs = getElapsedTimeMs();
        return initTick + elapsedTimeMs / TimerUtil.MS_PER_TICK;
    }

    public void cancelTimer(TimerHandle timer) {
        timerWheel.deleteTimer(timer);
    }

    /**
     * 一次性timer，支持手动取消
     */
    public TimerHandle addOnceTimer(Runnable action, long initialDelayMs) {
        return addTimerInner(action, TimerUtil.msToTick(initialDelayMs), 0, false);
    }

    /**
     * 可重复timer，支持手动取消
     */
    public TimerHandle addRepeatTimer(Runnable action, long initialDelayMs, long periodMs) {
        return addTimerInner(action, TimerUtil.msToTick(initialDelayMs), TimerUtil.msToTick(periodMs), true);
    }

    private void callTimeoutAction(TimerHandle handler) {
        try {
            handler.runAction();
        } catch (Exception e) {
            LOGGER.error(StringFormatter.format("timer-{} run failed.", handler), e);
        }
    }


    private void runOneTimer(TimerHandle handler) {
        try {
            handler.addRunCount();

            try (NoAsyncCheck noAsyncCheck = NoAsyncCheck.newInstance().begin()) {
                setCurTimerHandle(handler);
                callTimeoutAction(handler);
                CurrentExecutorUtil.resetTraceId();
            } finally {
                //清除当前执行的TimerHandle
                setCurTimerHandle(null);
                if (handler.isRunForever() && !handler.isCancelled()) {
                    handler.doNotCall_resetExpireTick();
                    addTimerToQueue(handler);
                }
            }
        } catch (Exception e) {
            LOGGER.error(StringFormatter.format("{} run timer {} field.", name, handler), e);
        }
    }

    private static final long TIMER_DELAY_WARNING = 25;

    public int runTimer() {
        if (belongThreadId != 0 && belongThreadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(StringFormatter.format("timerManager-{} belongs to thread-{}, but run at thread-{}", name, belongThreadId, Thread.currentThread().getId()));
        }

        if (isPaused() || isDestroyed || isRunning) {
            return 0;
        }

        startRunTimerMs = getCurrentSystemTimeMs();
        int num = 0;
        try {
            isRunning = true;
            long realTick = getRealTimeTickFloor();
            long tickNum = realTick - getCurrentTick();
            if (tickNum > TIMER_DELAY_WARNING && !firstRun) {
                LOGGER.warn(StringFormatter.format("timerManager-{} tick delay num:{}, currentTick:{}, realTimeTick:{}", name, tickNum, getCurrentTick(), getRealTimeTick()));
            } else if (tickNum < 0 && tickNum % 3000 == 0) {
                LOGGER.error(StringFormatter.format("realTimeTick {} less than logicTimeTick {}. maybe gm set system time before.", realTick, getCurrentTick()));
            }

            timerWheel.setRealTimeTick(realTick);
            if (isEnableFastRunning()) {
                timerWheel.setRealTimeTick(Long.MAX_VALUE);
            }

            while (true) {
                if (isDestroyed) {
                    break;
                }
                if (getCurrentSystemTimeMs() - startRunTimerMs > MAX_RUN_TIMER_MS_PER_TIME) {
                    break;
                }

                TimerHandle handler = timerWheel.peek();
                if (handler == null) {
                    break;
                }

                if (handler.getExpireTick() < getCurrentTick()) {
                    LOGGER.error(StringFormatter.format("timer-{} expireTick:{} < currentTick:{}", handler, handler.getExpireTick(), getCurrentTick()));
                } else if (handler.getExpireTick() > getCurrentTick()) {
                    throw new IllegalStateException(StringFormatter.format("timer-{} expireTick:{} > currentTick:{}. FATAL ERROR, PLEASE SHUTDOWN SERVER!!!", handler, handler.getExpireTick(), getCurrentTick()));
                }
                timerWheel.deleteTimer(handler);

                ++num;

                if (handler.isCancelled()) {
                    continue;
                }

                runOneTimer(handler);
            }
        } finally {
            firstRun = false;
            isRunning = false;
        }
        return num;
    }

    public int getTimerNum() {
        return timerWheel.getSize();
    }

    public boolean isNormal() {
        return accelerateRate == ACCELERATE_RATE_BASE;
    }

    public boolean isPaused() {
        return accelerateRate == 0L;
    }

    public boolean isAccelerated() {
        return ACCELERATE_RATE_BASE < accelerateRate;
    }

    public boolean pause() {
        if (accelerateRate == 0L) {
            return false;
        }
        return setAccelerateRate(0);
    }

    public boolean accelerate(float rate) {
        long value = (long) (rate * (float) ACCELERATE_RATE_BASE);
        return setAccelerateRate(value);
    }

    public boolean resume() {
        if (accelerateRate == ACCELERATE_RATE_BASE) {
            return false;
        }
        return setAccelerateRate(ACCELERATE_RATE_BASE);
    }

    private boolean setAccelerateRate(long newAccRate) {
        if (newAccRate < 0 || newAccRate > ACCELERATE_RATE_MAX) {
            throw new IllegalArgumentException(StringFormatter.format("timerManager-{} accRate:{} not in range[0,{}]", this, newAccRate, ACCELERATE_RATE_MAX));
        }
        if (newAccRate == accelerateRate) {
            return false;
        }
        long oldAccRate = accelerateRate;
        long currentTimeMs = getCurrentSystemTimeMs();
        // tmpOffsetTimeMs 当前时间 至 上一次修改状态 的逻辑偏移时间
        long tmpOffsetTimeMs = ((accelerateRate - ACCELERATE_RATE_BASE) * (currentTimeMs - statusStartTimeMs) / ACCELERATE_RATE_BASE);
        totalOffsetTimeMs += tmpOffsetTimeMs;
        accelerateRate = newAccRate;
        statusStartTimeMs = currentTimeMs;
        statusStartTick = getCurrentTick();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(StringFormatter.format("timerManager-{} set accelerate rate set from {} to {}. tmpOffsetTimeMs:{}, totalOffsetTimeMs:{}, currentTick:{}, currentLogicTimeMs:{}, currentSystemTimeMs:{}"
                    , name, oldAccRate, newAccRate, tmpOffsetTimeMs, totalOffsetTimeMs, getCurrentTick(), getCurrentLogicTimeMs(), getCurrentSystemTimeMs()));
        }
        return true;
    }

    public void destroy() {
        LOGGER.warn(StringFormatter.format("timerManager {} destroyed", name));
        isDestroyed = true;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public String getName() {
        return name;
    }

    public void setBelongThreadId(long threadId) {
        belongThreadId = threadId;
    }

    void addTimerToQueue(TimerHandle timer) {
        timerWheel.addTimer(timer);
    }

    @Override
    public String toString() {
        return name;
    }

    public void resetForTest() {
        timerWheel.clear();
    }

    public TimerHandle peekForTest() {
        return timerWheel.peek();
    }

    private TimerHandle addTimerInner(Runnable action, long initialDelayTick, long periodTick, boolean runForever) {
        checkAddTimerParam(runForever, periodTick);

        TimerHandle timer = new TimerHandle(this, action, initialDelayTick, periodTick, runForever);
        addTimerToQueue(timer);
        return timer;
    }

    private void checkAddTimerParam(boolean runForever, long periodTick) {
        if (belongThreadId != 0 && belongThreadId != Thread.currentThread().getId()) {
            throw new IllegalStateException(StringFormatter.format("timerManager-{} belongs to thread-{}, but timer add in thread-{}",
                    name, belongThreadId, Thread.currentThread().getId()));
        }
        if (runForever && periodTick <= 0) {
            throw new IllegalStateException(StringFormatter.format("forever timer period {} invalid", periodTick));
        }
    }

    private static long getCurrentSystemTimeMs() {
        return DateUtils.currentTimeMillis() / TimerUtil.MS_PER_TICK * TimerUtil.MS_PER_TICK;
    }

    public float getAccelerateRate() {
        if (accelerateRate == 0L) {
            return 0.0f;
        }
        if (accelerateRate == ACCELERATE_RATE_BASE) {
            return 1.0f;
        }
        return (float) accelerateRate / (float) ACCELERATE_RATE_BASE;
    }

    public long getStatusStartTime() {
        return statusStartTimeMs;
    }

    public long getStatusStartTick() {
        return statusStartTick;
    }

    public void doNotDirectCallSetEnableFastRunning(boolean enableFastRunning) {
        this.enableFastRunning = enableFastRunning;
        if (!enableFastRunning) {
            timerWheel.setRealTimeTick(getRealTimeTickFloor());
        }
    }

    public boolean isEnableFastRunning() {
        return enableFastRunning;
    }

    public TimerHandle getCurTimerHandle() {
        return curTimerHandle;
    }

    private void setCurTimerHandle(TimerHandle timerHandle) {
        curTimerHandle = timerHandle;
    }
}
