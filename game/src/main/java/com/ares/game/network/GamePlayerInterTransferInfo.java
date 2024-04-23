package com.ares.game.network;

import com.ares.common.bean.ServerType;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class GamePlayerInterTransferInfo {
    private Channel gatewayCtx;
    private Channel routerCtx;


    public Channel getContextByType(int serverType) {
        if (serverType == ServerType.GATEWAY.getValue()) {
            return gatewayCtx;
        }
        if (serverType == ServerType.ROUTER.getValue()) {
            return routerCtx;
        }
        return null;
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
