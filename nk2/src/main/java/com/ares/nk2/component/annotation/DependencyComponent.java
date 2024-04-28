package com.ares.nk2.component.annotation;

import com.ares.nk2.component.DependableInterface;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author pandyxu
 */
@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DependencyComponent {
    Class<? extends DependableInterface>[] value();
}
