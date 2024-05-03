package com.ares.nk2.coRedis;


import com.ares.nk2.coroutine.CoroutineAsync;
import io.lettuce.core.RedisFuture;

import java.util.function.Supplier;

/**
 * Created by levoneliu on 2018/9/5.
 */

public class CoRedisAsync<V> extends CoroutineAsync<V, RuntimeException> {

    protected Supplier<RedisFuture<V>> target;

    void coRedisRunnable() {
        RedisFuture<V> getFuture = target.get();
        getFuture.thenAccept(this::success);
    }

    CoRedisAsync(Supplier<RedisFuture<V>> pTarget) {
        super(null);
        target = pTarget;
        setRunnable(this::coRedisRunnable);
    }

    public void success(V result) {
        completed(result);
    }

    public void failure(RuntimeException code) {
        fail(code);
    }

}