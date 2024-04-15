package com.ares.core.utils;

import com.ares.core.json.transcoder.JsonObjectMapper;
import com.fasterxml.jackson.databind.JavaType;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    public static JavaType createListTypeByClass(Class<?> objClass) {
        return JsonObjectMapper.getInstance().getTypeFactory().constructParametricType(List.class, (Class<?>) objClass);
    }


    public static JavaType createMapTypeByClass(Class<?> keyClass, Class<?> valueClass) {
        return JsonObjectMapper.getInstance().getTypeFactory().constructParametricType(Map.class, keyClass, valueClass);
    }

    public static JavaType createJavaType(Type type) {
        return JsonObjectMapper.getInstance().constructType(type);
    }

    public static String toJsonString(Object object){
        try {
            return JsonObjectMapper.getInstance().writeValueAsString(object);
        }catch (Exception e){

        }
        return null;
    }

}
