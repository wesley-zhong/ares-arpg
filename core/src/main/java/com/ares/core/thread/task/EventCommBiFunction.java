package com.ares.core.thread.task;

public interface EventCommBiFunction<T1, T2> {
    void apply(T1 p1, T2 p2);
}
