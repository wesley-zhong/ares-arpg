package com.ares.core.tcp;

import com.ares.core.bean.AresPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;


public interface AresTKcpContext {

    AresPacket getRcvPackage();

    ChannelHandlerContext getCtx();

    void send(AresPacket... aresPacket);

    void send(ByteBuf byteBuf);

    InetSocketAddress getRemoteAddr();

    void cacheObj(Object object);

    Object getCacheObj();

    boolean isActive();

    void clearPackageData();

    void close();
}
