package com.ares.core.service;


import com.ares.core.annotation.MsgId;
import com.ares.core.bean.AresMsgIdMethod;
import com.ares.core.constdata.FConst;
import com.ares.core.exception.AresBaseException;
import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.protobuf.Parser;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

@Slf4j
public class AresServiceProxy {
    private final AresController aresController;
    private MethodAccess methodAccess;


    public AresServiceProxy(AresController aresController) {
        this.aresController = aresController;
    }

    public void init(IMsgCall iMsgCall) {
        Method[] methods = aresController.getClass().getDeclaredMethods();
        log.info("============ init  service:  {}  begin", aresController.getClass().getSimpleName());
        methodAccess = MethodAccess.get(aresController.getClass());

        for (Method method : methods) {
            MsgId calledMsgId = method.getDeclaredAnnotation(MsgId.class);
            if (calledMsgId == null) {
                continue;
            }
            int msgId = calledMsgId.value();
            String methodName = method.getName();
            AresMsgIdMethod aresMsgIdMethod = new AresMsgIdMethod();
            if (!Modifier.isPublic(method.getModifiers())) {
                log.warn("method name: {} called msgId: {} is not public will be ignored", methodName, msgId);
                continue;
            }
            log.info("--  init methodName: {}  -- callMsgId: {}", methodName, msgId);
            aresMsgIdMethod.setMethodIndex(methodAccess.getIndex(methodName));

            Type paramsType = getType(method);
            Class<?> paramClass = (Class<?>) paramsType;
            aresMsgIdMethod.setAresServiceProxy(this);
            Parser<?> parser = AresMsgIdMethod.pbParser(paramClass);
            aresMsgIdMethod.setParser(parser);

            iMsgCall.onMethodInit(msgId, aresMsgIdMethod);
        }
        log.info("============ init  service ={}  end", aresController.getClass().getSimpleName());
    }

    private Type getType(Method method) {
        int paramCount = method.getParameterCount();
        if (paramCount > 2) {
            throw new AresBaseException(FConst.ERROR_CODE_PARMAS_COUNT_MORE_THAN_ONE, FConst.ERROR_MSG_PARMAS_COUNT_MORE_THAN_ONE);
        }
        Type paramsType = method.getGenericParameterTypes()[0];
        if (paramCount == 2) {
            Type longParamsType = method.getGenericParameterTypes()[0];
            if (longParamsType != long.class && longParamsType != Long.class) {
                throw new AresBaseException(FConst.ERROR_CODE_FIRST_PARAMETER_MUST_LONG, FConst.ERROR_MSG_FIRST_PARAMETER_MUST_LONG);
            }
            paramsType = method.getGenericParameterTypes()[1];
        }
        return paramsType;
    }

    public Object callMethod(AresMsgIdMethod aresMsgIdMethod, Object... paramObj) {
        return methodAccess.invoke(aresController, aresMsgIdMethod.getMethodIndex(), paramObj);
    }
}
