package com.ares.core.thread.task;

import com.ares.core.thread.EventTask;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class TaskThEventTask<T1, T2, T3> implements EventTask {
    private T1 p1;
    private T2 p2;
    private T3 p3;
    private EventThFunction<T1, T2, T3> function;

    @Override
    public void execute() {
        try {
            function.apply(p1, p2, p3);
        } catch (Exception e) {
            log.error("----error", e);
        }
    }

    @Override
    public void clear() {
        function = null;
        p1 = null;
        p2 = null;
        p3 = null;
    }
}
