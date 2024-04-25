package com.ares.core.bean;

import com.ares.core.service.AresServiceProxy;
import com.google.protobuf.Parser;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Data
@Slf4j
public class AresMsgIdMethod {
    private int methodIndex;
    private Parser<?> parser;
    private AresServiceProxy aresServiceProxy;

    public static Parser<?> pbParser(Class<?> pbClass) {
        try {
            Method parserMethod = pbClass.getMethod("parser");
             return (Parser<?>) parserMethod.invoke(null);
        } catch (Exception e) {
            log.error("XXXXerror", e);
        }
        return  null;
    }
}
