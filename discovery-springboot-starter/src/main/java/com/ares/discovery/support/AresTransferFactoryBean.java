package com.ares.discovery.support;


import com.ares.core.bean.AresPacket;
import com.ares.transport.client.AresTcpClient;
import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

@Slf4j
public class AresTransferFactoryBean implements FactoryBean<Object>, InitializingBean,
        ApplicationContextAware {

    private Class<?> type;
    private String name;
    private String targetServiceName;

    private int areaId;
    private ApplicationContext applicationContext;

    private AresTcpClient aresTcpClient;

    @Override
    public Object getObject() {
        return getTarget();
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = context;
    }


    Object getTarget() {
        aresTcpClient = this.applicationContext.getBean(AresTcpClient.class);
//        InvocationHandler handler = (proxy, method, args) -> {
//
//           // log.info("-----------  call service ={} method ={}", targetServiceName, method.getName());
//            if (args.length == 2) {
//                if(args[1] instanceof Message) {
//                    aresTcpClient.send(areaId, targetServiceName, (int) args[0], (Message) args[1]);
//                    return null;
//                }
//                aresTcpClient.send(areaId, targetServiceName, (AresPacket) args[1]);
//                return null;
//            }
//
//            if (args.length == 3) {
//                if(args[1] instanceof Message) {
//                   // aresTcpClient.send((int) args[0], targetServiceName, (int) args[1], (Message) args[2]);
//                    return null;
//                }
//               // aresTcpClient.send((int) args[0], targetServiceName, (AresPacket) args[1]);
//                return null;
//            }
//            return  null;
//        };
    //    return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return this.type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }


    @Override
    public void afterPropertiesSet() throws Exception {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetServiceName() {
        return targetServiceName;
    }

    public void setTargetServiceName(String targetServiceName) {
        this.targetServiceName = targetServiceName;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

}
