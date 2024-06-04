package com.ares.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

//@ThreadSafe
public abstract class Event {
    private static final Logger LOGGER = LoggerFactory.getLogger(Event.class);
//    private static final Map<Long, Map<Object, Event>> QUEUE = new ConcurrentHashMap<>();
    private static final Map<Object, Event> QUEUE = new IdentityHashMap<>();
    protected Queue<Event> queue = null;

    /**
     * 会用'=='比较
     *
     * @return 返回投递的目标对象
     */
    public abstract Object getObject();

    /**
     * 重入数据排队
     */
    @SuppressWarnings("unchecked")
    public EventDispatchResult dispatch() {
//        CoroHandle<?> coro = CoroHandle.current();
//        if (coro == null) {
//            LOGGER.error("event dispatch error, coro is empty");
//            return EventDispatchResult.Error;
//        }
        Object object = getObject();
        if (object == null) {
            LOGGER.error("event dispatch error, target object is empty {}", getClass());
            return EventDispatchResult.Error;
        }
//        不同线程的协程id不会重复(同一个协程的同一个object的event放在同一个队列里)
//        Event event = QUEUE.computeIfAbsent(coro.getCoroBaseInfo().coroutineId(), k -> new IdentityHashMap<>(4)).putIfAbsent(object, this);
        Event event = QUEUE.putIfAbsent(object, this);
        //object在当前协程有event在分发中,入队,返回
        if (event != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("event {} dispatch later", this);
            }
            if (event.queue == null) {
                event.queue = new LinkedList<>();
            }
            //重入数据排队
            event.queue.offer(this);
            return EventDispatchResult.Queue;
        }
        //object在当前协程没有event在分发中,分发,分发完成后继续分发队列中的event
        event = this;
        do {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("event {} dispatch start", event);
            }
            Collection<EventHandler<?>> handlers = EventHandlerHolder.getHandlers(event);
            if (handlers != null && !handlers.isEmpty()) {
                for (EventHandler handler : handlers) {
                    try {
                        handler.handle(event);
                    } catch (Exception e) {
                        LOGGER.error("", e);
                    }
                }
            }
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("event {} dispatch finish", event);
            }
        } while (queue != null && (event = queue.poll()) != null);
//        QUEUE.computeIfPresent(coro.getCoroBaseInfo().coroutineId(), (k, v) -> {
//            v.remove(object);
//            return v.isEmpty() ? null : v;
//        });
        QUEUE.remove(object);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("event queue size {}", QUEUE.size());
        }
        // TODO 队列size监控
        return EventDispatchResult.Done;
    }

    @SuppressWarnings("unchecked")
    public EventDispatchResult dispatchDirectly() {
        Collection<EventHandler<?>> handlers = EventHandlerHolder.getHandlers(this);
        if (handlers != null && !handlers.isEmpty()) {
            for (EventHandler handler : handlers) {
                try {
                    handler.handle(this);
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            }
        }
        return EventDispatchResult.Done;
    }


    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
