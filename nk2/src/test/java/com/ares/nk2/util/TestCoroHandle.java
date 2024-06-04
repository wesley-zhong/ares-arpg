package com.ares.nk2.util;

import com.ares.nk2.coroutine.CoroExecutorService;
import com.ares.nk2.coroutine.CoroHandle;

import java.util.concurrent.Callable;

public class TestCoroHandle<V> extends CoroHandle<V> {

    public TestCoroHandle(String jobName, Callable<V> callable, CoroExecutorService executorService, RelationType relationType) {
        super(jobName, callable, executorService, relationType);
    }
}
