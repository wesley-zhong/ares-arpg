package com.ares.core.event;

@FunctionalInterface
public interface EventHandler<T extends Event> {
    void handle(T event);
}