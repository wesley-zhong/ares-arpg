package com.ares.discovery.annotation;


import com.ares.discovery.support.AresDiscoveryConfigure;
import com.ares.discovery.support.AresRpcClientRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({AresDiscoveryConfigure.class, AresRpcClientRegister.class})
public @interface EnableAresDiscovery {
    String[] value() default {};

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    Class<?>[] defaultConfiguration() default {};

    Class<?>[] clients() default {};
}
