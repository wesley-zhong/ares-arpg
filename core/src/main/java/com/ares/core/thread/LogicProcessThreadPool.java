package com.ares.core.thread;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogicProcessThreadPool extends AresThreadPool {

    public static LogicProcessThreadPool create(int logicAysnThreadCount) {
        return new LogicProcessThreadPool(logicAysnThreadCount);
    }

    public LogicProcessThreadPool(int logicAysnThreadCount) {
        super(new AresThreadFactory("a-l-t-P-"), logicAysnThreadCount);
    }
}
