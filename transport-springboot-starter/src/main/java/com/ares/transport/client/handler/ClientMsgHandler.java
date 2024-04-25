package com.ares.transport.client.handler;

import com.ares.core.bean.AresPacket;
import com.ares.core.tcp.AresTcpHandler;
import com.ares.transport.consts.FMsgId;
import com.ares.transport.context.AresTKcpContextImplEx;
import com.ares.transport.utils.AresPacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ClientMsgHandler extends ChannelInboundHandlerAdapter {
    private final AresTcpHandler aresTcpHandler;

    public ClientMsgHandler(AresTcpHandler aresTcpHandler) {
        this.aresTcpHandler = aresTcpHandler;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf body = (ByteBuf) msg;
        AresTKcpContextImplEx aresTcpContextEx = AresPacketUtils.parseAresPacket(ctx, body);
        processAresPacket(aresTcpContextEx);
    }

    private void processAresPacket(AresTKcpContextImplEx aresPacketEx) {
        AresPacket aresPacket = aresPacketEx.getRcvPackage();
        int length = aresPacket.getRecvByteBuf().readableBytes();
        try {
            aresPacket.parseHeader();
            if (aresPacket.getMsgId() == FMsgId.PONG) {
                return;
            }
            aresTcpHandler.handleMsgRcv(aresPacketEx);
        } catch (Throwable e) {
            log.error("==error length ={} msgId ={}  ", length, aresPacket.getMsgId(), e);
        } finally {
            aresPacket.release();
            aresPacketEx.clearPackageData();
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("ip ={} connected  success!!!", ctx.channel().remoteAddress());
        aresTcpHandler.onServerConnected(ctx.channel());
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.error(" {} connect lost ", ctx.channel().remoteAddress());
        aresTcpHandler.onServerClosed(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception { // (4)
        log.error("--------------   connect exceptionCaught  ip = {} ", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }
}
