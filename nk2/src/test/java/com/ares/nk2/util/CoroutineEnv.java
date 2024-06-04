package com.ares.nk2.util;

import com.ares.nk2.coroutine.CoroHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

public class CoroutineEnv {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoroutineEnv.class);

    protected boolean inited = false;
    protected TestCoroExecutor systemExecuter = null;
    protected TestCoroExecutor testCaseExecuter = null;
    protected TestCoroExecutor testCaseExecuter2 = null;


    private static class InstanceHodler {
        private static CoroutineEnv evn = new CoroutineEnv();
    }

    public TestCoroExecutor getSystemExecuter() {
        return systemExecuter;
    }

    public TestCoroExecutor getTestCaseExecuter() {
        return testCaseExecuter;
    }

    public TestCoroExecutor getTestCaseExecuter2() {
        return testCaseExecuter2;
    }

    public static CoroutineEnv getInstance() {
        return InstanceHodler.evn;
    }

    public CoroutineEnv() {
        init();
    }

    private ConcurrentLinkedQueue<Runnable> runSet = new ConcurrentLinkedQueue<>();

    public void addRunnable(Runnable run) {
        runSet.add(run);
    }

    public void init() {
        if (inited) {
            return;
        }
        inited = true;
        // todo xiaolongjia
//        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
//        for (ch.qos.logback.classic.Logger logger : loggerContext.getLoggerList()) {
//            logger.setLevel(Level.DEBUG);
//        }
        LOGGER.error("env init start");
//
//        Properties properties = new Properties();
//        try {
//            properties.load(new FileInputStream("config.test"));
//        } catch (Exception e) {
//            LOGGER.error("no config found ", e);
//        }
        CoroHandle.init();
        systemExecuter = TestCoroExecutor.newInstanceBySystemContainer("system_jobQueue", 1000, 10);
        testCaseExecuter = TestCoroExecutor.newInstanceWithNewContainer("testCase_jobQueue", 1000, 400);
        testCaseExecuter2 = TestCoroExecutor.newInstanceWithNewContainer("testCase2_jobQueue", 1000, 400);
        try {
            systemExecuter.runJob(() -> {
                while (true) {
                    try {

                        CoroHandle.proc();
                        for (Runnable run : runSet) {
                            run.run();
                        }
                        CoroHandle.sleep(1);
                    } catch (Exception e) {
                        LOGGER.error("procThread: ", e);
                    }
                }

            }, "test_case_daemon");
        } catch (Exception e) {
            LOGGER.error("systemExecuter.runJob fail ", e);
        }

        //procThread.start();
        LOGGER.error("env init lock");

        //LockManager.getInstance().init(1);


        LOGGER.error("env init fini");
    }


}
