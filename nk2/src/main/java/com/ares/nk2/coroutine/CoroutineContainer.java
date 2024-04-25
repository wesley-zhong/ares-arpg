package com.ares.nk2.coroutine;

import com.ares.nk2.coroutine.exception.CoroCheckedException;
import com.ares.nk2.coroutine.exception.CoroExecutionException;
import com.ares.nk2.tool.StringFormatter;
import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class CoroutineContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoroutineContainer.class);
    private static final AtomicInteger nextContainerId = new AtomicInteger(-1);
    final int containerId;
    final String containerName;
    public CoroutineTimeWheel coroutineTimeWheel = new CoroutineTimeWheel();
    public volatile boolean isRunning = true;
    protected Thread coroThread = null;
    protected CoroutineSch sch;
    private volatile CopyOnWriteArrayList<CoroJobQueue> jobQueues = new CopyOnWriteArrayList<>();
    private volatile boolean isCoroThreadParking = false;
    private AtomicInteger currentRunningCnt = new AtomicInteger(0);
    ContinuationScope scope;

    public CoroutineContainer(String threadName) {
        containerName = threadName;
        containerId = nextContainerId.incrementAndGet();
        sch = new CoroutineSch(this);
        CoroutineMgr.getInstance().registerCoroContainer(this);
        scope = new ContinuationScope(threadName + "Scope");
        initMainThread(threadName);
    }

    public String getContainerName() {
        return containerName;
    }

    CopyOnWriteArrayList<CoroJobQueue> getJobQueues() {
        return jobQueues;
    }

    public CoroutineSch getSch() {
        return sch;
    }

    void runCoroSchOpMultiThread(Runnable run) {
        sch.runCoroSchOpMultiThread(run);
    }

    <V> V callCoroSchOpMultiThread(Callable<V> call) {
        if (call == null) {
            throw new CoroExecutionException("callable is null");
        }

        CoroutineAsync<V, RuntimeException> async = new CoroutineAsync<>();
        sch.runCoroSchOpMultiThread(new CallCoroSchOpMultiThread_4043(call, async));
        try {
            return async.syncGetResult(5000);
        } catch (TimeoutException e) {
            LOGGER.error("callCoroSchOpMultiThread timeout.", e);
            throw new CoroExecutionException(e.getMessage(), e.getCause());
        }
    }

    private final class CallCoroSchOpMultiThread_4043<V> implements Runnable {
        private final Callable<V> call;
        private final CoroutineAsync<V, RuntimeException> async;

        public CallCoroSchOpMultiThread_4043(Callable<V> call, CoroutineAsync<V, RuntimeException> async) {
            this.call = call;
            this.async = async;
        }

        @Override
        public void run() {
            try {
                V v = call.call();
                async.completed(v);
            } catch (Exception e) {
                async.fail(e);
            }
        }
    }

    public int getContainerId() {
        return containerId;
    }

    void setJobTypeCostCreateToRun(CoroExecutorService jobtype, long cost) {
        CoroJobQueue cjq = jobtype.getCoroJobQueue();
        if (cjq != null) {
            cjq.getCoroStat().totalCostCreateToRun += cost;
            cjq.addInQueueCostMs(cost);
        }
    }

    void setJobTypeCostRunToFini(CoroExecutorService jobtype, long cost) {
        CoroJobQueue cjq = jobtype.getCoroJobQueue();
        if (cjq != null) {
            cjq.getCoroStat().totalCostRunToFini += cost;
        }
    }

    void setFiniJob(CoroExecutorService jt) {
        CoroJobQueue cjq = jt.getCoroJobQueue();
        if (cjq != null) {
            ++cjq.getCoroStat().totalFiniJobCnt;
        }
    }

    void increaseRunningJob(CoroExecutorService type) {
        CoroJobQueue jq = type.getCoroJobQueue();
        if (jq != null) {
            jq.increaseRunningCnt();
        }
    }

    void decreaseRunningJob(CoroExecutorService type) {
        CoroJobQueue jq = type.getCoroJobQueue();
        if (jq != null) {
            jq.decreaseRuningCnt();
        }
    }

    static void uncaughtException(Thread t, Throwable e) {
        LOGGER.error("fatalError\n", e);
    }

    class CoroMainThread extends Thread {
        CoroMainThread(String name) {
            super(name);
            setUncaughtExceptionHandler(CoroutineContainer::uncaughtException);
        }

        CoroutineContainer getContainer() {
            return CoroutineContainer.this;
        }

        @Override
        public void run() {
            getVtMain().run();
        }
    }

    public CoroWorker initMainThread(String name){
        coroThread = new CoroMainThread(name);
        return null;
    }

    public Runnable getVtMain() {
        return new MainVtJob();
    }

    public String getMainCoroName() {
        return containerName + "CoroMain";
    }

    public String getWorkerName() {
        return containerName + CoroWorker.getWorkerCnt();
    }

    public boolean isRunning() {
        return isRunning;
    }

    void setIsRunningFalse() {
        isRunning = false;
    }

    private void stopStatistics() {
        int tmpCurrentRunningCnt = 0;
        for (CoroJobQueue queue : getJobQueues()) {
            if (queue == null) {
                continue;
            }
            if (queue.checkEmptyBeforeStop && queue.runningJobCnt.get() > 0) {
                tmpCurrentRunningCnt += queue.runningJobCnt.get();
            }
        }
        currentRunningCnt.set(tmpCurrentRunningCnt);
    }

    final boolean canStop() {
        return currentRunningCnt.get() <= 0;
    }

    int getAllJobCnt() {
        int allJobCnt = 0;
        for (CoroJobQueue queue : getJobQueues()) {
            allJobCnt += queue.getJobCnt();
        }
        return allJobCnt;
    }

    void addWaitParkTimeoutJobList(CoroWorker worker) {
        coroutineTimeWheel.insert(worker);
    }

    final void notifyThread() {
        if (isCoroThreadParking) {
            LockSupport.unpark(coroThread);
        }
    }

    public final void waitThread(long ms) {
        try {
            isCoroThreadParking = true;
            LockSupport.parkNanos(coroThread, ms * 1000000);
        } finally {
            isCoroThreadParking = false;
        }
    }

    private Thread getCoroThread() {
        return coroThread;
    }

    public long getCoroThreadId() {
        return coroThread.getId();
    }

    public final void startContainer() {
        LOGGER.error(StringFormatter.format("startContainer coroThread state:{}", coroThread.getState()));
        coroThread.start();
    }

    final void checkUntimedPark() {
        sch.checkUntimedPark();
    }

    final void stopProc() {
        stopStatistics();
    }

    CoroJobQueue createJobQueue(String qName, int concurrentJobCnt, int maxJobOneCycle, boolean runwhenstopping) {
        if (CoroutineMgr.getInstance().isStop) {
            LOGGER.error("cannot create jobqueue when isStop");
            return null;
        }

        CoroJobQueue queue = new CoroJobQueue(qName, concurrentJobCnt, maxJobOneCycle, runwhenstopping, this);
        jobQueues.add(queue);
        return queue;
    }

    protected <V> CoroHandle<V> offerJob(CoroHandle<V> coroHandle) throws CoroCheckedException {
        CoroJobQueue queue = coroHandle.getExecutorService().getCoroJobQueue();
        if (queue == null) {
            throw new CoroCheckedException("CoroQueueTypeNotExist");
        }

        coroHandle.getCoroStatInfo().setEnQueueTime();

        if (!queue.offerJob(coroHandle)) {
            throw new CoroCheckedException("CoroQueueWaitFull");
        }
        sch.onOfferJob();

        return coroHandle;
    }

    public void yieldTo(CoroHandle handle) {
        if (handle.getCoroBaseInfo().getStatus() == CoroHandle.CoroJobStatus.READY) {
            assert handle.getCoroWorker() != null : "yield to null worker";
            assert !handle.getCoroWorker().isExit() : "yield to exit worker";
            handle.resume();
            handle.getCoroWorker().onResume();
            yieldTo(handle.getCoroWorker());
        }
    }

    public void coroInit() {
        int poolcnt = CoroutineConfig.getPoolCnt();
        for (int i = 0; i < poolcnt; ++i) {
            createNewWorker();
        }
    }

    public void yieldTo(CoroWorker worker) {
        ((Continuation) worker.continuation).run();
    }

    public void yieldToMain() {
        Continuation.yield(scope);
    }

    public CoroHandle current() {
        Object continuation = Continuation.getCurrentContinuation(scope);
        if (continuation instanceof CoroWorker.CoroContiuation) {
            CoroWorker worker = ((CoroWorker.CoroContiuation) continuation).getWorker();
            return worker.getJob();
        }
        return null;
    }

    public CoroWorker createNewWorker() {
//        if (CoroHandle.getRunWorkerCnt().get() >= CoroutineConfig.getMaxPoolCnt()) {
//            throw new RuntimeException(StringFormatter.format("runWorkerCnt {} >= maxPoolCnt {}. can't create more worker"
//                    , CoroHandle.getRunWorkerCnt().get(), CoroutineConfig.getMaxPoolCnt()));
//        }

        CoroWorker worker = new CoroWorker(this, this.getWorkerName());
        return worker;
    }

    public class MainVtJob implements Runnable {

        MainVtJob() {

        }

        @Override
        public void run() {
            LOGGER.error(StringFormatter.format("container name {} {}", getMainCoroName(), Thread.currentThread().getName()));
            try {
                coroInit();
                while (isRunning) {
                    try {
                        sch.coroSchedule();
                    } catch (Throwable e) {
                        LOGGER.error("corocontainer execption:", e);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
            LOGGER.error(StringFormatter.format("coroThread {} stopped", getCoroThread().getName()));
        }

    }

}
