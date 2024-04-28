package com.ares.nk2.base.coroutine;

import com.ares.nk2.coroutine.CoroHandle;
import com.ares.nk2.util.CoroutineEnv;
import org.jctools.queues.MpscArrayQueue;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;


public class CoroPerformance {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoroPerformance.class);
    boolean needclean = false;

    @BeforeClass
    public static void initialise() throws Exception {
        CoroutineEnv.getInstance();
    }

    CoroHandle coroHandleParkPT1 = null;
    CoroHandle coroHandleParkPT2 = null;
    volatile long countForParkPT = 0;
    long startTimeForParkPT = 0;


    static class Stater {
        AtomicLong count = new AtomicLong();
        boolean needClear = false;
        String name;

        void inc() {
            count.incrementAndGet();
        }

        Stater(String name) {
            this.name = name;
        }

        void checkClear() {
            if (needClear) {
                count.set(0);
                needClear = false;
            }
        }

        void dump() {
            //只输出1000秒
            for (int i = 0; i < 3000; ++i) {
                try {
                    Thread.sleep(1000);
                    LOGGER.error("tps {} {}", name, count);
                    needClear = true;
                    checkClear();

                } catch (Exception e) {

                }
            }
        }
    }

    private void foo() {
//        Thread.sleep(50);
    }

    private void bar() {
//        Thread.sleep(50);
    }

    @Ignore
    @Test
    public void performanceYield() throws Exception {
        Stater performanceYieldStater = new Stater("performanceYield");
//
        CoroutineEnv.getInstance().getTestCaseExecuter().runJob(() -> {
            while (true) {
                foo();
                CoroHandle.yield();
                performanceYieldStater.inc();
            }
        }, "foo");
        CoroutineEnv.getInstance().getTestCaseExecuter().runJob(() -> {
            while (true) {
                bar();
                CoroHandle.yield();
                performanceYieldStater.inc();
            }
        }, "bar");
        performanceYieldStater.dump();
    }


    static class myCallable2 implements Callable {
        ArrayList<CoroHandle> coroArr1;
        int index;
        Stater performanceParkPTStater;

        myCallable2(ArrayList coroArr, int index, Stater stater) {
            coroArr1 = coroArr;
            this.index = index;
            performanceParkPTStater = stater;
        }

        @Override
        public Object call() {
            try {
                try {
                    CoroHandle.park(100);
                } catch (Exception e) {

                }
                while (true) {
                    performanceParkPTStater.checkClear();
                    coroArr1.get(index).unpark();
                    CoroHandle.park();
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
            return null;
        }
    }

    static class myCallable1 implements Callable {
        ArrayList<CoroHandle> coroArr2;
        int index;
        Stater performanceParkPTStater;

        myCallable1(ArrayList coroArr, int index, Stater stater) {
            coroArr2 = coroArr;
            this.index = index;
            performanceParkPTStater = stater;
        }

        @Override
        public Object call() {
            try {
                //CoroHandle.park();
                while (true) {
                    CoroHandle.park();
                    performanceParkPTStater.inc();
                    coroArr2.get(index).unpark();
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
            return null;
        }
    }

    @Ignore
    @Test
    public void testMpscContainer() throws Exception {
        Stater testMpscContainer = new Stater("testMpscContainer");

        Queue<Integer> arrayQueue = new MpscArrayQueue<>(1000000);//new ArrayBlockingQueue<Integer>(1000000);
        Integer test = 1;
        Thread thread1 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        arrayQueue.offer(test);
                        testMpscContainer.inc();
                    } catch (Exception e) {
                        try {
                            Thread.sleep(1);
                        } catch (Exception d) {

                        }
                    }
                }
            }
        };
        thread1.start();

        Thread thread2 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (!arrayQueue.isEmpty()) {
                        arrayQueue.poll();
                    }
                    testMpscContainer.checkClear();
                }

            }
        };
        thread2.start();
        testMpscContainer.dump();

    }

    @Ignore
    @Test
    public void performanceParkPT() throws Exception {
        Stater performanceParkPTStater = new Stater("performanceParkPT");

        CoroutineEnv.getInstance().getSystemExecuter().runJob(() -> {
            ArrayList<CoroHandle> coroArr1 = new ArrayList<>();
            ArrayList<CoroHandle> coroArr2 = new ArrayList<>();
            int total = 20;
            for (int i = 0; i < total; ++i) {
                CoroHandle coroHandle = CoroutineEnv.getInstance().getTestCaseExecuter().submit(new myCallable1(coroArr2, i, performanceParkPTStater), "test1" + i);
                coroArr1.add(coroHandle);
            }
            for (int i = 0; i < total; ++i) {
                //CoroutineMgr.getInstance().setTracerName("test20");
                CoroHandle coroHandle = CoroutineEnv.getInstance().getTestCaseExecuter().submit(new myCallable2(coroArr1, i, performanceParkPTStater), "test2" + i);
                coroArr2.add(coroHandle);
            }
            return null;
        }, "test");
        performanceParkPTStater.dump();
    }

    CoroHandle coroHandleParkST1 = null;
    CoroHandle coroHandleParkST2 = null;


    //zcye park st
    @Ignore
    @Test
    public void performanceParkST() throws Exception {
        Stater performanceParkSTStater = new Stater("performanceParkST");

        CoroutineEnv.getInstance().getSystemExecuter().runJob(() -> {
            coroHandleParkST1 = CoroutineEnv.getInstance().getSystemExecuter().submit(() -> {

                CoroHandle.park();
                while (true) {
                    coroHandleParkST2.unpark();
                    performanceParkSTStater.inc();
                    CoroHandle.park();
                }
            }, "performanceParkST1");
            coroHandleParkST2 = CoroutineEnv.getInstance().getSystemExecuter().submit(() -> {
                CoroHandle.sleep(1);
                while (true) {
                    coroHandleParkST1.unpark();
                    CoroHandle.park();
                    performanceParkSTStater.checkClear();
                }
            }, "performanceParkST2");
            return null;
        }, "performanceParkST3");
        performanceParkSTStater.dump();
    }

    long countForRunPt = 0;

    //跨线程submit
    @Ignore
    @Test
    public void performanceSubmitPT() throws Exception, InterruptedException {

        Stater performanceSubmitPTStater = new Stater("performanceSubmitPT");
        Thread thread = new Thread() {
            @Override
            public void run() {
                performanceSubmitPTStater.dump();
            }
        };
        thread.start();

        while (true) {
            try {
                CoroutineEnv.getInstance().getTestCaseExecuter().runJob(() -> {
                    performanceSubmitPTStater.inc();
                    performanceSubmitPTStater.checkClear();
                    return null;
                }, "performanceSubmitPT");
            } catch (Exception e) {
                Thread.sleep(10);
            }
            if (!thread.isAlive()) {
                break;
            }
        }
    }

    //跨线程join/get
    @Ignore
    @Test
    public void performanceGetPT() throws Exception, InterruptedException {
        Stater performanceGetPTStater = new Stater("performanceGetPT");
        CoroutineEnv.getInstance().getSystemExecuter().runJob(() -> {
            long count = 0;
            long starttime = System.currentTimeMillis();
            for (int i = 0; i < 100; ++i) {
                CoroutineEnv.getInstance().getSystemExecuter().runJob(() -> {
                    while (true) {
                        CoroHandle coroHandle = CoroutineEnv.getInstance().getTestCaseExecuter().submit(() -> {
                            return null;
                        }, "performanceGetPTinner");
                        coroHandle.get();
                        performanceGetPTStater.inc();
                        performanceGetPTStater.checkClear();
                    }
                }, "performanceGetPT");
            }
            return null;
        }, "");
        performanceGetPTStater.dump();
    }

    long countForRunSt = 0;
    int performanceSubmitSTCnt = 0;

    //zcye get submit st
    @Ignore
    @Test
    public void performanceSubmitST() throws Exception, InterruptedException {
        Stater performanceSubmitSTStater = new Stater("performanceSubmitST");
        CoroutineEnv.getInstance().getTestCaseExecuter().runJob(() -> {
            while (true) {
                CoroHandle cur = CoroHandle.current();
                for (int i = 0; i < 1000; ++i) {
                    CoroutineEnv.getInstance().getTestCaseExecuter().submit(() -> {
                        performanceSubmitSTStater.inc();
                        performanceSubmitSTStater.checkClear();
                        performanceSubmitSTCnt++;
                        if (performanceSubmitSTCnt == 1000) {
                            //LOGGER.error("unpark {} {} {}", cur, performanceSubmitSTStater.count, CoroHandle.current());
                            performanceSubmitSTCnt = 0;
                            cur.unpark();
                        }
                        return null;
                    }, "performanceSubmitSTinner");
                }
                CoroHandle.park();
            }
        }, "performanceSubmitST");
        performanceSubmitSTStater.dump();
    }

    //zcye get st
    @Ignore
    @Test
    public void performanceGetST() throws Exception, InterruptedException {
        Stater performanceGetSTStater = new Stater("performanceGetST");
        //CoroutineMgr.getInstance().setTracerName("getst");
        //CoroutineMgr.getInstance().setTracerName("getstinner");
        CoroutineEnv.getInstance().getTestCaseExecuter().runJob(() -> {
            try {
//                CoroutineEnv.getInstance().getTestCaseExecuter().onlyForTestSubmit(() -> {
//                    while (true) {
//                        LOGGER.error("thread run {}", Thread.currentThread());
//                        LockSupport.parkNanos(2000000000);
//                    }
//                    //return null;
//                }, "monitor", "", false);
                //LOGGER.error("thread name {}", Thread.currentCarrierThread());
                while (true) {
                    CoroHandle coroHandle = CoroutineEnv.getInstance().getTestCaseExecuter().submit(() -> {
                        // CoroutineMgr.getInstance().msgqueue.offer(NKStringFormater.format("thread run {}", Thread.currentThread()));
                        return null;
                    }, "getstinner");
                    coroHandle.get();
                    performanceGetSTStater.inc();
                    performanceGetSTStater.checkClear();
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
            return null;
        }, "getst");
        performanceGetSTStater.dump();
    }
}
