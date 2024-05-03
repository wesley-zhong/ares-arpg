package com.ares.discovery.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AresAsynTransport {
    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";

    String qualifier() default "";

    String url() default "";

    boolean primary() default true;
}
