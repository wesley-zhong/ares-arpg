package com.ares.transport.client;

import com.ares.core.bean.AresPacket;
import com.ares.core.tcp.AresTcpHandler;
import com.ares.transport.client.handler.ClientIdleStateHandler;
import com.ares.transport.client.handler.ClientMsgHandler;
import com.ares.transport.decoder.AresBasedFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class AresTcpClientConn implements InitializingBean {

    @Value("${netty.client.SO_SNDBUF:32768}")
    private int soSndBuf;
    @Value("${netty.client.SO_RCVBUF:32768}")
    private int soRevBuf;
    @Value("${netty.client.WATER_MARK:32768}")
    private int waterMark;
    private Bootstrap bs;
    private AresTcpHandler aresTcpHandler;
    private MessageToByteEncoder<AresPacket> msgEncoder;

    private boolean useLinux() {
        return Epoll.isAvailable();
    }


    public AresTcpClientConn(AresTcpHandler aresTcpHandler, MessageToByteEncoder<AresPacket> msgEncoder) {
        this.aresTcpHandler = aresTcpHandler;
        this.msgEncoder = msgEncoder;
    }


    public void initWithMsgEncoder() {
        boolean useLinux = useLinux();
        log.info(" ################## client use linux={}", useLinux);
        EventLoopGroup bossGroup;
        if (useLinux) {
            bossGroup = new EpollEventLoopGroup(1);
        } else {
            bossGroup = new NioEventLoopGroup(1);
        }

        bs = new Bootstrap();
        bs.group(bossGroup)
                .channel(useLinux ? EpollSocketChannel.class : NioSocketChannel.class)
//                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(waterMark, waterMark))
                .option(ChannelOption.SO_SNDBUF, soSndBuf)
                .option(ChannelOption.SO_RCVBUF, soRevBuf)
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
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initWithMsgEncoder();
    }
}
