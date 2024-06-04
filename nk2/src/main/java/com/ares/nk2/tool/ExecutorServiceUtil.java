package com.ares.nk2.tool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.collect.Sets;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;

public abstract class ExecutorServiceUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorServiceUtil.class);
    private static final ConcurrentHashMap<String, WeakReference<ExecutorService>> executorServiceMap = new ConcurrentHashMap<>();
    private static final Set<String> noForceShutdownThreads = Sets.newConcurrentHashSet();
    private static final Set<String> ignoreThreads = Sets.newConcurrentHashSet();

    public enum THREAD_SHUTDOWN_TYPE {
        THREAD_SHUTDOWN_TYPE_IGNORE,
        THREAD_SHUTDOWN_TYPE_FORCE_SHUTDOWN,
        THREAD_SHUTDOWN_TYPE_NO_FORCE_SHUTDOWN
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(String threadName) {
        return newSingleThreadScheduledExecutor(threadName, true, false);
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(String threadName, THREAD_SHUTDOWN_TYPE type) {
        if (type == THREAD_SHUTDOWN_TYPE.THREAD_SHUTDOWN_TYPE_IGNORE) {
            return newSingleThreadScheduledExecutor(threadName, false, true);
        } else if (type == THREAD_SHUTDOWN_TYPE.THREAD_SHUTDOWN_TYPE_FORCE_SHUTDOWN) {
            return newSingleThreadScheduledExecutor(threadName, true, false);
        } else {
            return newSingleThreadScheduledExecutor(threadName, false, false);
        }
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(String threadName, boolean forceShutdown, boolean ignore) {
        if (executorServiceMap.containsKey(threadName)) {
            LOGGER.error(NKStringFormater.format("already has used the thread name {}", threadName));
            return null;
        }
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).setNameFormat(threadName).build());
        if (null != executorServiceMap.putIfAbsent(threadName, new WeakReference<>(ses))) {
            LOGGER.error(NKStringFormater.format("already has used the thread name {}", threadName));
            ses.shutdown();
        }
        if (ignore) {
            ignoreThreads.add(threadName);
        } else if (forceShutdown == false) {
            noForceShutdownThreads.add(threadName);
        }
        return ses;
    }

    public static ScheduledExecutorService newScheduledThreadPool(String threadName, int corePoolSize) {
        return newScheduledThreadPool(threadName, corePoolSize, true);
    }

    public static ScheduledExecutorService newScheduledThreadPool(String threadName, int corePoolSize, boolean forceShutdown) {
        if (executorServiceMap.containsKey(threadName)) {
            LOGGER.error(NKStringFormater.format("already has used the thread name {}", threadName));
            return null;
        }
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(corePoolSize, new ThreadFactoryBuilder().setDaemon(true).setNameFormat(threadName + "-%d").build());
        if (null != executorServiceMap.putIfAbsent(threadName, new WeakReference<>(ses))) {
            LOGGER.error(NKStringFormater.format("already has used the thread name {}", threadName));
            ses.shutdown();
        }
        if (forceShutdown == false) {
            noForceShutdownThreads.add(threadName);
        }
        return ses;
    }

    public static ExecutorService newFixedThreadPool(String threadName, int threads) {
        return newFixedThreadPool(threadName, threads, true);
    }

    public static ExecutorService newFixedThreadPool(String threadName, int threads, boolean forceShutdown) {
        if (executorServiceMap.containsKey(threadName)) {
            LOGGER.error(NKStringFormater.format("already has used the thread name {}", threadName));
            return null;
        }
        ExecutorService es = Executors.newFixedThreadPool(threads, new ThreadFactoryBuilder().setDaemon(true).setNameFormat(threadName + "-%d").build());
        if (null != executorServiceMap.putIfAbsent(threadName, new WeakReference<>(es))) {
            LOGGER.error(NKStringFormater.format("already has used the thread name {}", threadName));
            es.shutdown();
        }
        if (forceShutdown == false) {
            noForceShutdownThreads.add(threadName);
        }
        return es;
    }

    public static ExecutorService newSingleThreadExecutor(String threadName) {
        return newSingleThreadExecutor(threadName, true);
    }

    public static ExecutorService newSingleThreadExecutor(String threadName, boolean forceShutdown) {
        if (executorServiceMap.containsKey(threadName)) {
            LOGGER.error(NKStringFormater.format("already has used the thread name {}", threadName));
            return null;
        }
        ExecutorService es = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).setNameFormat(threadName).build());
        if (null != executorServiceMap.putIfAbsent(threadName, new WeakReference<>(es))) {
            LOGGER.error(NKStringFormater.format("already has used the thread name {}", threadName));
            es.shutdown();
        }
        if (forceShutdown == false) {
            noForceShutdownThreads.add(threadName);
        }
        return es;
    }

    public static ThreadPoolExecutor newThreadPoolExecutor(String threadName,
                                                           int corePoolSize,
                                                           int maximumPoolSize,
                                                           long keepAliveTime,
                                                           TimeUnit unit,
                                                           BlockingQueue<Runnable> workQueue) {
        return newThreadPoolExecutor(threadName, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, true);
    }

    public static ThreadPoolExecutor newThreadPoolExecutor(String threadName,
                                                           int corePoolSize,
                                                           int maximumPoolSize,
                                                           long keepAliveTime,
                                                           TimeUnit unit,
                                                           BlockingQueue<Runnable> workQueue,
                                                           boolean forceShutdown) {
        if (executorServiceMap.containsKey(threadName)) {
            LOGGER.error(NKStringFormater.format("already has used the thread name {}", threadName));
            return null;
        }
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new ThreadFactoryBuilder().setDaemon(true).setNameFormat(threadName + "%d").build());
        if (null != executorServiceMap.putIfAbsent(threadName, new WeakReference<>(tpe))) {
            LOGGER.error(NKStringFormater.format("already has used the thread name {}", threadName));
            tpe.shutdown();
        }
        if (forceShutdown == false) {
            noForceShutdownThreads.add(threadName);
        }
        return tpe;
    }

    public static void shutdownGraceful(Class<?> clazz, List<ExecutorService> executorServices, int timeoutInSeconds) {

        ExecutorService instance;
        Iterator<ExecutorService> iterator = executorServices.iterator();
        while (iterator.hasNext()) {
            instance = iterator.next();
            if (instance == null) {
                iterator.remove();
            } else {
                instance.shutdownNow();
            }
        }

        long endTime = DateUtils.currentTimeMillis() + timeoutInSeconds * 1000L;
        while (true) {
            if (executorServices.isEmpty()) {
                LOGGER.error(NKStringFormater.format("{} shutdownGraceful ok", clazz.getSimpleName()));
                break;
            }
            iterator = executorServices.iterator();
            while (iterator.hasNext()) {
                instance = iterator.next();
                try {
                    if (true == instance.awaitTermination(1, TimeUnit.SECONDS)) {
                        iterator.remove();
                        continue;
                    }
                    if (DateUtils.currentTimeMillis() > endTime) {
                        LOGGER.error("shutdown graceful failed timeout");
                        break;
                    }
                } catch (InterruptedException e) {
                    // ignore
                }
                LOGGER.error(NKStringFormater.format("await termination {} total= {}", clazz.getSimpleName(), executorServices.size()));
            }
        }
    }

    public static void shutdownGraceful() {
        Set<String> noForceThreads = new HashSet<>();
        ExecutorService instance;
        Map.Entry<String, WeakReference<ExecutorService>> entry;
        Iterator<Map.Entry<String, WeakReference<ExecutorService>>> iterator = executorServiceMap.entrySet().iterator();
        while (iterator.hasNext()) {
            entry = iterator.next();
            if (entry == null) {
                iterator.remove();
                continue;
            }
            instance = entry.getValue().get();
            if (instance == null) {
                iterator.remove();
                continue;
            }

            if (ignoreThreads.contains(entry.getKey())) {
                iterator.remove();
                continue;
            }

            if (noForceShutdownThreads.contains(entry.getKey())) {
                noForceThreads.add(entry.getKey());
            }
            instance.shutdownNow();
        }

        boolean graceful = true;
        WeakReference<ExecutorService> ref;
        long startTime = DateUtils.currentTimeMillis();
        long endTime = startTime + 10 * 1000L;
        while (true) {
            if (executorServiceMap.isEmpty() || graceful == false) {
                break;
            }

            Iterator<Map.Entry<String, WeakReference<ExecutorService>>> it = executorServiceMap.entrySet().iterator();
            while (it.hasNext()) {
                entry = it.next();
                ref = entry.getValue();
                if (ref == null) {
                    LOGGER.error(NKStringFormater.format("{} finished ref == null", entry.getKey()));
                    it.remove();
                    noForceThreads.remove(entry.getKey());
                    continue;
                }
                instance = ref.get();
                if (instance == null) {
                    LOGGER.error(NKStringFormater.format("{} finished instance == null", entry.getKey()));
                    it.remove();
                    noForceThreads.remove(entry.getKey());
                    continue;
                }
                try {
                    if (true == instance.awaitTermination(1, TimeUnit.SECONDS)) {
                        LOGGER.error(NKStringFormater.format("{} finished, cost {} ms", entry.getKey(), (DateUtils.currentTimeMillis() - startTime)));
                        it.remove();
                        noForceThreads.remove(entry.getKey());
                        continue;
                    }
                    if (DateUtils.currentTimeMillis() > endTime) {
                        if (noForceThreads.isEmpty()) {
                            LOGGER.error("shutdown graceful failed timeout");
                            graceful = false;
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    // ignore
                }
                LOGGER.error(NKStringFormater.format("await termination total= {} {}", executorServiceMap.size(), noForceThreads));
            }
        }

        LOGGER.error("shutdown graceful ok");
    }

    public static void shutdownAndAwaitTermination(String threadName) {
        WeakReference<ExecutorService> ref = executorServiceMap.get(threadName);
        if (ref == null || ref.get() == null) {
            LOGGER.error(NKStringFormater.format("not found ExecutorService {}", threadName));
            return;
        }
        long now = System.currentTimeMillis();
        long warnTimeout = now + 120 * 1000L;
        long breakTimeout = now + 300 * 1000L;
        ExecutorService es = ref.get();
        if (es == null) {
            LOGGER.error(NKStringFormater.format("es == null ExecutorService {}", threadName));
            return;
        }
        try {
            es.shutdown();
            boolean warned = false;
            while (es.awaitTermination(5, TimeUnit.SECONDS) == false) {
                now = System.currentTimeMillis();
                if (now >= warnTimeout && warned == false) {
                    warned = true;
                }
                if (now >= breakTimeout) {
                    LOGGER.error(NKStringFormater.format("shutdownAndAwaitTermination timeout {}", threadName));
                    break;
                }
                LOGGER.error(NKStringFormater.format("shutdown {}...", threadName));
            }
            executorServiceMap.remove(threadName);
        } catch (InterruptedException e) {
            LOGGER.error("", e);
        }

    }
}
