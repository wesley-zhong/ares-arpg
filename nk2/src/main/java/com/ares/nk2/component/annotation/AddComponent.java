package com.ares.nk2.component.annotation;

import com.ares.nk2.component.BaseComponent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author timixu
 * @date 2021/1/21 16:26
 */
@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AddComponent {
    Class<? extends BaseComponent>[] value();
}
