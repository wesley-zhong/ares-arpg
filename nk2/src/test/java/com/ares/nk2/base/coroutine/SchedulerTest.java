package com.ares.nk2.base.coroutine;

import com.ares.nk2.util.CoroutineEnv;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class SchedulerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerTest.class);

    @BeforeClass
    public static void initialise() throws Exception {
        if (CoroutineEnv.getInstance() == null) {
            throw new Exception("init env fail");
        }
    }

    boolean exec = false;

    @Ignore
    @Test
    public void testSchedule() {
//        CoroutineEnv.getInstance().getSystemExecuter().schedule("testcaes", 100, (Callable<?>) () -> {
//            exec = true;
//            return null;
//        }, false);
//        LOGGER.error("start test");
//        //while (true) {
//        try {
//            Thread.sleep(2000);
//        } catch (Exception ignored) {
//            LOGGER.error("", ignored);
//        }
//        Assert.assertTrue(exec);
        //}
    }

    int cnt = 0;

    @Test
    public void testScheduleAtFixRate() {

//        CoroutineEnv.getInstance().getSystemExecuter().scheduleAtFixedRate("testcaes", 100, 1000, (Callable<?>) () -> {
//            LOGGER.error("abc");
//            ++cnt;
//            return null;
//        }, false, false);
//        LOGGER.error("start test");
//
//        try {
//            Thread.sleep(5000);
//        } catch (Exception ignored) {
//            LOGGER.error("", ignored);
//        }
//
//        Assert.assertTrue((cnt > 3) && (cnt < 6));

    }
}
    
