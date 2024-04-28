package com.ares.nk2.tool;

public class DateUtils {
    public static long currentTimeSec() {
        return System.currentTimeMillis() / 1000;
    }

    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static long currentTimeNanos() {
        return System.nanoTime();
    }

    public static long currentTimeMicroSec() {
        return System.nanoTime() / 1000;
    }
}
