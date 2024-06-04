package com.ares.nk2.coroutine;

import com.ares.nk2.timer.TimerHandle;
import com.ares.nk2.timer.TimerManager;
import com.ares.nk2.tool.NKStringFormater;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.Callable;

public class CurrentExecutorUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrentExecutorUtil.class);

    public static <V> void runJob(String jobName, Callable<V> callable) {
        CoroExecutorService coroExecutorService = CoroExecutorService.current();
        if (coroExecutorService == null) {
            return;
        }

        try {
            coroExecutorService.runJob(callable, jobName);
        } catch (Exception e) {
            LOGGER.error(NKStringFormater.format("failed to runJob, jobName: {}", jobName), e);
        }
    }

    public static <V> List<CoroHandle<V>> batchSubmitJob(Collection<Callable<V>> callableCollection, String jobName) {
        ArrayList<CoroHandle<V>> coroHandleArray = new ArrayList<>();
        CoroExecutorService coroExecutorService = CoroExecutorService.current();
        if (coroExecutorService == null) {
            return coroHandleArray;
        }

        for (Callable<V> callable : callableCollection) {
            CoroHandle<V> coroHandle = coroExecutorService.submit(callable, jobName);
            if (coroHandle != null) {
                coroHandleArray.add(coroHandle);
            } else {
                LOGGER.error("coroHandle == null");
            }
        }

        return coroHandleArray;
    }

    public static <K, V> Map<K, CoroHandle<V>> batchSubmitJob(Map<K, Callable<V>> callableMap, String jobName) {
        Map<K, CoroHandle<V>> coroHandleMap = new HashMap<>();

        CoroExecutorService coroExecutorService = CoroExecutorService.current();
        if (coroExecutorService == null) {
            return coroHandleMap;
        }

        for (Map.Entry<K, Callable<V>> entry : callableMap.entrySet()) {
            CoroHandle<V> coroHandle = coroExecutorService.submit(entry.getValue(), jobName);
            if (coroHandle != null) {
                coroHandleMap.put(entry.getKey(), coroHandle);
            } else {
                LOGGER.error("coroHandle == null");
            }
        }

        return coroHandleMap;
    }

    public static TimerHandle addOnceTimer(Runnable action, long initialDelayMs) {
        CoroExecutorService service = CoroExecutorService.current();
        if (service != null) {
            return service.getTimerManager().addOnceTimer(action, initialDelayMs);
        } else {
            throw new RuntimeException("not add in coro env");
        }
    }

    public static TimerHandle addRepeatTimer(Runnable action, long initialDelayMs, long periodMs) {
        CoroExecutorService service = CoroExecutorService.current();
        if (service != null) {
            return service.getTimerManager().addRepeatTimer(action, initialDelayMs, periodMs);
        } else {
            throw new RuntimeException("not add in coro env");
        }
    }

    public static long getCurrentTick() {
        return getTimerManager().getCurrentTick();
    }

    public static int getTimerNum() {
        return getTimerManager().getTimerNum();
    }

    private static TimerManager getTimerManager() {
        return CoroExecutorService.current().getTimerManager();
    }

    public static void registerCustomTimerManager(TimerManager timerManager) {
        CoroExecutorService.current().registerCustomTimerManager(timerManager);
    }

    public static void removeCustomTimerManager(TimerManager timerManager) {
        CoroExecutorService.current().removeCustomTimerManager(timerManager);
    }

    public static boolean hasCoroEnv() {
        return CoroExecutorService.current() != null;
    }

    public static void resetTraceId() {
        try {
            CoroHandle<?> coroHandle = CoroHandle.current();
            if (coroHandle != null) {
                coroHandle.getCoroBaseInfo().resetTraceId();
            }
        } catch (Throwable e) {
            LOGGER.error("reset trace id failed. ", e);
        }
    }
}


