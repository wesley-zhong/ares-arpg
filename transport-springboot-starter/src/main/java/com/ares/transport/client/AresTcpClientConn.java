package com.ares.transport.client;

import com.ares.core.bean.AresPacket;
import com.ares.core.tcp.AresClientTcpHandler;
import com.ares.core.tcp.AresTcpHandler;
import com.ares.transport.client.handler.ClientIdleStateHandler;
import com.ares.transport.client.handler.ClientMsgHandler;
import com.ares.transport.decoder.AresBasedFrameDecoder;
import com.ares.transport.encode.AresPacketMsgEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class AresTcpClientConn {
    private Bootstrap bs;

    public void init(AresTcpHandler aresTcpHandler) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        bs = new Bootstrap();
        bs.group(bossGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(512 * 1024, 1024 * 1024))
                .option(ChannelOption.SO_SNDBUF, 1024 * 1024)
                .option(ChannelOption.SO_RCVBUF, 1024 * 1024)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline()
                                .addLast(new IdleStateHandler(0, 10, 30))
                                .addLast(new ClientIdleStateHandler())
                                .addLast(new AresBasedFrameDecoder())
                                .addLast(new AresPacketMsgEncoder())
                                .addLast(new ClientMsgHandler(aresTcpHandler));
                    }
                });
    }

    public void initWithMsgEncoder(AresTcpHandler aresTcpHandler, MessageToByteEncoder<AresPacket> msgEncoder){
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        bs = new Bootstrap();
        bs.group(bossGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(512 * 1024, 1024 * 1024))
                .option(ChannelOption.SO_SNDBUF, 1024 * 1024)
                .option(ChannelOption.SO_RCVBUF, 1024 * 1024)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline()
                                .addLast(new IdleStateHandler(0, 10, 30))
                                .addLast(new ClientIdleStateHandler())
                                .addLast(new AresBasedFrameDecoder())
                                .addLast(msgEncoder)
                                .addLast(new ClientMsgHandler(aresTcpHandler));
                    }
                });
    }

    public Channel connect(String ip, int port) {
        try {
            ChannelFuture connect = bs.connect(ip, port).sync();
            log.info("----- conn ip ={} port ={} success", ip, port);
            return connect.channel();
        } catch (Exception e) {
            log.error("---conn error msg={}", e.getMessage());
        }
        return  null;
    }
}
