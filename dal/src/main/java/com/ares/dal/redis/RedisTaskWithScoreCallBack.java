package com.ares.dal.redis;

public interface RedisTaskWithScoreCallBack<T> {
    int onTask(T task, long score);
}
