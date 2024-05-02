package com.ares.game.network;

import com.ares.common.bean.ServerType;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class GamePlayerInterTransferInfo {
    private Channel gatewayCtx;
    private long gateSid;
    private Channel routerCtx;
    private volatile long threadHashCode;


    public Channel getContextByType(int serverType) {
        if (serverType == ServerType.GATEWAY.getValue()) {
            return gatewayCtx;
        }
        if (serverType == ServerType.ROUTER.getValue()) {
            return routerCtx;
        }
        return null;
    }

    public void setGateSid(long sid) {
        this.gateSid = sid;
    }

    public long getGateSid() {
        return this.gateSid;
    }

    public void setThreadHashCode(long hashCode) {
        this.threadHashCode = hashCode;
    }

    public long getThreadHashCode() {
        return this.threadHashCode;
    }

    public void setContext(int serverType, Channel channel) {
        if (serverType == ServerType.GATEWAY.getValue()) {
            this.gatewayCtx = channel;
            return;
        }
        if (serverType == ServerType.ROUTER.getValue()) {
            this.routerCtx = channel;
            return;
        }
        log.error("serverType ={} not allowed here", serverType);
    }
}
