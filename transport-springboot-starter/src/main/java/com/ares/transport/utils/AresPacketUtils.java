package com.ares.transport.utils;


import com.ares.core.bean.AresPacket;
import com.ares.transport.context.AresTKcpContextImplEx;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AresPacketUtils {
    public static AresTKcpContextImplEx parseAresPacket(ChannelHandlerContext ctx, ByteBuf in) {
        AresPacket aresPacket = AresPacket.create();
        aresPacket.setRecvByteBuf(in);
        return new AresTKcpContextImplEx(ctx, aresPacket);
    }

}
