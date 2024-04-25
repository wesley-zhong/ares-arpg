package com.ares.core.service;

import com.ares.core.bean.AresMsgIdMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ServiceMgr implements IMsgCall, InitializingBean {
    @Autowired
    private List<AresController> aresServices;
    public void init() {
        for (AresController service : aresServices) {
            if (service instanceof Proxy) {
                return;
            }
            AresServiceProxy aresServiceProxy = new AresServiceProxy(service);
            aresServiceProxy.init(this);
        }
    }


    private final Map<String, List<AresServiceProxy>> aresServiceMaps = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    @Override
    public void onMethodInit(int msgId, AresMsgIdMethod aresMsgIdMethod) {
        serviceMethods.put(msgId, aresMsgIdMethod);
    }

    @Override
    public AresMsgIdMethod getCalledMethod(int msgId) {
        return serviceMethods.get(msgId);
    }

    private final Map<Integer, AresMsgIdMethod> serviceMethods = new HashMap<>();
}
