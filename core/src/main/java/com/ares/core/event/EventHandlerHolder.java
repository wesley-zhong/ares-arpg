package com.ares.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventHandlerHolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandlerHolder.class);
    private static final Map<String, List<EventHandler<? extends Event>>> HANDLERS = new ConcurrentHashMap<>();

    public static <T extends Event> void register(Class<T> event, EventHandler<T> handler) {
        HANDLERS.compute(event.getName(), (key, value) -> {
            if (value == null) {
                value = new LinkedList<>();
                value.add(handler);
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("handler {} listening event {} success(new)", handler.getClass().getSimpleName(), event.getSimpleName());
                }
                return value;
            }
            for (EventHandler<?> val : value) {
                if (val.getClass().equals(handler.getClass())) {
                    return value;
                }
            }
            value.add(handler);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("handler {} listening event {} success", handler.getClass().getSimpleName(), event.getSimpleName());
            }
            return value;
        });
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("event {} has bind handlers number {}",
                    handler.getClass().getSimpleName(),
                    HANDLERS.getOrDefault(event.getName(), Collections.emptyList()).size());
        }
    }

    static Collection<EventHandler<? extends Event>> getHandlers(Event event) {
        return HANDLERS.getOrDefault(event.getClass().getName(), null);
    }

    static Collection<EventHandler<? extends Event>> getHandlersByClass(Class<? extends Event> clazz) {
        return HANDLERS.getOrDefault(clazz.getName(), null);
    }
}
