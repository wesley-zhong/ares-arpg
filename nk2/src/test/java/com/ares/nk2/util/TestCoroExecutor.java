package com.ares.nk2.util;

import com.ares.nk2.coroutine.CoroExecutorService;
import com.ares.nk2.coroutine.CoroHandle;
import com.ares.nk2.coroutine.CoroutineContainer;
import com.ares.nk2.coroutine.CoroutineMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class TestCoroExecutor extends CoroExecutorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestCoroExecutor.class);

    public TestCoroExecutor(String sname, int concurrentJobcnt, int maxjobonececnt, CoroutineContainer container) {
        super(sname, concurrentJobcnt, maxjobonececnt, container, InitContext.defaultInstance());
    }

    public static TestCoroExecutor newInstanceBySystemContainer(String name, int concurrentJobcnt, int maxJobOneCycyle) {
        CoroutineContainer container = CoroutineMgr.getInstance().getSystemContainer();
        return new TestCoroExecutor(name, concurrentJobcnt, maxJobOneCycyle, container);
    }

    public static TestCoroExecutor newInstanceWithNewContainer(String name, int concurrentJobcnt, int maxJobOneCycyle) {
        return new TestCoroExecutor(name, concurrentJobcnt, maxJobOneCycyle, null);
    }

    public static TestCoroExecutor current() {
        CoroExecutorService currentCoroExecutorService = CoroHandle.currentExectorService();
        if (currentCoroExecutorService == null) {
            return null;
        }

        if (!(currentCoroExecutorService instanceof TestCoroExecutor)) {
            return null;
        }

        return (TestCoroExecutor) currentCoroExecutorService;
    }


    ////////////////////////////////////////////////////////TimiCoroutineUtil////////////////////////////////////////////////////////////////////////////////////
    public <V> CoroHandle<V> submit(Callable<V> call, String jobName) {
        return super.submit(call, jobName);
    }
}
