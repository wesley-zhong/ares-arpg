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
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

public class CoroBaseExtTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoroBaseExtTest.class);

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
        }, "testFlow");
    }

    static long sum = 0;
    static long lastTime;
    final static int yieldCountOneJob = 3;
    final static int callerThreadCount = 3;
    final ReentrantLock lock = new ReentrantLock();


    public void testOnePass(ReentrantLock lock, int checkInterval) throws TimeoutException, InterruptedException {
        for (int i = 0; i < yieldCountOneJob; ++i) {
            if (!lock.tryLock()) {
                Assert.fail("ReentrantLock ERROR!!!");
            }
            ++sum;
            lock.unlock();
            CoroHandle.yield();
        }

        if (sum >= checkInterval) {
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
    }

    // linux 3  3
    // jku
    // !always_yield_to_main yield: 500w
    // always_yield_to_main yield: 250w
    // !always_yield_to_main sleep: 91w
    // always_yield_to_main sleep: 90w

    // COT
    // tps : 12w
    // !always_yield_to_main yield: 350w
    // always_yield_to_main yield: 13w
    // !always_yield_to_main sleep: 18w
    // always_yield_to_main sleep: 12w


    @Ignore
    @Test
    public void testPerformanceRunMT() throws Exception, InterruptedException {
        System.out.println(System.getProperty("java.runtime.version"));
        ArrayList<Thread> tList = new ArrayList<>(callerThreadCount);
        for (int i = 0; i < callerThreadCount; ++i) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            CoroutineEnv.getInstance().getTestCaseExecuter().runJob(() -> {
                                testOnePass(lock, 1000111);
                                return 0;
                            }, "");
                        } catch (Exception e) {
                            //LOGGER.error("runJob error: ", e);
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            });
            tList.add(t);
        }
        for (int i = 0; i < callerThreadCount; ++i) {
            tList.get(i).start();
        }
        for (int i = 0; i < callerThreadCount; ++i) {
            tList.get(i).join();
        }
        //Thread.sleep(20);
    }

    // linux 3  3
    // jku
    // fake yield: 45w
    // sleep: 2000

    // COT
    // sleep: 2000


    @Ignore
    @Test
    public void testPerformanceCallMT() throws Exception, InterruptedException {
        System.out.println(System.getProperty("java.runtime.version"));

        ArrayList<Thread> tList = new ArrayList<>(callerThreadCount);
        for (int i = 0; i < callerThreadCount; ++i) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            CoroutineEnv.getInstance().getTestCaseExecuter().callJob(() -> {
                                testOnePass(lock, 1001);
                                return 0;
                            }, "testPerformanceCallMT");
                        } catch (Exception e) {
                            //LOGGER.error("runJob error: ", e);
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            });
            tList.add(t);
        }
        for (int i = 0; i < callerThreadCount; ++i) {
            tList.get(i).start();
        }
        for (int i = 0; i < callerThreadCount; ++i) {
            tList.get(i).join();
        }
    }

    // linux 1000
    // jku
    // fake yield:
    // sleep: 20-30w

    // COT
    // sleep: 10w

    @Ignore
    @Test
    public void testPerformanceCallMC() throws Exception, InterruptedException {
        System.out.println(System.getProperty("java.runtime.version"));

        for (int i = 0; i < 999; ++i) {
            try {
                CoroutineEnv.getInstance().getTestCaseExecuter2().runJob(() -> {
                    while (true) {
                        try {
                            CoroutineEnv.getInstance().getTestCaseExecuter().callJob(() -> {
                                testOnePass(lock, 100001);
                                return 0;
                            }, "testPerformanceCallMCinner");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, "testPerformanceCallMC");
            } catch (Exception e) {
                LOGGER.error("getTestCaseExecuter2 runJob error: ", e);
                //Thread.sleep(20);
            }
        }
        while (true) {
            Thread.sleep(20);
        }
    }

    // linux 1000
    // jku
    // fake yield:
    // sleep: 30w

    // COT
    // sleep: 11w

    @Ignore
    @Test
    public void testPerformanceRunMC() throws Exception, InterruptedException {
        System.out.println(System.getProperty("java.runtime.version"));

        for (int i = 0; i < 999; ++i) {
            try {
                CoroutineEnv.getInstance().getTestCaseExecuter2().runJob(() -> {
                    while (true) {
                        try {
                            CoroutineEnv.getInstance().getTestCaseExecuter().runJob(() -> {
                                testOnePass(lock, 100001);
                                return 0;
                            }, "testPerformanceRunMCinner");
                        } catch (Exception e) {
                            //e.printStackTrace();
                            TestCoroHandle.sleep(20);
                        }
                    }
                }, "testPerformanceRunMC");
            } catch (Exception e) {
                LOGGER.error("getTestCaseExecuter2 runJob error: ", e);
                //Thread.sleep(20);
            }
        }
        while (true) {
            Thread.sleep(20);
        }
    }

    // linux
    // jku
    // fake yield:
    // sleep: 35w

    // COT
    // sleep: 12w

    @Ignore
    @Test
    public void testPerformanceRunST() throws Exception, InterruptedException {
        try {
            CoroutineEnv.getInstance().getTestCaseExecuter().runJob(() -> {
                //System.out.println("callJob:1");
                while (true) {
                    try {
                        //System.out.println("callJob:2");
                        CoroutineEnv.getInstance().getTestCaseExecuter().runJob(() -> {
                            testOnePass(lock, 100011);

                            return 0;
                        }, "testPerformanceRunSTinner");
                    } catch (Exception e) {
                        //LOGGER.error("runJob error: ", e);
                        TestCoroHandle.sleep(20);
                    }
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


    // linux 1  3
    // jku
    // fake yiled: 350w
    // sleep: 1000

    // COT:
    // sleep : 928

    @Ignore
    @Test
    public void testPerformancCallST() throws Exception, InterruptedException {
        try {
            CoroutineEnv.getInstance().getTestCaseExecuter().runJob(() -> {
                //System.out.println("callJob:1");
                while (true) {
                    try {
                        //System.out.println("callJob:2");
                        CoroutineEnv.getInstance().getTestCaseExecuter().callJob(() -> {
                            testOnePass(lock, 100011);
                            return 0;
                        }, "testPerformancCallSTinner");
                    } catch (Exception e) {
                        //LOGGER.error("runJob error: ", e);
                        TestCoroHandle.sleep(20);
                    }
                    TestCoroHandle.yield();
                }
            }, "testPerformancCallST");
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
