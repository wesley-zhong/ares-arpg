package com.router.bean;

import com.ares.common.bean.ServerType;
import com.ares.transport.bean.ServerNodeInfo;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RouterPlayerInterTransferInfo {
    private ServerChannelInfo gameCtx;
    @Getter
    private ServerChannelInfo teamCtx;

    public Channel getContextByType(int serverType) {
        if (serverType == ServerType.GAME.getValue()) {
            return gameCtx == null ? null : gameCtx.getChannel();
        }
        if (serverType == ServerType.TEAM.getValue()) {
            return teamCtx == null ? null : teamCtx.getChannel();
        }
        return null;
    }

    public void setContext(ServerNodeInfo serverNodeInfo, Channel channelHandlerContext) {
        if (serverNodeInfo.getServerType() == ServerType.GAME.getValue()) {
            this.gameCtx = new ServerChannelInfo(serverNodeInfo.getServiceId(), channelHandlerContext);
            return;
        }
        if (serverNodeInfo.getServerType() == ServerType.TEAM.getValue()) {
            this.teamCtx = new ServerChannelInfo(serverNodeInfo.getServiceId(), channelHandlerContext);
            return;
        }
        log.error("serverType ={} not allowed here", serverNodeInfo.getServerType());
    }
}
