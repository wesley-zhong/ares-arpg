package com.ares.nk2.base.coroutine;

import com.ares.nk2.coroutine.CoroHandle;
import com.ares.nk2.util.CoroutineEnv;
import com.ares.nk2.util.TestCoroHandle;
import net.openhft.affinity.Affinity;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class CoroBaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoroBaseTest.class);

    @BeforeClass
    public static void initialise() throws Exception {
        CoroutineEnv.getInstance();
    }

    @Ignore
    @Test
    public void testFlow() throws Exception {
        CoroutineEnv.getInstance().getSystemExecuter().callJob(() -> {
            System.out.println("hello flow");
            return 0;
        }, "hello_test");
    }

    @Ignore
    @Test
    public void testPark() throws Exception {

        CoroutineEnv.getInstance().getSystemExecuter().callJob(() -> {
            long start = System.currentTimeMillis();
            CoroHandle handle = CoroutineEnv.getInstance().getTestCaseExecuter().submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {

                    for (int i = 0; i < 3; ++i) {
                        LOGGER.error("hello testPark " + i + " " + (System.currentTimeMillis() - start));
                        CoroHandle.park(1000);

                    }
                    return null;
                }
            }, "testpark");

            for (int i = 0; i < 3; ++i) {
                //LOGGER.error("unpark sleep " + i + " " + (System.currentTimeMillis() - start));
                CoroHandle.sleep(500);
                //LOGGER.error("unpark " + i + " " + (System.currentTimeMillis() - start));
                handle.unpark();
            }


            handle.get();
            long cost = System.currentTimeMillis() - start;
            System.out.println("hello testPark " + cost);
            Assert.assertTrue(cost < 1600);
            Assert.assertTrue(cost > 1400);

            return 0;
        }, "testPark");


    }

    static long sum = 0;
    static long lastTime;


    //Xeon
    //jku
    // runjob  220w (240w P) (240w P400)
    // calljob 12w (6-10w P) (6-10w P400)


    //coroOnThread
    //runjob 40w
    //calljob 3.5w

    //coroOnThread park 55w
    //runjob 50-70w  (108-125 Affinity) (107-120 AP)  (180w AP400)
    //calljob 3.5w - 4.4w (4.7-5.9w A) (6-7w AP)  (6-7w AP400)

    ///////////////////////////////////////////////////////////
    //i7 0214version jdk
    //jku
    //runjob 350w
    //calljob 12w
    //
    //coroOnThread
    //runjob 120w
    //calljob 6w
    ///////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////
    //mbp 0214version jdk
    //jku
    //runjob 400w  (park 300w)
    //calljob 1.3w ?? (park 1.2w)
    //
    //coroOnThread
    //runjob 11w (park 11w)  14-15w(affinity)
    //calljob 1w (park 1w) 1w
    ///////////////////////////////////////////////////////////
    // 使用 arrayblockingqueue 替换mpscarrayqueue 性能提升40->50w by zcye 2021.4.20
    @Ignore
    @Test
    public void testPerformanceRunMT() throws Exception, InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        final AtomicLong fullCnt = new AtomicLong();

        Thread thread1 = new Thread() {
            @Override
            public void run() {
                try {
                    CoroutineEnv.getInstance().getTestCaseExecuter().runJob(() -> {
//                    if (!lock.tryLock()) {
//                        Assert.fail("ReentrantLock ERROR!!!");
//                    }
                        ++sum;
//                    lock.unlock();
                        return 0;
                    }, "testPerformanceRunMT1");
                } catch (Exception e) {
                    //LOGGER.error("runJob error: ", e);
                    fullCnt.addAndGet(1);
                    try {
                        Thread.sleep(20);
                    } catch (Exception f) {

                    }
                }
            }
        };
        thread1.start();

        while (true) {
            try {
                CoroutineEnv.getInstance().getTestCaseExecuter().runJob(() -> {
//                    if (!lock.tryLock()) {
//                        Assert.fail("ReentrantLock ERROR!!!");
//                    }
                    ++sum;
//                    lock.unlock();

                    if (sum >= 1000111) {
                        long now = System.currentTimeMillis();
                        if (now != lastTime) {
                            System.out.println("tps:" + sum * 1000 / (now - lastTime) + "  affinity:" + Affinity.getCpu() + " "
                                    + Affinity.getAffinity() + " " + Affinity.getThreadId() + "fullcnt:" + fullCnt.get());
                        } else {
                            System.out.println("too fast!");
                        }
                        sum = 0;
                        lastTime = now;
                    }

                    return 0;
                }, "testPerformanceRunMT");
            } catch (Exception e) {
                //LOGGER.error("runJob error: ", e);
                fullCnt.addAndGet(1);
                Thread.sleep(20);
            }
        }
        //Thread.sleep(20);
    }

    @Ignore
    @Test
    public void testPerformanceCallMT() throws Exception, InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        ArrayList<Thread> arrayList = new ArrayList<>();

        for (int i = 0; i < 20; ++i) {
            CoroutineEnv.getInstance().getTestCaseExecuter2().runJob(() -> {
                while (true) {
                    try {
                        CoroutineEnv.getInstance().getTestCaseExecuter().callJob(() -> {
                            if (!lock.tryLock()) {
                                Assert.fail("ReentrantLock ERROR!!!");
                            }
                            ++sum;
                            lock.unlock();
                            if (sum >= 111111) {
                                long now = System.currentTimeMillis();
                                if (now != lastTime) {
                                    System.out.println("tps:" + sum * 1000 / (now - lastTime) + "  affinity:" + Affinity.getCpu() + " "
                                            + Affinity.getAffinity() + " " + Affinity.getThreadId());
                                } else {
                                    System.out.println("too fast!");
                                }
                                sum = 0;
                                lastTime = now;
                            }

                            return 0;
                        }, "testPerformanceCallMT2");
                    } catch (Exception e) {
                        LOGGER.error("", e);

                    }
                }
            }, "testPerformanceCallMT1");

        }
        while (true) {
            Thread.sleep(1);
        }
    }


    //sleep 300w
    //yield 280w

    //only yieldToMain
    //sleep 290w
    //yield 200w


    //Xeon
    //jku
    //run (150W P)   (150W P400)
    //call (140W P)   (150W P400)


    //coroOnThread
    //runjob 30w
    //calljob 4.3w

    //coroOnThread park
    //runjob 35-50w (60-80w Affinity) (70-80W AP) (120W AP400)
    //calljob 4.8w  (9w Affinity) (9w AP)  (9W AP400)


    ///////////////////////////////////////////////////////////
    //i7 0214version jdk
    //jku
    //runjob 150w
    //calljob 160w
    //
    //coroOnThread
    //runjob 85w
    //calljob 9w
    ///////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////
    //mbp 0214version jdk
    //jku
    //runjob 150w (park 180w)
    //calljob 220w (park 220w)
    //
    //coroOnThread
    //runjob 11w (park 11w) 12-13w
    //calljob 8000 (park 8000) 1.3w
    ///////////////////////////////////////////////////////////

    // continuation 140Wtps 2021.4.20 by zcye
    @Ignore
    @Test
    public void testPerformanceRunST() throws Exception, InterruptedException {
        //ReentrantLock lock = new ReentrantLock();
        try {
            CoroutineEnv.getInstance().getTestCaseExecuter().runJob(() -> {
                //System.out.println("callJob:1");
                while (true) {
                    try {
                        //System.out.println("callJob:2");
                        for (int i = 0; i < 10; ++i) { //如果提交一个执行一个，tps是创建协程的性能大约34万/秒
                            CoroutineEnv.getInstance().getTestCaseExecuter().runJob(() -> {
//                                if (!lock.tryLock()) {
//                                    Assert.fail("ReentrantLock ERROR!!!");
//                                }
                                ++sum;
                                //  lock.unlock();
                                if (sum >= 1111110) {
                                    long now = System.currentTimeMillis();
                                    if (now != lastTime) {
                                        System.out.println("tps:" + sum * 1000 / (now - lastTime) + "  affinity:" + Affinity.getCpu() + " "
                                                + Affinity.getAffinity() + " " + Affinity.getThreadId());
                                    } else {
                                        System.out.println("too fast!");
                                    }
                                    sum = 0;
                                    lastTime = now;
                                }

                                return 0;
                            }, "testPerformanceRunST");
                        }
                    } catch (Exception e) {
                        //LOGGER.error("runJob error: ", e);
                        TestCoroHandle.sleep(20);
                    }//
                    TestCoroHandle.yield();
                }
            }, "testPerformanceRunST");
            while (true) {
                //System.out.println("callJob:3");
                Thread.sleep(20);
            }

        } catch (Exception e) {
            LOGGER.error("callJob error: ", e);
            Thread.sleep(20);
        }
    }


    @Ignore
    @Test
    public void testPerformancCallST() throws Exception, InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        try {
            CoroutineEnv.getInstance().getTestCaseExecuter().runJob(() -> {
                //System.out.println("callJob:1");
                while (true) {
                    try {
                        //System.out.println("callJob:2");
                        CoroutineEnv.getInstance().getTestCaseExecuter().callJob(() -> {
                            if (!lock.tryLock()) {
                                Assert.fail("ReentrantLock ERROR!!!");
                            }
                            ++sum;
                            lock.unlock();
                            if (sum >= 1111110) {
                                long now = System.currentTimeMillis();
                                if (now != lastTime) {
                                    System.out.println("tps:" + sum * 1000 / (now - lastTime) + "  affinity:" + Affinity.getCpu() + " "
                                            + Affinity.getAffinity() + " " + Affinity.getThreadId());
                                } else {
                                    System.out.println("too fast!");
                                }
                                sum = 0;
                                lastTime = now;
                            }

                            return 0;
                        }, "xx");
                    } catch (Exception e) {
                        //LOGGER.error("runJob error: ", e);
                        TestCoroHandle.sleep(20);
                    }
                    //TestCoroHandle.yield();
                }
            }, "xxx");
            while (true) {
                //System.out.println("callJob:3");
                Thread.sleep(20);
            }

        } catch (Exception e) {
            LOGGER.error("callJob error: ", e);
            Thread.sleep(20);
        }
    }


    //sleep 300w
    //yield 280w

    public static void main(String[] args) throws Exception {
        //testPerformanceRunST();
    }

}
