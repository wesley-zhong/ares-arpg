package com.ares.transport.server;


import com.ares.core.bean.AresPacket;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.tcp.AresTcpHandler;
import com.ares.transport.consts.FMsgId;
import com.ares.transport.context.AresTKcpContextImpl;
import com.ares.transport.context.AresTKcpContextImplEx;
import com.ares.transport.utils.AresPacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerPacketHandler extends ChannelInboundHandlerAdapter {
    private static AresTcpHandler aresRpcHandler;
    private int hearBeatCount;
    private int curIgnoreReadIdleCount;
    private static final int MAX_NO_CHECK_COUNT = 64;
    private final int totalIgnoreReadIdleCount;
    private final static int MSG_ID_OFFSET = 4;
    private final static AresPacket PONG = AresPacket.create((short) FMsgId.PONG);


    public ServerPacketHandler(AresTcpHandler aresRpc, int totalIgnoreReadIdleCount) {
        if (aresRpcHandler == null) {
            aresRpcHandler = aresRpc;
        }
        hearBeatCount = 0;
        curIgnoreReadIdleCount = 0;
        this.totalIgnoreReadIdleCount = totalIgnoreReadIdleCount;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object in) {
        ByteBuf body = (ByteBuf) in;
        AresTKcpContextImplEx aresTcpContextEx = AresPacketUtils.parseAresPacket(ctx, body);
        processAresPacket(aresTcpContextEx);
    }

    private void processAresPacket(AresTKcpContextImplEx aresMsgEx) {
        AresPacket aresPacket = aresMsgEx.getRcvPackage();
        int length = aresPacket.getRecvByteBuf().readableBytes();
        try {
            aresPacket.parseHeader();
            //check signature
            boolean ret = checkValid(aresMsgEx, aresPacket.getMsgId());
            if (!ret) {
                return;
            }
            //process heart beat
            if (aresPacket.getMsgId() == FMsgId.PING) {
                sendPing(aresMsgEx.getCtx());
                return;
            }
            aresRpcHandler.handleMsgRcv(aresMsgEx);
        } catch (Throwable e) {
            log.error("==error length ={} msgId ={}  ", length, aresPacket.getMsgId(), e);
        } finally {
            aresPacket.release();
            aresMsgEx.clearPackageData();
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        AresTKcpContextImplEx aresTcpContextEx = new AresTKcpContextImplEx(ctx);
        aresRpcHandler.onClientConnected(aresTcpContextEx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                curIgnoreReadIdleCount++;
                log.info("==================READER_IDLE idle close ServerIdleStateTrigger {} count={}", ctx.channel().remoteAddress(), curIgnoreReadIdleCount);
                if (curIgnoreReadIdleCount > totalIgnoreReadIdleCount) {
                    log.info("======= close socket={}", ctx.channel().remoteAddress());
                    ctx.close();
                }
            }
            return;
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        try {
            log.error("{} ------  socket channelInactive", ctx.channel().remoteAddress());
            AresTKcpContext aresTKcpContext = new AresTKcpContextImpl(ctx);
            aresRpcHandler.onClientClosed(aresTKcpContext);
            ctx.close();
        } finally {
            try {
                super.channelInactive(ctx);
            } catch (Exception e) {
                log.error(" error", e);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("{} ------  socket error, case={}", ctx.channel().remoteAddress(), cause.getMessage());
        ctx.close();
    }

    private void sendPing(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(PONG);
    }

    private boolean checkValid(AresTKcpContextImplEx aresMsgEx, int msgId) {
        if (msgId == FMsgId.PING) {
            if (hearBeatCount < MAX_NO_CHECK_COUNT) {
                hearBeatCount++;
                return true;
            }
            curIgnoreReadIdleCount = totalIgnoreReadIdleCount;
        }
        return aresRpcHandler.isChannelValidate(aresMsgEx);
    }
}