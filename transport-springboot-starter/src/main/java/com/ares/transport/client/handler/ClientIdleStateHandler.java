package com.ares.transport.client.handler;

import com.ares.core.bean.AresPacket;
import com.ares.transport.consts.FMsgId;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientIdleStateHandler extends ChannelInboundHandlerAdapter {

    private static final AresPacket PING = AresPacket.create(FMsgId.PING);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.ALL_IDLE) {
                log.info("================== client idle close ServerIdleStateTrigger {}", ctx.channel().remoteAddress());
                ctx.close();
            } else if (state == IdleState.WRITER_IDLE) {
                ctx.channel().writeAndFlush(PING);
            } else if (state == IdleState.READER_IDLE) {
                log.info("XXXXXXXXXXXXX no receive any msg {} ", ctx.channel().remoteAddress());
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
