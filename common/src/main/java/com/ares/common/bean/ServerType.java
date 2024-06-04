package com.ares.common.bean;

import lombok.Getter;
import lombok.Setter;

public enum ServerType {
    LOGIN(1,"login"),
    GATEWAY(2, "gateway"),
    GAME(3, "game"),
    ROUTER(4, "router"),
    TEAM(5,"team");

    @Getter
    private final int value;
    @Getter
    private final String name;

    private ServerType(int serverType, String serverName) {
        this.value = serverType;
        this.name = serverName;
    }

    public  static  ServerType from(String serverName){
        for (ServerType value : ServerType.values()) {
            if(serverName.contains(value.name)){
                return value;
            }
        }
        return  null;
    }
}
