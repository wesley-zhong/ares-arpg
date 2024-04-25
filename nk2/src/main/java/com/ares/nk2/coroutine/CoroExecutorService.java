package com.ares.nk2.coroutine;

import com.ares.nk2.coroutine.exception.CoroCheckedException;
import com.ares.nk2.timer.TimerManager;
import com.ares.nk2.timer.TimerUtil;
import com.ares.nk2.tool.StringFormatter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;

import static com.ares.nk2.coroutine.CoroHandle.RelationType.callJob;
import static com.ares.nk2.coroutine.CoroHandle.RelationType.runJob;

public class CoroExecutorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoroExecutorService.class);

    private final CoroJobQueue jobQueue;
    private final CoroutineContainer container;
    private final InitContext initContext;

    private final TimerManager timerManager;
    private final CopyOnWriteArrayList<WeakReference<TimerManager>> customTimerManagerList = new CopyOnWriteArrayList<>();
    private int zeroRunCnt = 0;

    public static final long DEFAULT_PARK_TIMEOUT_MS = 20_000L;
    ExecutorLocal.ExecutorLocalMap coroLocals = null;

    public static class InitContext {
        public final int executorServiceIndex;

        private InitContext(int executorServiceIndex) {
            this.executorServiceIndex = executorServiceIndex;
        }

        public static InitContext defaultInstance() {
            return new InitContext(0);
        }

        public static InitContext newInstance(int executorServiceIndex) {
            return new InitContext(executorServiceIndex);
        }
    }

    public CoroExecutorService(String name, int concurrentJobCnt, int maxJobOneCycle, CoroutineContainer container, InitContext initContext) {
        if (container == null) {
            container = CoroutineMgr.getInstance().createContainer(name);
            this.container = container;
        } else {
            this.container = container;
        }

        jobQueue = CoroutineMgr.getInstance().createJobQueue(name, concurrentJobCnt, maxJobOneCycle, false, container);
        this.initContext = initContext;
        timerManager = new TimerManager(name);

        startExecutorProc();
    }

    public static CoroExecutorService newInstanceWithExistingContainer(String name, int concurrentJobCnt, int maxJobOneCycle, CoroutineContainer container) {
        if (container == null) {
            throw new NullPointerException("container == null");
        }

        return new CoroExecutorService(name, concurrentJobCnt, maxJobOneCycle, container, InitContext.defaultInstance());
    }

    public static CoroExecutorService newInstanceWithNewContainer(String name, int concurrentJobCnt, int maxJobOneCycle, InitContext initContext) {
        return new CoroExecutorService(name, concurrentJobCnt, maxJobOneCycle, null, initContext);
    }

    public CoroutineContainer getContainer() {
        return container;
    }

    CoroJobQueue getCoroJobQueue() {
        return jobQueue;
    }

    public String getName() {
        return jobQueue.name;
    }

    public int getExecutorServiceIndex() {
        return initContext.executorServiceIndex;
    }

    final public <V> V callJob(long timeout, Callable<V> call, String jobname) {
        try {
            CoroutineContainer currentContainer = CoroutineMgr.getInstance().getCurrentContainer();
            if (currentContainer != null
                    && currentContainer == getContainer()) {
                try {
                    return call.call();
                } catch (Exception e) {
                    throw new RuntimeException("callJob failed", e);
                }
            } else {
                return callJobInternal(timeout, call, jobname);
            }
        } catch (TimeoutException e) {
            throw new RuntimeException("callJob timeout name " + jobname, e);
        }
    }

    final public <V> V callJob(Callable<V> call, String jobname) {
        return callJob(DEFAULT_PARK_TIMEOUT_MS, call, jobname);
    }

    final public <V> void runJob(Callable<V> call, String jobname) {
        runJobInternal(call, jobname);
    }

    public static CoroExecutorService current() {
        return CoroHandle.currentExectorService();
    }

    public static int getCurrentExecutorServiceIndex() {
        CoroExecutorService coroExecuterService = current();
        if (coroExecuterService != null) {
            return coroExecuterService.getExecutorServiceIndex();
        } else {
            return 0;
        }
    }

    protected <V> CoroHandle<V> submit(Callable<V> call, String jobName) {
        CoroHandle job = CoroHandle.newInstance(call, this, runJob, jobName);
        try {
            return CoroutineMgr.getInstance().submit(job);
        } catch (CoroCheckedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private <V> V callJobInternal(long timeout, Callable<V> call, String jobName) throws TimeoutException {
        CoroHandle<V> obj = CoroHandle.newInstance(call, this, callJob, jobName);
        try {
            CoroHandle<V> coroHandle = CoroutineMgr.getInstance().submit(obj);
            return coroHandle.get(timeout);
        } catch (CoroCheckedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private <V> void runJobInternal(Callable<V> call, String jobName) {
        CoroHandle<V> job = CoroHandle.newInstance(call, this, runJob, jobName);
        try {
            CoroutineMgr.getInstance().submit(job);
        } catch (CoroCheckedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void startExecutorProc() {
        runJob(this::startExecutorProc_2887, "procTimer");
    }

    private Integer startExecutorProc_2887() {
        CoroHandle handle = CoroHandle.current();
        if (handle != null) {
            handle.setNeedParkCountWarning(false);
        }
        executorProc();
        return 0;
    }

    private long runAllTimer() {
        long num = 0;
        num += timerManager.runTimer();
        for (WeakReference<TimerManager> item : customTimerManagerList) {
            TimerManager customTimerManager = item.get();
            if (customTimerManager == null) {
                customTimerManagerList.remove(item);
                continue;
            }
            if (customTimerManager.isDestroyed()) {
                LOGGER.error(StringFormatter.format("timerManager {} has destroyed, but not remove from executor", customTimerManager.getName()));
                customTimerManagerList.remove(item);
                continue;
            }
            num += customTimerManager.runTimer();
        }
        return num;
    }

    private void executorProc() {
        timerManager.setBelongThreadId(Thread.currentThread().getId());
        while (true) {
            try {
                long num = runAllTimer();
                if (num == 0) {
                    ++zeroRunCnt;
                    if (zeroRunCnt >= 30) {
                        CoroHandle.sleep(TimerUtil.MS_PER_TICK);
                        zeroRunCnt = 0;
                    }
                } else {
                    zeroRunCnt = 0;
                    CoroHandle.yield();
                }
            } catch (Exception e) {
                LOGGER.error(StringFormatter.format("executor-{} proc failed.", getName()), e);
            }
        }
    }

    public TimerManager getTimerManager() {
        return timerManager;
    }

    List<WeakReference<TimerManager>> getCustomTimerManagers() {
        return customTimerManagerList;
    }

    void registerCustomTimerManager(TimerManager timerManager) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(StringFormatter.format("register timerManager {}", timerManager.getName()));
        }
        timerManager.setBelongThreadId(Thread.currentThread().getId());
        customTimerManagerList.add(new WeakReference<>(timerManager));
    }

    void removeCustomTimerManager(TimerManager timerManager) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(StringFormatter.format("remove timerManager {}", timerManager.getName()));
        }
        for (WeakReference<TimerManager> item : customTimerManagerList) {
            if (item.get() == timerManager) {
                customTimerManagerList.remove(item);
                break;
            }
        }
    }
}

