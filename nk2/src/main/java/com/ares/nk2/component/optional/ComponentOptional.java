package com.ares.nk2.component.optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class ComponentOptional<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentOptional.class);
    private static final ComponentOptional<?> EMPTY = new ComponentOptional<>();
    private boolean check = false;

    protected final T value;

    public ComponentOptional() {
        value = null;
    }

    public ComponentOptional(T value) {
        this.value = value;
    }

    public static <T> ComponentOptional<T> of(T value) {
        return new ComponentOptional<>(value);
    }

    public static <T> ComponentOptional<T> empty() {
        @SuppressWarnings("unchecked")
        ComponentOptional<T> t = (ComponentOptional<T>) EMPTY;
        return t;
    }

    public T get() {
//        boolean pressTest = false;
//        if (ServerEngine.getInstance() != null && ServerEngine.getInstance().isPressTest()) {
//            pressTest = true;
//        }
//
//        if (!pressTest && !check) {
//            WechatLog.debugPanicLog("ComponentOptional, isNull should be called before get, caller: {}", FunctionUtil.getCallerInfo(0));
//        }
        return value;
    }

    public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (isNull()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(mapper.apply(value));
        }
    }

    public boolean isNull() {
        check = true;
        return value == null;
    }
    
    public boolean isPresent() {
        return !isNull();
    }

    public void ifPresent(Consumer<? super T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }
}
