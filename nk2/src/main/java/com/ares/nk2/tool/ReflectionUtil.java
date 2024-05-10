package com.ares.nk2.tool;

import org.reflections8.Reflections;
import org.reflections8.scanners.SubTypesScanner;
import org.reflections8.scanners.TypeAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

public class ReflectionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtil.class);

    public static <T> Set<Class<? extends T>> getPrefixSubtypeClazzSet(String prefix, Class<T> clazz) {
        Set<Class<? extends T>> clazzSet = new Reflections(prefix, new SubTypesScanner(false)).getSubTypesOf(clazz);
        clazzSet.removeIf(subtypeClazz -> !(subtypeClazz.getPackage().getName().startsWith(prefix)));
        return clazzSet;
    }

    public static <T> Set<Class<?>> getPrefixAnnotatedWithClazzSet(String prefix, final Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(prefix, new SubTypesScanner(false), new TypeAnnotationsScanner());
        Set<Class<?>> clazzSet = reflections.getTypesAnnotatedWith(annotation);
        clazzSet.removeIf(annotationClazz -> !(annotationClazz.getPackage().getName().startsWith(prefix)));
        return clazzSet;
    }

    public static boolean isOverriden(Method parent, Method toCheck) {
        if (parent.getDeclaringClass().isAssignableFrom(toCheck.getDeclaringClass())
                && parent.getName().equals(toCheck.getName())) {
            Class<?>[] params1 = parent.getParameterTypes();
            Class<?>[] params2 = toCheck.getParameterTypes();
            if (params1.length == params2.length) {
                for (int i = 0; i < params1.length; i++) {
                    if (!params1[i].equals(params2[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isClazzAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            LOGGER.error("failed to get getConstructor of clazz: {}", clazz.getSimpleName());
            return null;
        }
    }
}
