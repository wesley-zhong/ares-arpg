package com.ares.core.thread.task;

import com.ares.core.thread.EventTask;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskThEventTask<T1, T2, T3> implements EventTask {
    private T1 p1;
    private T2 p2;
    private T3 p3;
    private EventThFunction<T1, T2, T3> function;

    @Override
    public void execute() {
        function.apply(p1, p2, p3);
    }

    @Override
    public void clear() {
        p1 = null;
        p2 = null;
        p3 = null;
    }
}
