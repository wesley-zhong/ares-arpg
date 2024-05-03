package com.ares.core.thread.task;

import com.ares.core.thread.EventTask;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class TaskBiCommEventTask<T1, T2> implements EventTask {
    private T1 p1;
    private T2 p2;
    private EventCommBiFunction<T1, T2> function;

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
