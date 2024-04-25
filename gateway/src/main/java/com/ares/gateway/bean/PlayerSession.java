package com.ares.gateway.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PlayerSession {

    public PlayerSession(long uid) {
        this.uid = uid;
    }

    private long uid;
    private int areaId;
}
