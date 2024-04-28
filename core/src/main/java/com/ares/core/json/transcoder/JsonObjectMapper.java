package com.ares.core.json.transcoder;
import com.ares.core.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import java.io.DataInput;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


/**
 * @author wesley
 */
public class JsonObjectMapper {

    public static final ObjectMapper objectMapper = JsonObjectMapper.createInstance();

    private static ObjectMapper createInstance() {
        return new ObjectMapper().
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).
                configure(DeserializationFeature.WRAP_EXCEPTIONS, true).
                setSerializationInclusion(JsonInclude.Include.NON_NULL).
               // registerModule(new DateTimeModule()).
                configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public static ObjectMapper getInstance() {
        return JsonObjectMapper.objectMapper;
    }

    public static <T> T parseObject(String payLoad, Class<T> objClass) {
        try {
            return objectMapper.readValue(payLoad, objClass);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static <T> T parseObject(byte[] in, Class<T> objClass) {
        try {
            return objectMapper.readValue(in, objClass);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static <T> T parseObject(ByteBuf in, Class<T> objClass) {
        try {
            return objectMapper.readValue((DataInput) new ByteBufInputStream(in), objClass);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public static <T> List<T> parseList(String payLoad, Class<T> objClass) {
        try {
            JavaType listType = JsonUtil.createListTypeByClass(objClass);
            return objectMapper.readValue(payLoad, listType);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static <T> List<T> parseList(byte[] payLoad, Class<T> objClass) {
        try {
            JavaType listType = JsonUtil.createListTypeByClass(objClass);
            return objectMapper.readValue(payLoad, listType);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static <T> List<T> parseList(InputStream in, Class<T> objClass) {
        try {
            JavaType listType = JsonUtil.createListTypeByClass(objClass);
            return objectMapper.readValue(in, listType);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public static <K, V> Map<K, V> parseMap(String payLoad, Class<K> keyClass, Class<V> valueClass) {
        try {
            JavaType listType = JsonUtil.createMapTypeByClass(keyClass, valueClass);
            return objectMapper.readValue(payLoad, listType);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static <K, V> Map<K, V> parseMap(byte[] payLoad, Class<K> keyClass, Class<V> valueClass) {
        try {
            JavaType listType = JsonUtil.createMapTypeByClass(keyClass, valueClass);
            return objectMapper.readValue(payLoad, listType);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static <K, V> Map<K, V> parseMap(InputStream in, byte[] payLoad, Class<K> keyClass, Class<V> valueClass) {
        try {
            JavaType listType = JsonUtil.createMapTypeByClass(keyClass, valueClass);
            return objectMapper.readValue(in, listType);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public static String toJSONString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static byte[] toType(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static byte[] writeValueAsByte(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
