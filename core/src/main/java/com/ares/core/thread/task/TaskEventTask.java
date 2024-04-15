package com.ares.core.thread.task;

import com.ares.core.thread.EventTask;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskEventTask<T> implements EventTask {
    private T  p;
    private EventFunction<T> function;
    @Override
    public void execute() {
        function.apply(p);
    }

    @Override
    public void clear() {
        p = null;
        function = null;
    }
}
