package com.ares.nk2.component.annotation;

import com.ares.nk2.component.BaseEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jeffreyzhou
 * @date 2021/7/2
 */
@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentRoot {
    Class<? extends BaseEntity> entity();
}
