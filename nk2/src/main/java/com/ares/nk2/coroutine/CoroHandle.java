package com.ares.nk2.coroutine;

import com.ares.nk2.coroutine.coroHandleInner.CoroBaseInfo;
import com.ares.nk2.coroutine.coroHandleInner.CoroStatInfo;
import com.ares.nk2.coroutine.coroHandleInner.ParkInfo;
import com.ares.nk2.coroutine.coroHandleInner.Section;
import com.ares.nk2.coroutine.exception.CoroExecutionException;
import com.ares.nk2.coroutine.exception.UntimedCoroInterruptedException;
import com.ares.nk2.tool.DateUtils;
import com.ares.nk2.tool.StringFormatter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class CoroHandle<V> {
    public enum RelationType {
        callJob,
        runJob,
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(CoroHandle.class);

    private static AtomicLong workerCnt = new AtomicLong(0);
    private static AtomicLong runWorkerCnt = new AtomicLong(0);
    final CoroutineContainer coroutineContainer;
    private final CoroExecutorService executorService;
    private final RelationType relationType;
    private final Callable<V> callable;
    public CoroBaseInfo<V> coroBaseInfo = null;
    public CoroHandle<?> waitForFiniHandle = null;
    public LinkedList<CoroHandle<?>> waiters = null;
    public CoroWorker bindWorker = null;
    CoroStatInfo coroStatInfo = null;
    ParkInfo parkInfo = null;
    boolean isInitFini = false;
    Section section = new Section(this);
    private volatile ConcurrentLinkedQueue<Thread> waitForMeThread = null;
    private volatile ConcurrentHashMap<Thread, Long> waitThreadTimeout = null;

    private NoAsyncCheck noAsyncCheck;

    long lastWarningTime = 0;
    boolean needParkCountWarning = true;

    public boolean checkCanWaring() {
        if (!needParkCountWarning) {
            return false;
        }
        if (DateUtils.currentTimeMillis() - lastWarningTime >= 60 * 1000L) {
            lastWarningTime = DateUtils.currentTimeMillis();
            return true;
        }
        return false;
    }

    public void setNeedParkCountWarning(boolean need) {
        needParkCountWarning = need;
    }

    public CoroHandle(String jobName, Callable<V> callable, CoroExecutorService executorService, RelationType relationType) {
        this.executorService = executorService;
        coroutineContainer = executorService.getContainer();
        this.callable = callable;
        this.relationType = relationType;
        initCoroHandle();
        getCoroBaseInfo().init(jobName);
    }

    static <V> CoroHandle<V> newInstance(
            Callable<V> callable,
            CoroExecutorService jobType,
            RelationType relationType,
            String jobname) {
        CoroHandle coroHandle = new CoroHandle<>(jobname, callable, jobType, relationType);
        coroHandle.initFini();
        return coroHandle;
    }

    public static AtomicLong getWorkerCnt() {
        return workerCnt;
    }

    public static AtomicLong getRunWorkerCnt() {
        return runWorkerCnt;
    }

    public static void init() {
        CoroutineConfig.generateConfig();
    }

    public static boolean isInited() {
        return (CoroutineMgr.getInstance() != null);
    }

    public static void proc() {
        CoroutineMgr.getInstance().proc();
    }

    public static CoroHandle<?> current() {
        if (CoroutineMgr.getInstance() == null) {
            return null;
        }
        return CoroutineMgr.getInstance().current();
    }

    public static CoroExecutorService currentExectorService() {
        if (CoroutineMgr.getInstance() == null) {
            return null;
        }
        CoroHandle coroHandle = CoroutineMgr.getInstance().current();
        if (coroHandle == null) {
            return null;
        }
        return coroHandle.getExecutorService();
    }

    @SuppressWarnings({"unused", "unchecked"})
    public static <V> V current(Class<V> vClass) {
        if (CoroutineMgr.getInstance() == null) {
            return null;
        }
        return (V) CoroutineMgr.getInstance().current();
    }

    public static CoroutineContainer currentContainer() {
        return CoroutineMgr.getInstance().getCurrentContainer();
    }

    public static void yield() {
        CoroutineMgr.getInstance().yield();
    }

    public static void park() throws UntimedCoroInterruptedException {
        CoroHandle<?> currentHandle = current();
        CoroutineMgr.getInstance().park(currentHandle);
    }

    public static void park(Object parkVerifyObj) throws UntimedCoroInterruptedException {
        CoroutineMgr.getInstance().park(parkVerifyObj);
    }

    public static void park(long ms) throws TimeoutException {
        CoroHandle<?> currentHandle = current();
        CoroutineMgr.getInstance().park(currentHandle, ms);
    }

    public static void park(Object parkVerifyObj, long ms) throws TimeoutException {
        CoroutineMgr.getInstance().park(parkVerifyObj, ms);
    }

    public static void sleep(long ms) {
        long endMillis = DateUtils.currentTimeMillis() + ms;
        long diff = ms;
        do {
            try {
                CoroutineMgr.getInstance().sleep(diff);
                return;
            } catch (Exception e) {
                diff = endMillis - DateUtils.currentTimeMillis();
            }
        } while (diff > 1);
    }

    public static void preStop() {
        CoroutineMgr.getInstance().preStop();
    }

    public static boolean canStop() {
        return CoroutineMgr.getInstance().canStop();
    }

    public static void stopProc() {
        CoroutineMgr.getInstance().stopProc();
    }

    public static void shutdown() {
        CoroutineMgr.getInstance().shutdown();
    }

    public CoroutineContainer getContainer() {
        return coroutineContainer;
    }

    private void gardMultiThreadProblem() {
        assert CoroHandle.currentContainer() == coroutineContainer : "multi thread access";
    }

    public ParkInfo getParkInfo() {
        return parkInfo;
    }

    protected void reset() {
        waitForFiniHandle = null;
        waiters = null;
        waitForMeThread = null;
        waitThreadTimeout = null;
        initCoroHandle();
        initFini();
    }

    public CoroStatInfo getCoroStatInfo() {
        return coroStatInfo == null ? new CoroStatInfo(this) : coroStatInfo;
    }

    public CoroWorker getCoroWorker() {
        return bindWorker;
    }

    public void setCoroWorker(CoroWorker worker) {
        bindWorker = worker;
    }

    public StackTraceElement[] getStackTrace() {
        if (getCoroWorker() == null) {
            LOGGER.error("get stack trace from no worker handle");
            return null;
        }
        return getCoroWorker().getStackTrace();
    }

    void initCoroHandle() {
        isInitFini = false;
        coroStatInfo = new CoroStatInfo(this);
        if (coroBaseInfo != null) {
            coroBaseInfo.reset();
        } else {
            coroBaseInfo = new CoroBaseInfo(this);
        }
        parkInfo = new ParkInfo(this);
    }

    public void initFini() {
        isInitFini = true;
        sectionRecord(CoroHandleAction.CHA_INIT, null);
    }

    public void sectionRecord(int action, Throwable exception) {
        long durationNs = section.record(action);

        switch (action) {
            case CoroHandleAction.CHA_INIT:
                getCoroBaseInfo().setStatus(CoroJobStatus.READY);
                CoroHandle<?> curr = CoroHandle.current();
                if (curr != null) {
                    if (coroBaseInfo.getReentrantTraceId() == 0) {
                        coroBaseInfo.setReentrantTraceId(curr.getCoroBaseInfo().getReentrantTraceId());
                        coroBaseInfo.setCreateReentrantTraceId(coroBaseInfo.reentrantTraceId);
                    }
                } else {
                    getCoroBaseInfo().resetTraceId();
                }
                if (coroBaseInfo.createThreadName.isEmpty()) {
                    coroBaseInfo.createThreadName = Thread.currentThread().getName();
                }
                onInit(0);
                break;
            case CoroHandleAction.CHA_START:
                if (!isInitFini) {
                    throw new CoroExecutionException("coroHandle cannot start without call CoroHandler.initFini()");
                }
                getCoroBaseInfo().setStatus(CoroJobStatus.RUNNING);
                getCoroStatInfo().startRunTime = DateUtils.currentTimeMillis();
                coroutineContainer.setJobTypeCostCreateToRun(getExecutorService(), getCoroStatInfo().startRunTime - getCoroStatInfo().enQueueTime);
                coroutineContainer.increaseRunningJob(getExecutorService());
                onStart((int) CoroStatInfo.nsToMs(durationNs));
                break;
            case CoroHandleAction.CHA_FINI:
                getCoroStatInfo().addCpuCostNs(durationNs);
                getCoroStatInfo().addTotalCostNs(durationNs);
                if (exception != null) {
                    getCoroBaseInfo().setStatus(CoroJobStatus.FINI_EXCEPT);
                    onException(exception);
                } else {
                    getCoroBaseInfo().setStatus(CoroJobStatus.FINI_SUCC);
                    onComplete();
                }
                break;
            case CoroHandleAction.CHA_FINALLY:
                getCoroStatInfo().addCpuCostNs(durationNs);
                getCoroStatInfo().addTotalCostNs(durationNs);
                onFinally();
                break;
            default:
                break;
        }
    }


    @SuppressWarnings("DuplicateBranchesInSwitch")
    public void sectionStart(int action) {
        long durationNs = section.start(action);
        if (durationNs < 0) {
            return;
        }
        switch (action) {
            case CoroHandleAction.CHA_PARK:
                getCoroStatInfo().addCpuCostNs(durationNs);
                getCoroStatInfo().addTotalCostNs(durationNs);
                getCoroBaseInfo().setStatus(CoroJobStatus.PARKED);
                ++getCoroStatInfo().parkCount;
                onPark();
                break;
            case CoroHandleAction.CHA_RESUME:
                getCoroStatInfo().addTotalCostNs(durationNs);
                getCoroBaseInfo().setStatus(CoroJobStatus.RUNNING);
                onResume();
                break;
            case CoroHandleAction.CHA_INIT:
                throw new CoroExecutionException("illagal call");
            case CoroHandleAction.CHA_START:
                throw new CoroExecutionException("illagal call");
            case CoroHandleAction.CHA_FINI:
                throw new CoroExecutionException("illagal call");
            case CoroHandleAction.CHA_FINALLY:
                throw new CoroExecutionException("illagal call");
            default:
                throw new CoroExecutionException("illagal call");
        }
    }

    public void sectionEnd(int action, Throwable exception) {
        long durationNs = section.end(action);
        if (durationNs < 0) {
            return;
        }

        getCoroStatInfo().addTotalCostNs(durationNs);
        switch (action) {
            case CoroHandleAction.CHA_INIT:
                throw new CoroExecutionException("illagal call");
            case CoroHandleAction.CHA_START:
                throw new CoroExecutionException("illagal call");
            case CoroHandleAction.CHA_PARK:
                onParkEnd((int) CoroStatInfo.nsToMs(durationNs));
                break;
            case CoroHandleAction.CHA_RESUME:
                getCoroStatInfo().addCpuCostNs(durationNs);
                onResumeEnd((int) CoroStatInfo.nsToMs(durationNs));
                break;
            case CoroHandleAction.CHA_FINI:
                throw new CoroExecutionException("illagal call");
            case CoroHandleAction.CHA_FINALLY:
                throw new CoroExecutionException("illagal call");
            default:
                throw new CoroExecutionException("illagal call");
        }
    }

    protected void onInit(int duration) {
        if (getRelationType() == RelationType.callJob) {
            setNeedRecordException(false);
        } else {
            getCoroBaseInfo().resetTraceId();
            setNeedRecordException(true);
        }
    }

    protected void onStart(int duration) {
        executorService.getCoroJobQueue().getCoroStat().startCoroCount++;
    }

    protected void onPark() {
        executorService.getCoroJobQueue().getCoroStat().parkCoroCount++;
        if (noAsyncCheck != null) {
            noAsyncCheck.onPark();
        }
    }

    protected void onParkEnd(int duration) {

    }

    protected void onResume() {
        executorService.getCoroJobQueue().getCoroStat().resumeCoroCount++;
    }

    protected void onResumeEnd(int duration) {

    }

    protected void onComplete() {
    }

    protected void onException(Throwable result) {
        if (result != null) {
            if (relationType == RelationType.runJob) {
                LOGGER.error(StringFormatter.format("corohandle_runjob_exception_log, coroHandleId: {}", getCoroBaseInfo().coroHandleId()), result);
            }
        }
    }

    protected void onFinally() {

    }

    private void start() {
        sectionRecord(CoroHandleAction.CHA_START, null);
    }

    public CoroBaseInfo getCoroBaseInfo() {
        return coroBaseInfo;
    }

    private boolean needRecordException = true;

    public void setNeedRecordException(boolean needRecordException) {
        this.needRecordException = needRecordException;
    }

    public boolean getNeedRecordException() {
        return needRecordException;
    }

    private void exception(Throwable t) {
        coroBaseInfo.callException = t;
        sectionRecord(CoroHandleAction.CHA_FINI, t);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(StringFormatter.format("coro create excep {}", t.getMessage()));
        }
    }

    protected boolean needLogException(Throwable t) {
        return true;
    }

    public void resume() {
        getParkInfo().setUnPark();
    }

    private void complete() {
        sectionRecord(CoroHandleAction.CHA_FINI, null);
    }

    protected void coroFinally() {
        coroutineContainer.setFiniJob(getExecutorService());
        long deltaTime = DateUtils.currentTimeMillis() - getCoroStatInfo().startRunTime;
        coroutineContainer.setJobTypeCostRunToFini(getExecutorService(), deltaTime);
        coroutineContainer.decreaseRunningJob(getExecutorService());
        sectionRecord(CoroHandleAction.CHA_FINALLY, null);
    }

    private void setWaitThreadTimeout(Thread th, long timeout) {
        if (waitThreadTimeout == null) {
            synchronized (this) {
                if (waitThreadTimeout == null) {
                    waitThreadTimeout = new ConcurrentHashMap<>();
                }
            }
        }
        if (timeout > 0) {
            waitThreadTimeout.put(th, timeout + DateUtils.currentTimeMillis());
        } else {
            waitThreadTimeout.put(th, timeout);
        }
    }

    private void removeWaitForMeThread(Thread th) {
        if (waitThreadTimeout != null) {
            waitThreadTimeout.remove(th);
        }
    }

    private boolean isThreadWaitTimeout(Thread th) {
        if (waitThreadTimeout == null) {
            return false;
        }
        Long timeout = waitThreadTimeout.getOrDefault(th, -1L);
        if (timeout > 0) {
            return timeout <= DateUtils.currentTimeMillis();
        } else {
            return timeout < 0;
        }
    }

    private void addWaitForMeThread(Thread th) {
        if (!isFini()) {
            if (waitForMeThread == null) {
                synchronized (this) {
                    if (waitForMeThread == null) {
                        waitForMeThread = new ConcurrentLinkedQueue<>();
                    }
                }
            }

            waitForMeThread.offer(th);
        }
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    public void run() {
        try {
            start();
            getCoroBaseInfo().result = callable.call();
            complete();
        } catch (RuntimeException e) {
            exception(e);
        } catch (Exception e) {
            exception(e);
        } catch (Throwable e) {
            unCaughtedError(e);
            exception(e);
        } finally {
            coroFinally();
        }
    }

    public CoroExecutorService getExecutorService() {
        return executorService;
    }

    private void realUnpark(Object obj) {
        CoroutineMgr.getInstance().realUnpark(this, obj);
    }

    public boolean isFini() {
        return getCoroBaseInfo().status == CoroJobStatus.FINI_EXCEPT || getCoroBaseInfo().status == CoroJobStatus.FINI_SUCC;
    }

    public final String coroutineName() {
        if (getCoroWorker() == null) {
            return "NoCoro";
        }
        return getCoroWorker().coroutineName();
    }

    void threadWait(long timeout) throws TimeoutException {
        if (!isFini()) {
            addWaitForMeThread(Thread.currentThread());
            setWaitThreadTimeout(Thread.currentThread(), timeout);
        }

        synchronized (Thread.currentThread()) {
            while (!isFini()) {
                if (!isThreadWaitTimeout(Thread.currentThread())) {
                    try {
                        Thread.currentThread().wait(timeout);
                    } catch (InterruptedException e) {
                        LOGGER.error("thread interrupted: ", e);
                    }
                } else {
                    throw new TimeoutException("thread wait timeout");
                }
            }
        }

        removeWaitForMeThread(Thread.currentThread());
    }

    void throwTrueException() {
        if (CoroJobStatus.FINI_EXCEPT == getCoroBaseInfo().status) {
            if (getCoroBaseInfo().callException != null) {
                if (getCoroBaseInfo().callException instanceof RuntimeException) {
                    throw (RuntimeException) getCoroBaseInfo().callException;
                } else {
                    throw new CoroExecutionException(getCoroBaseInfo().callException);
                }
            }
        }
    }

    public final V get() throws CoroExecutionException {
        try {
            return get(CoroutineMgr.DEFAULT_PARK_TIMEOUT_MS);
        } catch (TimeoutException e) {
            throw new CoroExecutionException(e.getMessage(), e);
        }
    }

    public final V get(long time) throws TimeoutException {
        long leftTimeMs = time + getCoroStatInfo().getCreateTime() - DateUtils.currentTimeMillis();
        if (leftTimeMs <= 0) {
            leftTimeMs = 1;
        }
        return (V) CoroutineMgr.getInstance().get(this, leftTimeMs);
    }

    public final void join() throws CoroExecutionException {
        get();
    }

    public final void join(long time) throws CoroExecutionException, TimeoutException {
        get(time);
    }

    void awakeByWaitingFinished(CoroHandle<?> waitingHandle) {
        if (CoroutineMgr.getInstance().getCurrentContainer() == coroutineContainer) {
            if (getCoroWorker() == null) {
                return;
            }
            if (waitForFiniHandle != waitingHandle) {
                return;
            }
            getCoroWorker().addReadyJobList();

        } else {
            coroutineContainer.runCoroSchOpMultiThread(()->{
                        if (getCoroWorker() == null) {
                            return;
                        }
                        if (waitForFiniHandle != waitingHandle) {
                            return;
                        }
                        getCoroWorker().addReadyJobList();
                    });
        }
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void awakeAllWaiters() {
        while (waiters != null && !waiters.isEmpty()) {
            CoroHandle<?> handle = waiters.poll();
            if (handle != null) {
                handle.awakeByWaitingFinished(this);
            }
        }
        while (waitForMeThread != null && !waitForMeThread.isEmpty()) {
            Thread th = waitForMeThread.poll();
            if (th != null) {
                synchronized (th) {
                    // LOGGER.error("notify thread {} {}", th, this);
                    th.notify();
                }
            }
        }
        if (waitThreadTimeout != null) {
            waitThreadTimeout.clear();
        }
    }

    private void addToWaiterList(CoroHandle<?> handle) {
        gardMultiThreadProblem();
        if (waiters == null) {
            waiters = new LinkedList<>();
        }

        if (waiters.size() >= 1) {
            LOGGER.error(StringFormatter.format("waitForMeJobList.size(): {} >= 1", waiters.size()));
        }

        waiters.offer(handle);    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean addWaiter(CoroHandle<?> handle) {
        if (!isFini()) {
            if (CoroutineMgr.getInstance().getCurrentContainer() != coroutineContainer) {
                coroutineContainer.runCoroSchOpMultiThread(()->addWaiter(handle));
            } else {
                addToWaiterList(handle);
            }
            return false;
        }
        handle.awakeByWaitingFinished(this);
        return true;
    }

    public void onPinned(String reason) {
        LOGGER.error(StringFormatter.format("fatalError coroutine pinned on {} of {} on thread {} reentranceid {}",
                coroutineName(), getCoroBaseInfo().getJobName(), coroutineContainer.containerName, getCoroBaseInfo().getReentrantTraceId()));
    }

    public boolean needRecord(Throwable t) {
        return true;
    }

    public void unCaughtedError(Throwable t) {
        LOGGER.error("fatalError exception:\n", t);
    }

    public final void unpark() {
        realUnpark(this);
    }

    public final void unpark(Object parkVerifyObj) {
        realUnpark(parkVerifyObj);
    }


    public V getResult() {
        return coroBaseInfo.result;
    }

    public Throwable getException() {
        return getCoroBaseInfo().callException;
    }

    public static long getCoroYieldNs() {
        CoroHandle<?> coroHandle = current();
        if (coroHandle == null) {
            return 0L;
        }
        CoroStatInfo coroStatInfo = coroHandle.getCoroStatInfo();
        return coroStatInfo.getTotalCostNs() - coroStatInfo.getCpuCostNs();
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public void setNoAsyncCheck(NoAsyncCheck noAsyncCheck) {
        this.noAsyncCheck = noAsyncCheck;
    }

    public static class CoroJobStatus {
        public static final int INIT = 0;
        public static final int READY = 1;
        public static final int RUNNING = 2;
        public static final int PARKED = 3;
        public static final int FINI_SUCC = 4;
        public static final int FINI_EXCEPT = 5;
    }

    public static class CoroHandleAction {
        public static final int CHA_DEFAULT = 0;
        public static final int CHA_INIT = 1;
        public static final int CHA_START = 2;
        public static final int CHA_PARK = 3;
        public static final int CHA_RESUME = 4;
        public static final int CHA_FINI = 5;
        public static final int CHA_FINALLY = 6;
    }

}
