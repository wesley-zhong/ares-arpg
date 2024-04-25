package com.ares.nk2.timer;

import com.ares.nk2.tool.StringFormatter;

public class TimerWheel {
//    private static final Logger LOGGER = LoggerFactory.getLogger(TimerWheel.class);

    //TVR是第一个轮数据，TVN是后面几个轮
    private static final int TVR_BITS = 8;
    private static final int TVN_BITS = 6;
    private static final int TVR_SIZE = 1 << TVR_BITS;
    private static final int TVN_SIZE = 1 << TVN_BITS;
    private static final int TVR_MASK = TVR_SIZE - 1;
    private static final int TVN_MASK = TVN_SIZE - 1;
    private static final int INIT_TVN_COUNT = 4;

    //服务器从启动到现在运行的tick数，每20ms tick一次
    private long currentTick;
    private long realTick;


    public final TimerVector[] timerVectors = new TimerVector[INIT_TVN_COUNT + 1];
    private TimerList runningTimerList;
    private int count;

    public TimerWheel(long currentTick) {
        this.currentTick = currentTick;
        realTick = currentTick;
    }

    public void init() {
        timerVectors[0] = new TimerVector(TVR_SIZE);
        for (int i = 0; i < INIT_TVN_COUNT; i++) {
            timerVectors[i + 1] = new TimerVector(TVN_SIZE);
        }
        count = 0;
        runningTimerList = timerVectors[0].get(Math.toIntExact(currentTick & TVR_MASK));
    }

    public static class TimerVector {
        private final TimerList[] timerList;

        TimerVector(int size) {
            timerList = new TimerList[size];
            for (int i = 0; i < size; i++) {
                timerList[i] = new TimerList();
            }
        }

        TimerList get(int index) {
            if (index >= 0 && index < timerList.length) {
                return timerList[index];
            }

            return null;
        }
    }

    boolean addTimer(TimerHandle timer) {
        if (timer.next != null) {
            assert timer.prev != null;
            throw new IllegalStateException(StringFormatter.format("timer-{} already in timerManager-{}.", timer, timer.next.getTimerManager()));
        }

        TimerList timerList = calculateLocation(timer);
        if (timer.getExpireTick() >= currentTick) {
            timerList.addTimerBack(timer);
        } else {
            timerList.addExpiredTimer(timer);
            throw new IllegalStateException(StringFormatter.format("timer-{} already expired, currentTick {}, expiredTick {}", timer, currentTick, timer.getExpireTick()));
        }
        ++count;

        return true;
    }

    private TimerList calculateLocation(TimerHandle timerHandle) {
        long expiredTick = timerHandle.getExpireTick();
        long curTick = currentTick;
        long idx = expiredTick - curTick;
        int index;
        if (idx < 0) {
            return runningTimerList;
        } else if (idx < TVR_SIZE) {
            index = Math.toIntExact(expiredTick & TVR_MASK);
            return  timerVectors[0].get(index);
        } else if (idx < (1 << (TVR_BITS + TVN_BITS))) {
            index = Math.toIntExact((expiredTick >> TVR_BITS) & TVN_MASK);
            return timerVectors[1].get(index);
        } else if (idx < (1 << (TVR_BITS + 2 * TVN_BITS))) {
            index = Math.toIntExact((expiredTick >> (TVR_BITS + TVN_BITS)) & TVN_MASK);
            return timerVectors[2].get(index);
        } else if (idx < (1 << (TVR_BITS + 3 * TVN_BITS))) {
            index = Math.toIntExact((expiredTick >> (TVR_BITS + 2 * TVN_BITS)) & TVN_MASK);
            return timerVectors[3].get(index);
        } else {
            if ((idx >> (TVR_BITS + 4 * TVN_BITS)) > 0) {
                index = Math.toIntExact((currentTick >> (TVR_BITS + 3 * TVN_BITS)) & TVN_MASK);
            } else {
                index = Math.toIntExact((expiredTick >> (TVR_BITS + 3 * TVN_BITS)) & TVN_MASK);
            }
            return timerVectors[4].get(index);
        }
    }

    boolean deleteTimer(TimerHandle timer) {
        if (timer.next != null && timer.prev != null) {
            TimerList.removeTimer(timer);
            --count;
            return true;
        } else {
            timer.next = null;
            timer.prev = null;
            return false;
        }
    }

    public int getSize() {
        return count;
    }

    long getCurrentTick() {
        return currentTick;
    }

    public TimerHandle peek() {
        tick();
        return runningTimerList.peek();
    }

    public TimerHandle poll() {
        tick();

        TimerHandle timer = runningTimerList.poll();
        if (timer != null) {
            count--;
        }
        return timer;
    }

    void setRealTimeTick(long realTick) {
        this.realTick = realTick;
    }

    void clear() {
        init();
    }

    private void tick() {
        if (!runningTimerList.isEmpty()) {
            return;
        }

        while (realTick > currentTick) {
            ++currentTick;
            long index = currentTick & TVR_MASK;
            runningTimerList = timerVectors[0].get((int) index);
            int i = 0;
            while (index == 0 && i < INIT_TVN_COUNT) {
                index = (currentTick >> (TVR_BITS + i * TVN_BITS)) & TVN_MASK;
                cascade(i + 1, (int) index);
                i++;
            }

            if (!runningTimerList.isEmpty()) {
                return;
            }
        }
    }

    private void cascade(int wheelIndex, int slot) {
        TimerList carryList = timerVectors[wheelIndex].get(slot);
        TimerHandle timerHandle = carryList.pollTail();
        while (timerHandle != null) {
            TimerList addToList = calculateLocation(timerHandle);
            if (timerHandle.getExpireTick() >= currentTick) {
                addToList.addTimerFront(timerHandle);
            } else {
                addToList.addExpiredTimer(timerHandle);
                throw new IllegalStateException(
                        StringFormatter.format("timer-{} already expired, currentTick {}, expiredTick {}",
                                timerHandle, currentTick, timerHandle.getExpireTick()));
            }
            timerHandle = carryList.pollTail();
        }
    }

    /**
     * 当前tick的串行 timer list是否为空
     *
     * @return
     */
    public boolean isRunningTimerListEmpty() {
        return runningTimerList.isEmpty();
    }
}
