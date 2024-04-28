package com.ares.nk2.coRedis;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.lettuce.core.RedisFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Created by levoneliu on 2018/9/11.
 */

public class CoRedisAsyncExecutor<V> extends CoRedisAsync<V> {

    static Executor sch = null;

    static void registerExecutor(Executor sch) {
        CoRedisAsyncExecutor.sch = sch;
    }
    
    void coRedisAsyncExecutorInnerRunnable() {
        RedisFuture<V> getFuture = target.get();
        getFuture.thenAccept(CoRedisAsyncExecutor.this::success);
    }

    void coRedisAsyncExecutorRunnable() {
        if (sch == null) {
            sch = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("redis_req").build());
        }
        sch.execute(this::coRedisAsyncExecutorInnerRunnable);
    }

    CoRedisAsyncExecutor(Supplier<RedisFuture<V>> target) {
        super(target);
        setRunnable(this::coRedisAsyncExecutorRunnable);
    }

}
