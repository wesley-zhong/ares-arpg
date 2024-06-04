package com.ares.nk2.base.coroutine;

import com.ares.nk2.coroutine.CoroHandle;
import com.ares.nk2.util.CoroutineEnv;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoroGetTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoroGetTest.class);

    @BeforeClass
    public static void initialise() {
        CoroutineEnv.getInstance().init();
    }

    @Ignore
    @Test
    public void testGetFastBack() throws Exception {
        CoroutineEnv.getInstance().getTestCaseExecuter().runJob(() -> {

            CoroHandle<?> handle = CoroutineEnv.getInstance().getTestCaseExecuter().submit(() -> {
                while (true) {
                    CoroHandle.sleep(250);
                    LOGGER.error("inner finished");
                }
                //  return null;
            }, "test_a");

            LOGGER.error("before sleep");
            CoroHandle.sleep(100 * 1000);
            LOGGER.error("after sleep");

            try {
                LOGGER.error("before get");
                handle.get(100 * 1000);
                LOGGER.error("after get");
            } catch (Exception e) {
                LOGGER.error("", e);
            }

            return null;
        }, "test_b");

        while (true) {
            try {
                Thread.sleep(5 * 1000);
            } catch (Exception e) {

            }
        }
    }
}
