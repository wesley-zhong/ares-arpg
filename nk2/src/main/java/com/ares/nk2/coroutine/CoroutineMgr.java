package com.ares.nk2.coroutine;

import com.ares.nk2.coroutine.exception.CoroCheckedException;
import com.ares.nk2.coroutine.exception.CoroExecutionException;
import com.ares.nk2.coroutine.exception.UntimedCoroInterruptedException;
import com.ares.nk2.tool.DateUtils;
import com.ares.nk2.tool.FunctionUtil;
import com.ares.nk2.tool.NKStringFormater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.ares.nk2.coroutine.CoroHandle.CoroJobStatus.PARKED;

@ThreadSafe
public class CoroutineMgr {
    static final long DEFAULT_PARK_TIMEOUT_MS = 20000;
    private static final Logger LOGGER = LoggerFactory.getLogger(CoroutineMgr.class);

    final CopyOnWriteArrayList<CoroutineContainer> coroContainerArray = new CopyOnWriteArrayList<>();
    final ArrayList<CoroutineContainer> systemContainerArray = new ArrayList<>(16);
    private final AtomicInteger asyncIdCnt = new AtomicInteger(0);
    private final AtomicLong nextUniqueNumber = new AtomicLong(0);

    volatile boolean isStop = false;
    @SuppressWarnings("FieldCanBeLocal")
    private long shutDownTime = 0;
    private long lastProcTime = DateUtils.currentTimeMillis();
    private long checkUntimeCoro = DateUtils.currentTimeMillis();

    static public CoroutineMgr getInstance() {
        return CoroutineConfig.getMgr();
    }

    final void init() {
        for (int i = 0; i < CoroutineConfig.getCoroThreadCount(); ++i) {
            systemContainerArray.add(newContainerInstance("SharedThread" + i + "-"));
        }

        coroContainerArray.forEach(CoroutineContainer::startContainer);
    }

    protected CoroutineContainer newContainerInstance(String threadName) {
        return new CoroutineContainer(threadName);
    }

    CoroutineContainer createContainer(String name) {
        CoroutineContainer coroutineContainer = newContainerInstance(name);
        coroutineContainer.startContainer();
        return coroutineContainer;
    }

    private CoroutineContainer chooseSystemContainer(int shareKey) {
        int realId = shareKey % systemContainerArray.size();
        return systemContainerArray.get(realId);
    }

    public CoroutineContainer getSystemContainer() {
        return chooseSystemContainer(0);
    }

    public CoroJobQueue createJobQueue(String qName, int concurrentJobCnt, int maxJobOneCycle,
                                       boolean checkEmptyBeforeStop, CoroutineContainer bindContainer) {
        if (bindContainer == null) {
            LOGGER.error(NKStringFormater.format("{} bindContainer is null!", qName));
            return null;
        }
        return bindContainer.createJobQueue(qName, concurrentJobCnt, maxJobOneCycle, checkEmptyBeforeStop);
    }

