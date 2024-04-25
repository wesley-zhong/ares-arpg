package com.ares.nk2.timer;

public class TimerUtil {
    public final static long TICK_PER_SECOND = 50;
    public final static long MS_PER_TICK = 1000 / TICK_PER_SECOND;//20ms
    public final static long NS_PER_SECOND = 1000000000;
    public final static Object TIMER_HANDLE_DEFAULT_WEAK = new Integer(0);

    public static long msToTick(long deltaMs) {
        return (deltaMs + MS_PER_TICK - 1) / MS_PER_TICK;
    }


    /**
     * 返回delta ms
     *
     * @param tick 帧
     * @return 经过的逻辑时间
     */
    public static long tickToMs(long tick) {
        return tick * MS_PER_TICK;
    }

    public static long tickToSecond(long tick) {
        return tickToMs(tick) / 1000;
    }

    public static long nsToMs(long ns) {
        return ns / 1000000;
    }

    public static long nsToUs(long ns) {
        return ns / 1000;
    }
}
