package com.ares.core.thread.task;

public interface EventThFunction<T1, T2, T3> {
    void apply(T1 p1, T2 p2, T3 p3);
}
