package com.ares.core.thread.task;

import com.ares.core.thread.EventTask;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
public class TaskBiEventTask<T> implements EventTask {
    private long p1;
    private T p2;
    private EventBiFunction<T> function;

    @Override
    public void execute() {
        try {
            function.apply(p1, p2);
        } catch (Exception e) {
            log.error("error", e);
        }
    }

    @Override
    public void clear() {
        p2 = null;
        function = null;
    }
}
