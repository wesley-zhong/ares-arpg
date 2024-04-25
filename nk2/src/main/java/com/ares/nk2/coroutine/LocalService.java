package com.ares.nk2.coroutine;

import com.ares.nk2.tool.DateUtils;
import com.ares.nk2.tool.StringFormatter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public abstract class LocalService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalService.class);
    private static final int MAX_THREAD_NAME_LEN = 12;
    public static final long FIRST_SCH_ID = 0;

    public static final long defaultCallJobTimeout = 20000;
    private final ExecutorLocal<Boolean> procState = ExecutorLocal.withInitial(() -> false);
    private final ExecutorLocal<Boolean> reloadState = ExecutorLocal.withInitial(() -> false);

    private int executorServiceCount = 0;
    private int newContainerCount = 1;
    private final ArrayList<CoroExecutorService> executorArrayList = new ArrayList<>(32);
    private final String name;

    protected LocalService(String name) {
        this.name = name;
    }

    public final void init() {
        callJobInit();
    }

    public final int proc() {
        return callJobProc();
    }

    public final int stop() {
        return callJobStop();
    }

    public final int reload() {
        return callJobReload();
    }

    class CallJobInitCallable implements Callable<Void> {
        int executorServiceIndex;

        CallJobInitCallable(int executorServiceIndex) {
            this.executorServiceIndex = executorServiceIndex;
        }

        @Override
        public Void call() throws Exception {
            if (reloadState.get()) {
                throw new RuntimeException(StringFormatter.format("callJobReload running, type: {}, executorServiceIndex: {}", getName(), executorServiceIndex));
            }
            try {
                reloadState.set(true);
                executorLocalInit(executorServiceIndex);
            } finally {
                reloadState.set(false);
            }
            return null;
        }
    }

    public final void callJobInit() {
        for (int i = 0; i < getExecutorServiceCount(); i++) {
            LocalService.DoNotDirectCall.LocalServiceMaintain.callJob(this, i, executorLocalCallJobInitTimeout(), new CallJobInitCallable(i), "localServiceCallJobInit");
        }
    }

    class CallJobReloadCallable implements Callable<Integer> {
        int executorServiceIndex;

        CallJobReloadCallable(int executorServiceIndex) {
            this.executorServiceIndex = executorServiceIndex;
        }

        @Override
        public Integer call() throws Exception {
            if (reloadState.get()) {
                throw new RuntimeException(StringFormatter.format("callJobReload running, type: {}, executorServiceIndex: {}", getName(), executorServiceIndex));
            }

            try {
                reloadState.set(true);
                return executorLocalReload(executorServiceIndex);
            } catch (Throwable e) {
                LOGGER.error("", e);
                throw e;
            } finally {
                reloadState.set(false);
            }
        }
    }

    public final int callJobReload() {
        int ret = 0;

        for (int i = 0; i < getExecutorServiceCount(); i++) {
            Integer callJobResult = null;
            callJobResult = LocalService.DoNotDirectCall.LocalServiceMaintain.callJob(this, i, executorLocalCallJobReloadTimeout(), new CallJobReloadCallable(i), "localServiceCallJobReload");
            if (callJobResult == null) {
                ret = -1;
            } else {
                ret |= callJobResult;
            }
        }

        if (ret == 0) {
            LOGGER.warn(StringFormatter.format("callJobReload, ret: {}", ret));
        } else {
            LOGGER.warn(StringFormatter.format("failed to callJobReload, ret: {}", ret));
        }

        return ret;
    }


    class CallJobStopCallable implements Callable<Integer> {
        int executorServiceIndex;

        CallJobStopCallable(int executorServiceIndex) {
            this.executorServiceIndex = executorServiceIndex;
        }


        @Override
        public Integer call() throws Exception {
            return executorLocalStop(executorServiceIndex);
        }
    }

    public final int callJobStop() {
        LOGGER.warn(StringFormatter.format("local service:[{}] callJobStop begin", getName()));

        List<CoroHandle<Integer>> jobList = new ArrayList<>(getExecutorServiceCount());
        for (int i = 0; i < getExecutorServiceCount(); i++) {
            try {
                int executorServiceIndex = i;
                final CoroHandle<Integer> coroHandle = DoNotDirectCall.LocalServiceMaintain.submitJob(this, executorServiceIndex, new CallJobStopCallable(i),
                        "localServiceCallJobStop");
                jobList.add(executorServiceIndex, coroHandle);
            } catch (Exception e) {
                LOGGER.error(StringFormatter.format("local service:[{}] index:[{}] callJobStop failed! ", getName(), i), e);
            }
        }

        int ret = 0;
        long leftWaitTime = executorLocalCallJobStopTimeout();
        for (int i = 0; i < jobList.size(); i++) {
            final CoroHandle<Integer> job = jobList.get(i);
            if (job == null) {
                ret = -1;
                continue;
            }
            Integer result = null;
            try {
                final long beginMs = DateUtils.currentTimeMillis();
                result = job.get(leftWaitTime);
                leftWaitTime -= (DateUtils.currentTimeMillis() - beginMs);
            } catch (TimeoutException e) {
                ret = -1;
                LOGGER.error(StringFormatter.format("local service:[{}] index:[{}] callJobStop timeout", getName(), i), e);
                break;
            }
            if (result == null) {
                ret = -1;
            } else {
                ret |= result;
            }
        }

        if (ret == 0) {
            LOGGER.warn(StringFormatter.format("local service:[{}] callJobStop end, ret: {}", getName(), ret));
        } else {
            LOGGER.warn(StringFormatter.format("local service:[{}] callJobStop failed, ret: {}", getName(), ret));
        }

        return ret;
    }

    ArrayList<AtomicLong> procCostTime = new ArrayList<>();

    class CallJobProcCallable implements Callable<Integer> {
        int executorServiceIndex;

        CallJobProcCallable(int executorServiceIndex) {
            this.executorServiceIndex = executorServiceIndex;
        }

        @Override
        public Integer call() throws Exception {
            if (procState.get()) {
                LOGGER.error("threadLocalProc running");
                return 0;
            }
            int retcnt = 0;
            long startTimeMs = DateUtils.currentTimeMillis();
            try {
                procState.set(true);
                retcnt += executorLocalProc(executorServiceIndex);
                long deltaTime = DateUtils.currentTimeMillis() - startTimeMs;
                procCostTime.get(executorServiceIndex).set(deltaTime);
            } catch (Exception e) {
                LOGGER.error("threadLocalProc exception:", e);
                throw e;
            } finally {
                procState.set(false);
            }
            return retcnt;
        }
    }

    public final int callJobProc() {
        int ret = 0;
        ArrayList<CoroHandle<Integer>> resultArr = new ArrayList<>();
        int threadCount = getExecutorServiceCount();
        try {
            for (int i = 0; i < threadCount; i++) {
                if (procCostTime.size() <= i) {
                    procCostTime.add(new AtomicLong(0));
                }
                try {
                    Callable<Integer> call = new CallJobProcCallable(i);
                    resultArr.add(LocalService.DoNotDirectCall.LocalServiceMaintain.submitJob(this, i, call, "localServiceProc"));
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            }

            for (CoroHandle<Integer> handle : resultArr) {
                try {
                    ret += handle.get(5000L);
                } catch (Exception e) {
                    String jobName = handle.getCoroBaseInfo() == null ? "" : handle.getCoroBaseInfo().getJobName();
                    LOGGER.error(StringFormatter.format("local service callJobProc error, jobName: {}", jobName), e);
                }
            }

        } finally {
            for (int i = 0; i < threadCount; ++i) {
                procCostTime.get(i).set(0);
            }
        }
        return ret;
    }

    protected int executorLocalProc(int executorServiceIndex) {
        return 0;
    }

    protected void executorLocalInit(int executorServiceIndex) {
    }


    protected int executorLocalReload(int executorServiceIndex) {
        return 0;
    }

    protected int executorLocalStop(int executorServiceIndex) {
        return 0;
    }

    protected long executorLocalCallJobInitTimeout() {
        return 10000;
    }

    protected long executorLocalCallJobStopTimeout() {
        return 10000;
    }

    protected long executorLocalCallJobReloadTimeout() {
        return 10000;
    }

    protected long executorLocalAttrCollectorStopTimeout() {
        return 3_000;
    }

    protected void setNewContainerCount(int newContainerCount) {
        this.newContainerCount = newContainerCount;
    }

    private void generateExecutor(String jobQueueName, int concurrent, int maxJobOneCycle, CoroutineContainer coroutineContainer, CoroExecutorService.InitContext initContext) {
        CoroExecutorService coroExecuterService;
        if (coroutineContainer == null) {
            coroExecuterService = CoroExecutorService.newInstanceWithNewContainer(jobQueueName, concurrent, maxJobOneCycle, initContext);
        } else {
            coroExecuterService = CoroExecutorService.newInstanceWithExistingContainer(jobQueueName, concurrent, maxJobOneCycle, coroutineContainer);
        }

        executorArrayList.add(coroExecuterService);
        executorServiceCount = executorArrayList.size();
    }

    protected void generateExecutorGroupWithNewContainer(String jobQueueName, int concurrent, int maxJobOneCycle) {
        for (int i = 0; i < newContainerCount; ++i) {
            String eName = getThreadName(jobQueueName) + "-" + i;
            generateExecutor(eName, concurrent, maxJobOneCycle, null, CoroExecutorService.InitContext.newInstance(i));
        }
    }

    protected void generateExecutorWithContainer(String jobQueueName, int concurrent, int maxJobOneCycle, CoroutineContainer coroutineContainer) {
        generateExecutor(getThreadName(jobQueueName), concurrent, maxJobOneCycle, coroutineContainer, CoroExecutorService.InitContext.defaultInstance());
    }

    public final CoroExecutorService getExecutor(long hashKey) {
        if (executorArrayList.isEmpty()) {
            return null;
        }
        long index = Math.abs(hashKey) % executorArrayList.size();
        return executorArrayList.get((int) index);
    }

    public final String getName() {
        return name;
    }

    public int getExecutorServiceCount() {
        return executorServiceCount;
    }

    public int getExecutorServiceIndex(long key) {
        return (int) (Math.abs(key) % (long) getExecutorServiceCount());
    }

    public boolean belongsToCurrentExecutor(long hashKey) {
        return getExecutor(hashKey) == CoroExecutorService.current();
    }

    private static String getThreadName(String jobQueueName) {
        return jobQueueName.substring(0, Math.min(MAX_THREAD_NAME_LEN, jobQueueName.length()));
    }

    public enum DoNotDirectCall {
        LocalServiceMaintain,
        ;

        public <V> V callJob(LocalService localService, long hashKey, long timeout, Callable<V> call, String jobname) {
            CoroExecutorService executor = localService.getExecutor(hashKey);
            if (executor == null) {
                throw new RuntimeException(StringFormatter.format("failed to get executor, hashKey: {}", hashKey));
            }

            return executor.callJob(timeout, call, jobname);
        }

        public <V> void runJob(LocalService localService, long hashKey, Callable<V> call, String jobname) {
            CoroExecutorService executor = localService.getExecutor(hashKey);
            if (executor == null) {
                throw new RuntimeException(StringFormatter.format("failed to get executor, hashKey: {}", hashKey));
            }

            executor.runJob(call, jobname);
        }

        public final <V> CoroHandle<V> submitJob(LocalService localService, long hashKey, Callable<V> call, String jobname) {
            CoroExecutorService executor = localService.getExecutor(hashKey);
            if (executor == null) {
                throw new RuntimeException(StringFormatter.format("failed to get executor, hashKey: {}", hashKey));
            }

            return executor.submit(call, jobname);
        }

        public final <V> void broadcastRunJob(LocalService localService, Callable<V> call, String jobname) {
            for (CoroExecutorService executer : localService.executorArrayList) {
                executer.runJob(call, jobname);
            }
        }
    }
}
