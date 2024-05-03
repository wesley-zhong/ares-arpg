package com.ares.nk2.coroutine;

import com.ares.nk2.container.IntrusiveList;
import com.ares.nk2.tool.DateUtils;
import com.ares.nk2.tool.NKStringFormater;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class CoroutineSch {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoroutineSch.class);
    protected final CoroutineContainer container;
    protected IntrusiveList<CoroWorker> readyJobList = new IntrusiveList<>("readyJobList");
    protected int newJobPerCnt = 0;
    protected IntrusiveList<CoroWorker> untimedParkList = new IntrusiveList<>("untimedParkList");
    protected long lastHasToMainCheckTimeMs = 0;
    protected int idleProcCnt = 0;
    protected int sleepTime = 1;
    protected ConcurrentLinkedQueue<Runnable> coroSchOpQueue = new ConcurrentLinkedQueue<Runnable>();
    private int jobTypePollIdx = 0;
    protected AtomicLong jobCounter = new AtomicLong(0);

    public CoroutineSch(CoroutineContainer pContainer) {
        container = pContainer;
    }

    public IntrusiveList<CoroWorker> getReadyJobList() {
        return readyJobList;
    }

    public int getReadyJobCnt() {
        return readyJobList.size();
    }

    public long newJobCnt() {
        if (jobCounter.get() > CoroutineConfig.MaxStartNewJobOneCycleInCoroSchedule) {
            return CoroutineConfig.MaxStartNewJobOneCycleInCoroSchedule;
        }
        return jobCounter.get();
    }


    boolean needNotify() {
        if (jobCounter.get() >= CoroutineConfig.MaxStartNewJobOneCycleInCoroSchedule) {
            return true;
        }
        return false;
    }


    void runCoroSchOpMultiThread(Runnable run) {
        if (run != null) {
            coroSchOpQueue.offer(run);
            container.notifyThread();
        }
    }

    public int processCoroSchOp() {
        int cnt = 0;
        Runnable run = coroSchOpQueue.poll();
        while (run != null) {
            ++cnt;
            try {
                run.run();
            } catch (Throwable e) {
                LOGGER.error("", e);
            }
            run = coroSchOpQueue.poll();
        }
        return cnt;
    }

    void addReadyJob(CoroWorker worker) {
        worker.setWakeTime();
        readyJobList.insert(worker.listElem);
    }

    void addUntimedList(CoroWorker worker) {
        untimedParkList.insert(worker.listElem);
    }

    public CoroHandle<?> pollReadyJob() {
        processCoroSchOp();

        if (!readyJobList.isEmpty()) {
            CoroHandle<?> handle = readyJobList.first().getData().getJob();
            if (handle == null) {
                LOGGER.error(NKStringFormater.format("null handle found {}", readyJobList.first().getData().coroutineName()));
                readyJobList.first().remove();
            } else {
                handle.getCoroWorker().listElem.remove();
                if (handle.getCoroBaseInfo().getStatus() != CoroHandle.CoroJobStatus.READY) {
                    LOGGER.error(NKStringFormater.format("poll unready job {}", handle));
                    return pollReadyJob();
                }
            }
            return handle;
        }
        return null;
    }

    CoroHandle<?> pollReadyJobSkipSelf() {
        processCoroSchOp();

        if (!readyJobList.isEmpty()) {
            CoroHandle<?> handle = readyJobList.first().getData().getJob();
            if (handle == CoroHandle.current()) {
                return null;
            }
            readyJobList.delete(handle.getCoroWorker().listElem);

            if (handle.getCoroBaseInfo().getStatus() != CoroHandle.CoroJobStatus.READY) {
                LOGGER.error(NKStringFormater.format("poll unready job {}", handle));
                return pollReadyJob();
            }
            return handle;
        }
        return null;
    }

    public void coroSchedule() {
        while (container.isRunning) {
            int coroJobCnt = processCoroSchOp();
            int timeoutCnt = container.coroutineTimeWheel.proc();

            newJobPerCnt = 0;

            int pollReadyCnt = 0;
            int maxRunCnt = Math.min(getReadyJobCnt(), CoroutineConfig.MaxRunReadyJobOneCycleInCoroSchedule);
            for (int i = 0; i < maxRunCnt; ++i) {
                CoroHandle handle = pollReadyJob();
                if (handle != null) {
                    ++pollReadyCnt;
                    container.yieldTo(handle);
                } else {
                    break;
                }
            }

            lastHasToMainCheckTimeMs = DateUtils.currentTimeMillis();
            long curNewJobCnt = newJobCnt();

            while (newJobPerCnt < curNewJobCnt) {
                CoroWorker worker = container.createNewWorker();
                if (worker != null) {
                    long lastNewJobPerCnt = newJobPerCnt;
                    container.yieldTo(worker);
                    if (newJobPerCnt <= lastNewJobPerCnt) {
                        break;
                    }
                    continue;
                }
                break;
            }
            coroJobCnt = coroJobCnt <= 0 ? processCoroSchOp() : coroJobCnt;

            if (coroJobCnt <= 0 && pollReadyCnt <= 0 && newJobPerCnt <= 0 && timeoutCnt <= 0) {
                ++idleProcCnt;
                if (idleProcCnt >= CoroutineConfig.SleepWhenCannotProcCnt) {
                    idleProcCnt = 0;
                    container.waitThread(sleepTime);
                }
            } else {
                idleProcCnt = 0;
            }
        }
    }

    protected final <V> CoroHandle<V> pollJob() {
        CopyOnWriteArrayList<CoroJobQueue> tmpJobTypeArray = container.getJobQueues();

        if (tmpJobTypeArray.isEmpty()) {
            return null;
        }

        int startIdx = jobTypePollIdx % tmpJobTypeArray.size();
        jobTypePollIdx = startIdx + 1;
        CoroJobQueue startJobQueue = tmpJobTypeArray.get(startIdx);
        CoroJobQueue jobQueue = startJobQueue;
        while (true) {

            if (jobQueue.canRunMoreJob()) {
                jobCounter.decrementAndGet();
                CoroHandle handle = jobQueue.pollJob();
                ++newJobPerCnt;
                return handle;
            } else {
                jobQueue.resetPollCounter();
                int realIdx = jobTypePollIdx % tmpJobTypeArray.size();
                ++jobTypePollIdx;
                jobQueue = tmpJobTypeArray.get(realIdx);
                if (startJobQueue == jobQueue) {
                    break;
                }
            }
        }
        return null;
    }

    final void checkUntimedPark() {
        try {
            ArrayList<CoroHandle> handArr = new ArrayList<>();
            for (IntrusiveList.IntrusiveElem<CoroWorker> iter = untimedParkList.first(); iter != untimedParkList.end(); iter = iter.next()) {
                CoroHandle handle = iter.getData().getJob();
                long parkTime = handle.getParkInfo().getParkTime();
                if (parkTime > 0 && (DateUtils.currentTimeMillis() >= (parkTime + CoroutineConfig.maxMsUntimedCoroutinePark))) {
                    LOGGER.error(NKStringFormater.format("Coro {}:{} id {} handleid {} park for more than {} ms use jstack to check the stack"
                            , handle.coroutineName()
                            , handle.getCoroBaseInfo().getJobName()
                            , handle.getCoroBaseInfo().coroutineId()
                            , handle.getCoroBaseInfo().coroHandleId()
                            , DateUtils.currentTimeMillis() - handle.getParkInfo().getParkTime()));
                    handArr.add(handle);
                }
            }

            for (CoroHandle handle : handArr) {
                handle.getParkInfo().setInterrupted(true);
                handle.getCoroWorker().addReadyJobList();
            }

        } catch (Exception e) {
            LOGGER.error(NKStringFormater.format("CoroContainer {} procTotal Error", container.coroThread.getName()), e);
        }
    }


    protected void doYieldBySch() {
        container.yieldToMain();
//
//        if (hasToMain()) {
//            container.yieldToMain();
//        } else {
//            CoroHandle<?> nextHandle = pollReadyJobSkipSelf();
//            if (nextHandle != null) {
//                container.yieldTo(nextHandle);
//            } else {
//                container.yieldToMain();
//            }
//        }
    }

    public void onWorkerRun(CoroWorker worker) {
        try {
            CoroHandle.getRunWorkerCnt().incrementAndGet();
            while (true) {
                try {
                    if (worker.getWorkJobCnt() > 0) {
                        if (hasToMain()) {
                            return;
                        }
                    }
                    worker.setJob(pollJob());
                    if (worker.getJob() == null) {
                        return;
                    }
                    worker.getJob().setCoroWorker(worker);
                    worker.increaseWorkerId();

                    worker.getJob().run();

                    worker.getJob().awakeAllWaiters();
                    worker.getJob().setCoroWorker(null);
                    if (readyJobList.contains(worker.listElem)) {
                        LOGGER.error(NKStringFormater.format("worker error {} {} {} {} {}"
                                , worker.coroutineName(), worker.getJob().coroBaseInfo.coroutineId()
                                , worker.getJob().getCoroBaseInfo().getJobName()
                                , worker.getJob().getCoroBaseInfo().getReentrantTraceId()
                                , worker.getJob().getCoroBaseInfo().getCreateReentrantTraceId()));
                    }
                    worker.setJob(null);
                } catch (Exception t) {
                    LOGGER.error("worker Exception \n", t);
                } finally {
                    worker.listElem.remove();
                }
            }
        } finally {
            CoroHandle.getWorkerCnt().decrementAndGet();
            CoroHandle.getRunWorkerCnt().decrementAndGet();
        }
    }


    protected void checkHandleRunningStatus(CoroHandle handle) {

        if (handle == null || !handle.checkCanWaring()) {
            return;
        }

        double timeSec = (DateUtils.currentTimeMillis() - handle.getCoroStatInfo().getStartRunTime()) / 1000.0;

        if (timeSec <= 5) {
            return;
        }

        long parkcnt = handle.getCoroStatInfo().getParkCount();

        double parkPerSec = parkcnt / timeSec;

        if (parkPerSec > 1) {
            LOGGER.warn(NKStringFormater.format("job {} coroid {} park {} times per seconds for {} secs"
                    , handle.getCoroBaseInfo().getJobName(), handle.getCoroBaseInfo().coroutineId(), parkPerSec, timeSec));
        }

    }

    public boolean hasToMain() {
        if (DateUtils.currentTimeMillis() - lastHasToMainCheckTimeMs >= CoroutineConfig.MillSecToCheckTimeoutJob) {
            return true;
        }
        return false;
    }

    public void onOfferJob() {
        jobCounter.incrementAndGet();
        if (CoroutineMgr.getInstance().getCurrentContainer() != container) {
            if (needNotify()) {
                container.notifyThread();
            }
        }
    }

}
