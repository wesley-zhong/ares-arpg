package com.ares.nk2.base.coroutine;

import org.junit.Ignore;
import org.junit.Test;

public class CoroMemTest {
    @Ignore
    @Test
    public void performanceJavaApiYieldto() {
        while (true) {
            Thread thread = new Thread() {
                @Override
                public void run() {

                }
            };
            thread.start();
            try {
                thread.join();
            } catch (Exception e) {

            }
        }
    }
}
