package com.ares.dal.redis;

public interface RedisTaskCallBack<T> {
    int onTask(T task);
}
