

package com.ares.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Index {
    String value() default "";

    String prefix() default "";

    String suffix() default "";
}

