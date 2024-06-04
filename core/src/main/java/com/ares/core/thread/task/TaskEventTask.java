package com.ares.core.thread.task;

import com.ares.core.thread.EventTask;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class TaskEventTask<T> implements EventTask {
    private T p;
    private EventFunction<T> function;

    @Override
    public void execute() {
        try {
            function.apply(p);
        } catch (Exception e) {
            log.error("----error", e);
        }
    }

    @Override
    public void clear() {
        p = null;
        function = null;
    }
}
