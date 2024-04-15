package com.ares.core.thread.task;

import com.ares.core.thread.EventTask;
import lombok.Setter;

@Setter
public class TaskBiEventTask<T> implements EventTask {
    private long p1;
    private T  p2;
    private EventBiFunction<T> function;
    @Override
    public void execute() {
        function.apply(p1, p2);
    }

    @Override
    public void clear() {
        p2 = null;
        function = null;
    }
}
