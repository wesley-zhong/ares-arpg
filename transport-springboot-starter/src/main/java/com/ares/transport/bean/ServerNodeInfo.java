package com.ares.transport.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ServerNodeInfo {
    private int serverType;
    private String serviceId;
    private String serviceName;
    private String ip;
    private boolean available;
    private int port;
    private int onlineCount;
    private int id;
    private int groupId;
    private Map<String, String> metaData = new HashMap<>();

    @Override
    public int hashCode() {
        return serviceId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o.hashCode() != this.hashCode()) {
            return false;
        }
        if (o instanceof ServerNodeInfo oS) {
            return oS.getServiceId().equals(this.getServiceId());
        }
        return false;
    }

    @Override
    public String toString() {
        return serviceId;
    }
}