    public final <V> CoroHandle<V> submit(CoroHandle<V> coroHandle) throws CoroCheckedException {
        CoroutineContainer container = coroHandle.getExecutorService().getContainer();
        if (container == null) {
            throw new CoroCheckedException("CoroQueueTypeNotExist");
        }
        if (coroHandle.getCoroBaseInfo().getJobName() == null || coroHandle.getCoroBaseInfo().getJobName().isEmpty()) {
            try {
                throw new CoroCheckedException("coroHandle with no name");
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }

        return container.offerJob(coroHandle);
    }

    final void sleep(long ms) throws UntimedCoroInterruptedException {
        try {
            realPark(null, ms, null, false);
        } catch (TimeoutException e) {
            LOGGER.error("ERROR, sleep should not timeException: ", e);
        }
    }

    final void park(Object parkVerifyObj) throws UntimedCoroInterruptedException {
        try {
            realPark(parkVerifyObj, 0, null, false);
        } catch (TimeoutException e) {
            LOGGER.error("ERROR, umTimed park should not timeException: ", e);
        }
    }

    final void park(Object parkVerifyObj, long ms) throws TimeoutException {
        try {
            realPark(parkVerifyObj, ms, null, true);
        } catch (UntimedCoroInterruptedException e) {
            LOGGER.error("", e);
        }
    }

    final void doYieldTo() {
        CoroutineContainer container = getCurrentContainer();
        if (container == null) {
            return;
        }
        container.sch.doYieldBySch();
    }

    final void yield() {
        CoroHandle<?> handle = current();
        if (handle != null) {
            handle.getCoroWorker().addReadyJobList();
            doYieldTo();
        } else {
            Thread.yield();
        }
    }

    protected void realPark(Object parkVerifyObj, long timeoutMillisecs, CoroHandle<?> waitingHandle, boolean timeoutException) throws
            TimeoutException, UntimedCoroInterruptedException {
        if (timeoutMillisecs < 0) {
            throw new IllegalArgumentException("timeout < 0 " + timeoutMillisecs);
        }

        CoroHandle<?> currentHandle = current();
        if (currentHandle == null) {
            throw new CoroExecutionException("Cannot park without Coroutine");
        }

        currentHandle.getParkInfo().setPark(parkVerifyObj, timeoutMillisecs);

        if (timeoutMillisecs > 0) {
            currentHandle.getCoroWorker().addParkTimeoutList();
        } else {
            currentHandle.getCoroWorker().addUntimeList();
        }

        if (waitingHandle != null) {
            currentHandle.waitForFiniHandle = waitingHandle;
            waitingHandle.addWaiter(currentHandle);
        }
        doYieldTo();
        currentHandle.waitForFiniHandle = null;
        try {
            if (currentHandle.getParkInfo().isTimeout() && timeoutException) {
                String info = "None waiting";

                if (waitingHandle != null) {
                    info = NKStringFormater.format("park timeout, enQueueTime: {}, startruntime: {}, coroid: {}, coroname: {}, parktimeout: {}, timeoutMillisecs: {}, detail: {}",
                            waitingHandle.getCoroStatInfo().getEnQueueTime(),
                            waitingHandle.getCoroStatInfo().getStartRunTime(),
                            waitingHandle.getCoroBaseInfo().coroutineId(),
                            waitingHandle.coroutineName(),
                            waitingHandle.getParkInfo().getParkTimeout(),
                            timeoutMillisecs,
                            waitingHandle.getCoroBaseInfo().getJobName());
                    if (waitingHandle.getContainer() == currentHandle.getContainer()) {
                        if (waitingHandle.getCoroBaseInfo().getStatus() == PARKED) {
                            StackTraceElement[] elements = waitingHandle.getStackTrace();
                            LOGGER.error(NKStringFormater.format("timeout for waiting handle not completed {} jobname {}"
                                    , elements == null ? "none" : FunctionUtil.stackStraceToString(elements), waitingHandle.getCoroBaseInfo().jobName));
                        }
                    }
                }
                throw new TimeoutException(info);
            }

            if (currentHandle.getParkInfo().isInterrupted()) {
                String info = "None waitingHandle";
                if (waitingHandle != null) {
                    StringBuffer sb = new StringBuffer();
                    info = NKStringFormater.format("untime park timeout, enQueueTime: {}, startruntime: {}, coroid: {}, coroname: {}, parktimeout: {}, timeoutMillisecs: {} chain:{} detail: {}",
                            waitingHandle.getCoroStatInfo().getEnQueueTime(),
                            waitingHandle.getCoroStatInfo().getStartRunTime(),
                            waitingHandle.getCoroBaseInfo().coroutineId(),
                            waitingHandle.coroutineName(),
                            waitingHandle.getParkInfo().getParkTimeout(),
                            timeoutMillisecs,
                            sb,
                            waitingHandle.getCoroBaseInfo().getJobName());
                }
                currentHandle.getParkInfo().setInterrupted(false);
                throw new UntimedCoroInterruptedException(info);
            }
        } finally {
            currentHandle.getParkInfo().finiUnpark();
        }
    }

    protected void unparkOp(CoroHandle handle, Object obj) {
        Object parkVerifyObj = handle.getParkInfo().parkVerifyObj;
        if (parkVerifyObj != obj) {
            return;
        }
        if (handle.getCoroBaseInfo().status == CoroHandle.CoroJobStatus.PARKED) {
            if (handle.getCoroWorker() != null) {
                handle.getCoroWorker().addReadyJobList();
            }
        } else {
            if (handle.getParkInfo().isTimeout()) {
                return;
            }
            try {
                throw new CoroExecutionException("unparkop status fail " + handle.getCoroBaseInfo().status + " " + handle.getCoroBaseInfo().coroHandleId());
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
    }

    protected void realUnpark(CoroHandle handle, Object obj) {
        if (CoroutineMgr.getInstance().getCurrentContainer() != handle.coroutineContainer) {
            handle.coroutineContainer.runCoroSchOpMultiThread(new RealUnpark_17157(obj, handle));
        } else {
            unparkOp(handle, obj);
        }
    }

    private final class RealUnpark_17157 implements Runnable {
        private final Object obj;
        private final CoroHandle handle;

        public RealUnpark_17157(Object obj, CoroHandle handle) {
            this.obj = obj;
            this.handle = handle;
        }

        @Override
        public void run() {
            unparkOp(handle, obj);
        }
    }

    public Object get(CoroHandle handle, long time) throws TimeoutException {
        handle.setNeedRecordException(false);
        try {
            if (!handle.isFini()) {
                CoroutineContainer coroutineContainer = handle.getContainer();
                coroutineContainer.notifyThread();
                if (CoroHandle.current() == null) {
                    handle.threadWait(time);
                } else {
                    try {
                        CoroutineMgr.getInstance().realPark(null, time, handle, (time > 0));
                    } catch (UntimedCoroInterruptedException e) {
                        if (time > 0) {
                            LOGGER.error("", e);
                        } else {
                            throw new CoroExecutionException(e);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            handle.setNeedRecordException(true);
            throw e;
        }
        handle.throwTrueException();
        return handle.getCoroBaseInfo().result;
    }

    public final CoroHandle<?> current() {
        CoroutineContainer container = getCurrentContainer();
        if (container != null) {
            return container.current();
        }
        return null;
    }

    final void proc() {
        if (DateUtils.currentTimeMillis() - lastProcTime >= 60_000) {// 输出日志
            lastProcTime = DateUtils.currentTimeMillis();
        }
        if (DateUtils.currentTimeMillis() - checkUntimeCoro >= 30_000) {
            checkUntimeCoro = DateUtils.currentTimeMillis();
            coroContainerArray.forEach(CoroutineMgr::proc_25390);
        }
    }

    private static void proc_25390(CoroutineContainer entry) {
        try {
            entry.runCoroSchOpMultiThread(entry::checkUntimedPark);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    final void preStop() {
        isStop = true;
        setStopStatus();
    }

    private void doStopStatus() {
        for (CoroutineContainer entry : coroContainerArray) {
            entry.stopProc();
        }
    }

    private void setStopStatus() {
        doStopStatus();
    }

    private void updateStopStatus() {
        if (isStop && (DateUtils.currentTimeMillis() - lastProcTime >= 100)) {
            doStopStatus();
        }
    }

    final void stopProc() {
        if (canStop()) {
            return;
        }
        updateStopStatus();
    }

    final void shutdown() {
        shutDownTime = DateUtils.currentTimeMillis();
        for (CoroutineContainer entry : coroContainerArray) {
            entry.setIsRunningFalse();
        }
        for (CoroutineContainer entry : coroContainerArray) {
            if (DateUtils.currentTimeMillis() - shutDownTime < 5000) {
                try {
                    entry.coroThread.join(5000);
                    entry.coroThread.interrupt();
                } catch (Exception e) {
                    LOGGER.error("join exception ", e);
                }
            }
            if (DateUtils.currentTimeMillis() - shutDownTime >= 5000) {
                LOGGER.error(NKStringFormater.format("force exit {}", entry.coroThread.getName()));
            }
        }
    }

    final void registerCoroContainer(CoroutineContainer container) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(NKStringFormater.format("register {} {}", container.containerId, container));
        }
        coroContainerArray.add(container);
    }

    boolean canStop() {
        for (CoroutineContainer entry : coroContainerArray) {
            try {
                if (!entry.canStop()) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    public CoroutineContainer getCurrentContainer() {
        Thread thread = Thread.currentThread();
        if (thread instanceof CoroutineContainer.CoroMainThread) {
            return ((CoroutineContainer.CoroMainThread) thread).getContainer();
        }
        return null;
    }

    public long genAsyncId() {
        long timeFlag = (DateUtils.currentTimeMillis() / 1000);
        return (timeFlag << 32) + asyncIdCnt.getAndAdd(1);
    }

    @SuppressWarnings("unused")
    private long getUniqueNumber() {
        return nextUniqueNumber.incrementAndGet();
    }
}

