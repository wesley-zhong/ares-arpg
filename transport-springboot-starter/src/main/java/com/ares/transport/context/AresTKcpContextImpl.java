package com.ares.transport.context;

import com.ares.core.bean.AresPacket;
import com.ares.core.exception.AresBaseException;
import com.ares.core.tcp.AresTKcpContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class AresTKcpContextImpl implements AresTKcpContext {
    private static final String CACHE_KEY = "X_C_K";
    private static final AttributeKey<Object> ATTRIBUTE_KEY = AttributeKey.valueOf(CACHE_KEY);
    private final ChannelHandlerContext ctx;



    @Override
    public AresPacket getRcvPackage() {
        return null;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public AresTKcpContextImpl(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public void send(AresPacket... aresPacket) {
        if (isChannelActive(ctx)) {
            tcpSend(aresPacket);
            return;
        }
        log.error("context={}, not active", this);
    }

    public void send(ByteBuf byteBuf) {
        if (isChannelActive(ctx)) {
            tcpSendByteBuf(byteBuf);
            return;
        }
        byteBuf.release();
        log.error("context={}, not active", this);
    }

    public InetSocketAddress getRemoteAddr() {
        if (ctx != null) {
            return (InetSocketAddress) ctx.channel().remoteAddress();
        }
        return null;
    }

    public int hashCode() {
        if (ctx != null) {
            return ctx.channel().id().asLongText().hashCode();
        }
        return super.hashCode();
    }

    @Override
    public String toString() {
        if (ctx != null) {
            return ctx.channel().toString();
        }
        return null;
    }

    public void cacheObj(Object object) {
        if (ctx != null) {
            ctx.channel().attr(ATTRIBUTE_KEY).set(object);
            return;
        }
        throw new AresBaseException(-10, "XXXXerror");
    }

    public Object getCacheObj() {
        if (ctx != null) {
            return ctx.channel().attr(ATTRIBUTE_KEY).get();
        }
        throw new AresBaseException(-11, "XXXXerror");
    }

    @Override
    public boolean isActive() {
        if (ctx == null) {
            return false;
        }
        if (ctx.channel() == null) {
            return false;
        }
        return ctx.channel().isActive();
    }

    @Override
    public void clearPackageData() {

    }

    public boolean equals(Object target) {
        if (this == target) {
            return true;
        }
        if (target instanceof com.ares.core.tcp.AresTKcpContext ptarget) {
            if (ptarget.hashCode() != this.hashCode()) {
                return false;
            }
            if (ptarget.getCtx() != null && this.getCtx() != null) {
                if (ptarget.getCtx().channel() == this.getCtx().channel()) {
                    return true;
                }
                return ptarget.getCtx().channel().id().asLongText().equals(this.getCtx().channel().id().asLongText());
            }
        }
        return false;
    }

    private boolean isChannelActive(ChannelHandlerContext ctx) {
        return ctx != null && ctx.channel() != null && ctx.channel().isActive();
    }

    private void tcpSend(AresPacket... aresPackets) {
        Channel tcpChanel = ctx.channel();
        if (tcpChanel != null && tcpChanel.isWritable()) {
            for (AresPacket aresPacket : aresPackets) {
                ctx.write(aresPacket);
            }
            ctx.flush();
            return;
        }
        // AresPacket aresPacket = aresPackets[0];
        log.error("ctx ={} send error closed it", ctx);
        ctx.close();
    }

    public void close() {
        if (ctx != null) {
            //when call context.close() if this set null the onCall will not get the current cache
            // ctx.channel().attr(ACACHE_KEY).set(null);
            ctx.channel().close();
        }
    }


    private void tcpSendByteBuf(ByteBuf byteBuf) {
        //ByteBuf will release in channel pipleine
        try {
            ctx.writeAndFlush(byteBuf);
        } catch (Exception e) {
            byteBuf.release();
            log.error("tcp send error", e);
        }
    }
}
