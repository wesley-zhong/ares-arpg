package com.ares.core.thread;

import com.ares.core.bean.AresMsgIdMethod;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.thread.task.*;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.TimeoutBlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DisruptorSingleExecutor implements IMessageExecutor {

    //65536条消息
    private final int MAX_QUE_SIZE = 1 << 13;// 2 << 15;

    private final RingBuffer<AresEventProcess> ringBuffer;
    private final Disruptor<AresEventProcess> disruptor;


    public DisruptorSingleExecutor(ThreadFactory threadFactory) {
        disruptor = new Disruptor<>(new AresEventFactory(), MAX_QUE_SIZE, threadFactory, ProducerType.MULTI, new TimeoutBlockingWaitStrategy(10, TimeUnit.MILLISECONDS));
        disruptor.handleEventsWith(new AresEventHandler());
        ringBuffer = disruptor.getRingBuffer();
    }


    @Override
    @SuppressWarnings("unchecked")
    public void start() {
        disruptor.start();
    }

    @Override
    public void stop() {
        disruptor.shutdown();
    }


    @Override
    public boolean isFull() {
        return !ringBuffer.hasAvailableCapacity(1);
    }

    @Override
    public void execute(AresTKcpContext aresTKcpContext, AresMsgIdMethod method, long param1, Object param2) {
        try {
            final long sequence = ringBuffer.tryNext();
            try {
                PacketEventTask packetEventTask = new PacketEventTask();
                packetEventTask.setAresTKcpContext(aresTKcpContext);
                packetEventTask.setMethod(method);
                packetEventTask.setParam1(param1);
                packetEventTask.setParam2(param2);
                packetEventTask.setMsgHeader(aresTKcpContext.getRcvPackage().getRecvHeader());
                AresEventProcess aresEventProcess = ringBuffer.get(sequence);
                aresEventProcess.setEventTask(packetEventTask);
            } finally {
                ringBuffer.publish(sequence);
            }
        } catch (Exception e) {
            // This exception is used by the Disruptor as a global goto. It is a singleton
            // and has no stack trace.  Don't worry about performance.
            log.error("Logic thread disruptor buff is error ", e);
        }
    }

    @Override
    public <T1, T2, T3> void execute(T1 p1, T2 p2, T3 p3, EventThFunction<T1, T2, T3> method) {
        try {
            final long sequence = ringBuffer.tryNext();
            try {
                TaskThEventTask<T1, T2, T3> taskBiEventTask = new TaskThEventTask<>();
                taskBiEventTask.setP1(p1);
                taskBiEventTask.setP2(p2);
                taskBiEventTask.setP3(p3);
                taskBiEventTask.setFunction(method);
                AresEventProcess aresEventProcess = ringBuffer.get(sequence);
                aresEventProcess.setEventTask(taskBiEventTask);
            } finally {
                ringBuffer.publish(sequence);
            }
        } catch (Exception e) {
            // This exception is used by the Disruptor as a global goto. It is a singleton
            // and has no stack trace.  Don't worry about performance.
            log.error("Logic thread disruptor buff is error", e);
        }

    }

    @Override
    public <T> void execute(long p1, T p2, EventBiFunction<T> method) {
        try {
            final long sequence = ringBuffer.tryNext();
            try {
                TaskBiEventTask<T> taskBiEventTask = new TaskBiEventTask<>();
                taskBiEventTask.setP1(p1);
                taskBiEventTask.setP2(p2);
                taskBiEventTask.setFunction(method);
                AresEventProcess aresEventProcess = ringBuffer.get(sequence);
                aresEventProcess.setEventTask(taskBiEventTask);
            } finally {
                ringBuffer.publish(sequence);
            }
        } catch (Exception e) {
            // This exception is used by the Disruptor as a global goto. It is a singleton
            // and has no stack trace.  Don't worry about performance.
            log.error("Logic thread disruptor buff is error", e);
        }
    }

    @Override
    public <T> void execute(T p, EventFunction<T> method) {
        try {
            final long sequence = ringBuffer.tryNext();
            try {
                TaskEventTask<T> taskEventTask = new TaskEventTask<>();
                taskEventTask.setP(p);
                taskEventTask.setFunction(method);
                AresEventProcess aresEventProcess = ringBuffer.get(sequence);
                aresEventProcess.setEventTask(taskEventTask);
            } finally {
                ringBuffer.publish(sequence);
            }
        } catch (Exception e) {
            // This exception is used by the Disruptor as a global goto. It is a singleton
            // and has no stack trace.  Don't worry about performance.
            log.error("Logic thread disruptor buff is error", e);
        }
    }

    @Override
    public <T1, T2> void execute(T1 p1, T2 p2, EventCommBiFunction<T1, T2> method) {
        try {
            final long sequence = ringBuffer.tryNext();
            try {
                TaskBiCommEventTask<T1, T2> taskEventTask = new TaskBiCommEventTask<>();
                taskEventTask.setP1(p1);
                taskEventTask.setP2(p2);
                taskEventTask.setFunction(method);
                AresEventProcess aresEventProcess = ringBuffer.get(sequence);
                aresEventProcess.setEventTask(taskEventTask);
            } finally {
                ringBuffer.publish(sequence);
            }
        } catch (Exception e) {
            // This exception is used by the Disruptor as a global goto. It is a singleton
            // and has no stack trace.  Don't worry about performance.
            log.error("Logic thread disruptor buff is error", e);
        }
    }
}
