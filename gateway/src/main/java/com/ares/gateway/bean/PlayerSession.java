package com.ares.gateway.bean;

import com.ares.core.tcp.AresTKcpContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerSession {
    private long uid;
    private long sid;
    private AresTKcpContext aresTKcpContext;
    private boolean valid;

    public void cacheMySelf() {
        aresTKcpContext.cacheObj(this);
    }

    public void setValid() {
        this.valid = true;
    }

    public void clearCacheMySelf() {
        aresTKcpContext.cacheObj(null);
    }

    public void close() {
        clearCacheMySelf();
        aresTKcpContext.close();
    }

    public PlayerSession(long uid, AresTKcpContext aresTKcpContext) {
        this.uid = uid;
        this.aresTKcpContext = aresTKcpContext;
    }

    @Override
    public String toString() {
        return "uid:" + uid + " sid:"+sid +" from:" + aresTKcpContext.getCtx().channel();
    }
}
