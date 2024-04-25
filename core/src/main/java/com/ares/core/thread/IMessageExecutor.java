package com.ares.core.thread;


import com.ares.core.bean.AresMsgIdMethod;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.thread.task.EventBiFunction;
import com.ares.core.thread.task.EventFunction;
import com.ares.core.thread.task.EventThFunction;

/**
 * 消息处理器
 */
public interface IMessageExecutor {
    /**
     * 启动消息处理器
     */
    void start();

    /**
     * 停止消息处理器
     */
    void stop();


    /**
     * 判断队列是否已经达到上限了
     *
     * @return
     */
    boolean isFull();


    /**
     * 执行任务
     */
    void execute(AresTKcpContext aresTKcpContext, AresMsgIdMethod method, long param1, Object param2);

    <T1,T2, T3> void execute(T1 p1, T2 p2, T3 p3, EventThFunction <T1, T2, T3> method);
    <T> void execute(long p1, T p2, EventBiFunction<T> method);
    <T> void execute(T p2, EventFunction<T> method);
}