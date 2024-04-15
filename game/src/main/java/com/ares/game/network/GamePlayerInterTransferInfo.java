package com.ares.game.network;

import com.ares.common.bean.ServerType;
import io.netty.channel.ChannelHandlerContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class GamePlayerInterTransferInfo {
    private ChannelHandlerContext gatewayCtx;
    private ChannelHandlerContext routerCtx;


    public ChannelHandlerContext getContextByType(int serverType){
        if(serverType == ServerType.GATEWAY.getValue()){
            return gatewayCtx;
        }
        if(serverType == ServerType.ROUTER.getValue()){
            return  routerCtx;
        }
        return null;
    }

    public void setContext(int serverType, ChannelHandlerContext channelHandlerContext){
        if(serverType == ServerType.GATEWAY.getValue()){
             this.gatewayCtx =  channelHandlerContext;
             return;
        }
        if(serverType == ServerType.ROUTER.getValue()){
          this.routerCtx = channelHandlerContext;
          return;
        }
        log.error("serverType ={} not allowed here", serverType);
    }
}
