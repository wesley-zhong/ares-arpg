package com.ares.nk2.coroutine;

import com.ares.nk2.coroutine.exception.UntimedCoroInterruptedException;
import com.ares.nk2.tool.DateUtils;
import com.ares.nk2.tool.StringFormatter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.*;

public class CoroutineAsync<V, E extends Throwable> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoroutineAsync.class);
    private Runnable _runnable = null;
    private final CoroHandle coroHandle;
    private volatile boolean completed;
    private Throwable exception;
    private V result;
    private transient Thread registrationThread;
    private transient volatile boolean registrationComplete;
    private long timeoutNanos;
    private transient long deadline;
    static final String notInCoroutineEnv = "Method called not from within a coroutine";

    public CoroutineAsync() {
        this(null);
    }

    public CoroutineAsync(Runnable runnable) {
        coroHandle = (CoroHandle) CoroHandle.current();
        _runnable = runnable;
    }

    protected void setRunnable(Runnable runnable) {
        _runnable = runnable;
    }

    public V run() throws E {
        if (coroHandle == null) {
            throw new IllegalThreadStateException(notInCoroutineEnv);
        }

        if (registrationComplete) {
            throw new IllegalStateException("This FiberAsync instance has already been used");
        }

        try {
            registrationThread = Thread.currentThread();
            requestAsync();
        } catch (Throwable t) {
            asyncFailed(t);
        } finally {
            registrationComplete = true;
        }
        try {
            CoroHandle.park(this);
        } catch (UntimedCoroInterruptedException e) {
            if (!completed) {
                LOGGER.error("fatal: coro park max config timeout", e);
            }
        }
        return getResult();
    }

    public V run(long timeout) throws E, TimeoutException {
        return run(timeout, TimeUnit.MILLISECONDS);
    }

    public V run(long timeout, TimeUnit unit) throws E, TimeoutException {
        if (CoroHandle.current() == null) {
            throw new IllegalThreadStateException(notInCoroutineEnv);
        }

        if (registrationComplete) {
            throw new IllegalStateException("This FiberAsync instance has already been used");
        }

        if (unit == null) {
            return run();
        }
        if (timeout <= 0) {
            throw new TimeoutException();
        }

        timeoutNanos = unit.toNanos(timeout);
        deadline = System.nanoTime() + timeoutNanos;

        try {
            registrationThread = Thread.currentThread();
            requestAsync();
        } catch (Throwable t) {
            asyncFailed(t);
        } finally {
            registrationComplete = true;
        }

        if (timeoutNanos > 0 && deadline == 0)
        {
            deadline = System.nanoTime() + timeoutNanos;
        }

        long begin = DateUtils.currentTimeMillis();
        while (!completed) {
            long now = System.nanoTime();
            long diff = (deadline - now) / 1_000_000L;

            if (diff <= 0) {
                exception = new TimeoutException();
                completed = true;
                long end = DateUtils.currentTimeMillis();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(StringFormatter.format("CoroutineAsync timeout, cost: {}, begin: {}, end: {}, object: {}",
                            end - begin, begin, end, this));
                }
                throw (TimeoutException) exception;
            }

            try {
                CoroHandle.park(this, diff);
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }

        return getResult();
    }

    protected void interrupted() {

    }

    protected void requestAsync() {
        if (_runnable != null) {
            _runnable.run();
        }
    }

    protected V requestSync() throws E, ExecutionException {
        throw new IllegalThreadStateException(notInCoroutineEnv);
    }

    private V requestSync(long timeout, TimeUnit unit) throws ExecutionException, TimeoutException, E {
        throw new IllegalThreadStateException(notInCoroutineEnv);
    }

    private void unpark() {
        coroHandle.unpark(this);
    }

    protected void asyncCompleted(V result) {
        if (completed)
        {
            return;
        }
        this.result = result;
        completed = true;
        unpark();
    }

    public void completed(V result) {
        asyncCompleted(result);
    }

    public void asyncFailed(Throwable t) {
        if (t == null) {
            throw new IllegalArgumentException("t must not be null");
        }
        if (completed)
        {
            return;
        }
        exception = t;
        completed = true;
        unpark();
    }

    public void fail(Throwable t) {
        asyncFailed(t);
    }

    protected void prepark() {
    }

    public final boolean isCompleted() {
        return completed;
    }

    public final V getResult() throws E {
        if (!completed) {
            throw new IllegalStateException("Not completed");
        }
        if (exception != null) {
            throw wrapException(exception);
        }
        return result;
    }

    public final V syncGetResult(long timeoutMs) throws E, TimeoutException {
        long begin = DateUtils.currentTimeMillis();

        while (!completed) {
            long left = timeoutMs + begin - DateUtils.currentTimeMillis();
            if (left <= 0) {
                throw new TimeoutException();
            } else {
                CoroHandle.park(this, left);
            }
        }

        return getResult();
    }

    private E wrapException(Throwable t) {
        return (E) t;
    }

    public static <V, E extends Exception> V runBlocking(ExecutorService exec, Callable<V> callable) throws E, InterruptedException {
        try {
            return new ThreadBlockingFiberAsync<>(exec, callable).run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static <V, E extends Exception> V runBlocking(ExecutorService exec, long timeout, TimeUnit unit, Callable<V> callable) throws E, Exception {
        return new ThreadBlockingFiberAsync<>(exec, callable).run(timeout, unit);
    }

    private static class ThreadBlockingFiberAsync<V, E extends Exception> extends CoroutineAsync<V, E> {
        private final ExecutorService exec;
        private final Callable<V> action;
        private Future<?> fut;


        public ThreadBlockingFiberAsync(ExecutorService exec, Callable<V> action) {
            this.exec = exec;
            this.action = action;
        }


        @Override
        protected void requestAsync() {
            fut = exec.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        V res = action.call();
                        asyncCompleted(res);
                    } catch (Throwable e) {
                        asyncFailed(e);
                    }
                }
            });
        }


        @Override
        protected V requestSync() throws E {
            try {
                return action.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void interrupted() {
            if (fut != null) {
                fut.cancel(true);
            }
            super.interrupted();
        }
    }
}
