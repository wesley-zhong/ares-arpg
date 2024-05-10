package com.ares.nk2.coroutine;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

class CoroJobQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoroJobQueue.class);
    final String name;
    final Queue<CoroHandle> jobQueue;

    final int maxRunningJobCnt;
    AtomicInteger runningJobCnt = new AtomicInteger(0);

    final int maxPollJobOnce;

    public static class JobInQueueCostMsStat {
        long cnt;
        long costMsSum;
        long costMsMax;
        long costMsMin;

        public JobInQueueCostMsStat() {
            reset();
        }

        long getAvg() {
            if (cnt == 0) {
                return 0;
            } else {
                return costMsSum / cnt;
            }
        }

        void reset() {
            cnt = 0;
            costMsSum = 0;
            costMsMax = 0;
            costMsMin = Long.MAX_VALUE;
        }

        void add(long cost) {
            cnt++;
            costMsSum += cost;
            if (cost > costMsMax) {
                costMsMax = cost;
            }
            if (cost < costMsMin) {
                costMsMin = cost;
            }
        }

        public void merge(JobInQueueCostMsStat stat) {
            cnt += stat.cnt;
            costMsSum += stat.costMsSum;
            if (stat.costMsMax > costMsMax) {
                costMsMax = stat.costMsMax;
            }
            if (stat.costMsMin < costMsMin) {
                costMsMin = stat.costMsMin;
            }
        }
    }

    final JobInQueueCostMsStat jobInQueueCostMsStat = new JobInQueueCostMsStat();

    public void addInQueueCostMs(long cost) {
        jobInQueueCostMsStat.add(cost);
    }

    static class CoroStat {
        int curPollJobOnce = 0;
        long totalPollJobCnt = 0;
        long totalFiniJobCnt = 0;
        long totalCostCreateToRun = 0;
        long totalCostRunToFini = 0;
        long waitQueueFullCount = 0;
        long jobsInQueue = 0;
        long resumeCoroCount = 0;
        long startCoroCount = 0;
        long parkCoroCount = 0;

        public void addStat(CoroStat coroStat) {
            curPollJobOnce += coroStat.curPollJobOnce;
            totalPollJobCnt += coroStat.totalPollJobCnt;
            totalFiniJobCnt += coroStat.totalFiniJobCnt;
            totalCostCreateToRun += coroStat.totalCostCreateToRun;
            totalCostRunToFini += coroStat.totalCostRunToFini;
            waitQueueFullCount += coroStat.waitQueueFullCount;
            jobsInQueue += coroStat.jobsInQueue;
            resumeCoroCount += coroStat.resumeCoroCount;
            startCoroCount += coroStat.startCoroCount;
            parkCoroCount += coroStat.parkCoroCount;
        }
    }

    CoroStat coroStat = new CoroStat();

    final boolean checkEmptyBeforeStop;
    final CoroutineContainer container;
    //final AtomicInteger waitingQueueSize = new AtomicInteger();
    public static final int DEFAULT_MAX_WAITING_JOB_COUNT = 2000;

    public CoroStat getCoroStat() {
        return coroStat;
    }

    public CoroStat clearCoroStat() {
        CoroStat tmp = coroStat;
        coroStat = new CoroStat();
        return tmp;
    }

    CoroJobQueue(String qName, int concurrentJobCnt, int maxJobOneCycle, boolean checkEmptyBeforeStop, CoroutineContainer container) {
        name = qName;
        maxRunningJobCnt = concurrentJobCnt;
        maxPollJobOnce = maxJobOneCycle;
        this.checkEmptyBeforeStop = checkEmptyBeforeStop;
        this.container = container;
        jobQueue = new ArrayBlockingQueue<>(100_0000);
    }

    long lastWarningTime = 0;

    int getJobCnt() {
        return jobQueue.size();
    }

    boolean offerJob(CoroHandle handle) {
//        if (!jobQueue.isEmpty()) {
//            if (!canRunMoreJobInternal()) {
//                lastWarningTime = DateUtils.currentTimeMillis();
//                LOGGER.error(StringFormatter.format("cannot run more job on this queue {}, running job cnt {}:{}, pool job once {}:{}"
//                        , name, runningJobCnt.get(), maxRunningJobCnt, coroStat.curPollJobOnce, maxPollJobOnce));
//            }
//        }
        boolean result = jobQueue.offer(handle);
        if (!result) {
            LOGGER.error("coro queue full");
            coroStat.waitQueueFullCount++;
        }

        return result;
    }

    CoroHandle pollJob() {
        ++coroStat.curPollJobOnce;
        ++coroStat.totalPollJobCnt;
        return jobQueue.poll();
    }

    boolean canRunMoreJob() {
        if (jobQueue.isEmpty()) {
            return false;
        }

        return canRunMoreJobInternal();
    }

    private boolean canRunMoreJobInternal() {
        if (runningJobCnt.get() >= maxRunningJobCnt) {
            LOGGER.error("can't RunMoreJobInternal. runningJobCnt " + runningJobCnt.get() + " >= maxRunningJobCnt " + maxRunningJobCnt);
            return false;
        }

        if (coroStat.curPollJobOnce >= maxPollJobOnce) {
            LOGGER.error("can't RunMoreJobInternal. curPollJobOnce " + coroStat.curPollJobOnce + " >= maxPollJobOnce " + maxPollJobOnce);
            return false;
        }

        return true;
    }

    void resetPollCounter() {
        coroStat.curPollJobOnce = 0;
    }

    void increaseRunningCnt() {
        runningJobCnt.incrementAndGet();
    }

    void decreaseRuningCnt() {
        runningJobCnt.decrementAndGet();
    }
}
