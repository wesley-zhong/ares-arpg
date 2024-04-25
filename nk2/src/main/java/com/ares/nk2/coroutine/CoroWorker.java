package com.ares.nk2.coroutine;

import com.ares.nk2.container.IntrusiveList;
import com.ares.nk2.tool.DateUtils;
import com.ares.nk2.tool.StringFormatter;
import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class CoroWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoroWorker.class);

    private static AtomicInteger workerCnt = new AtomicInteger(0);
    private final long workerId;
    private int workJobCnt = 0;
    private static final int WORK_ID_OFFSET = 1000;

    private CoroHandle currJob = null;

    private final CoroutineContainer coroutineContainer; // 所归属的container线程
    public boolean isWorkerExit = false;
    private long wakeTime = 0;
    private String coroName;
    final public IntrusiveList.IntrusiveElem<CoroWorker> listElem;

    final Object continuation;

    class CoroContiuation extends Continuation {
        String name;

        String getName() {
            return name;
        }

        CoroContiuation(ContinuationScope scope, Runnable target, String name) {
            super(scope, target);
            this.name = name;
        }

        CoroWorker getWorker() {
            return CoroWorker.this;
        }

        @Override
        protected void onPinned(Pinned reason) {
            try {
                super.onPinned(reason);
            } catch (Exception e) {
                LOGGER.error("", e);
                if (getJob() != null) {
                    getJob().onPinned(reason.toString());
                }
                throw e;
            }
        }
    }

    class ContinuationRunner implements Runnable {
        @Override
        public void run() {
            workerRun();
        }
    }

    static public int getWorkerCnt() {
        return workerCnt.get();
    }

    public int getWorkJobCnt() {
        return workJobCnt;
    }

    public CoroutineContainer getContainer() {
        return coroutineContainer;
    }


    public boolean isExit() {
        return isWorkerExit;
    }


    public CoroHandle getJob() {
        return currJob;
    }

    public void setJob(CoroHandle handle) {
        currJob = handle;
    }

    private void gardMultiThreadProblem() {
        if (CoroHandle.currentContainer() != coroutineContainer) {
            LOGGER.error("multi thread access", new Throwable());
        }
    }

    public void clearAllCoroList() {
        gardMultiThreadProblem();
        listElem.remove();
    }

    void addParkTimeoutList() {
        clearAllCoroList();
        coroutineContainer.addWaitParkTimeoutJobList(this);
    }

    void addUntimeList() {
        clearAllCoroList();
        coroutineContainer.sch.addUntimedList(this);
    }

    void addReadyJobList() {
        if (getJob().getCoroBaseInfo().getStatus() != CoroHandle.CoroJobStatus.READY) {
            clearAllCoroList();
            getJob().getCoroBaseInfo().setStatus(CoroHandle.CoroJobStatus.READY);
            coroutineContainer.sch.addReadyJob(this);
        } else {
            gardMultiThreadProblem();
            LOGGER.error(StringFormatter.format("addReadyJobList {}", getJob().getCoroBaseInfo().coroutineId()));
        }
    }

    CoroWorker(CoroutineContainer container, String name) {
        coroName = name;
        coroutineContainer = container;
        int myCnt = workerCnt.incrementAndGet();
        workerId = myCnt * WORK_ID_OFFSET + coroutineContainer.containerId * (long) Math.pow(10, String.valueOf(CoroutineConfig.getMaxPoolCnt()).length()) * WORK_ID_OFFSET;
        listElem = IntrusiveList.getElem(this);
        CoroHandle.getWorkerCnt().incrementAndGet();
        continuation = new CoroContiuation(container.scope, new ContinuationRunner(), container.getWorkerName());
    }

    public void increaseWorkerId() {
        ++workJobCnt;
        workJobCnt = workJobCnt % WORK_ID_OFFSET;
        getJob().getCoroBaseInfo().setCoroutineId(workerId + workJobCnt);
    }

    public void workerRun() {
        try {
            coroutineContainer.sch.onWorkerRun(this);
        } catch (Exception e) {
            LOGGER.error(StringFormatter.format("worker exit {}", coroName), e);
        } finally {
            isWorkerExit = true;
        }
    }

    public String coroutineName() {
        return coroName;
    }

    public void setWakeTime() {
        wakeTime = DateUtils.currentTimeMillis();
    }

    public void onResume() {
    }

    public void run() {
        ((Continuation) continuation).run();
    }

    public StackTraceElement[] getStackTrace() {
        return ((Continuation) continuation).getStackTrace();
    }
}
