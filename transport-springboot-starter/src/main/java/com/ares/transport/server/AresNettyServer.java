package com.ares.transport.server;

import com.ares.core.bean.AresPacket;
import com.ares.core.tcp.AresTcpHandler;
import com.ares.transport.decoder.AresBasedFrameDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
public class AresNettyServer implements InitializingBean {
    @Value("${server.port:8080}")
    private int port;
    @Value("${server.tcp-port:0}")
    private int tcpPort;

    @Value("${packet.limit:524288}")//512K
    private int packetLimit;

    @Value("${use_linux:true}")
    private boolean isUseLinux;

    @Value("${reuse_port:false}")
    private boolean reusePort;

    @Value(("${tcp.server.heartBeat.time:22000}"))
    private long tcpServerHeartBeatTime;
    @Value("${first.total.ignore.read.idle.count:0}")
    private int firstTotalIgnoreReadIdleCount;
    @Value("${netty.server.SO_SNDBUF:32768}")
    private int soSndBuf;
    @Value("${netty.server.SO_RCVBUF:32768}")
    private int soRevBuf;
    @Value("${netty.server.WATER_MARK:32768}")
    private int waterMark;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private MessageToByteEncoder<AresPacket> msgEncoder;
    private AresTcpHandler aresTcpHandler;


    private final List<Channel> bindChannels = new ArrayList<>();

    public AresNettyServer(AresTcpHandler aresServerTcpHandler, MessageToByteEncoder<AresPacket> msgEncoder) {
        this.msgEncoder = msgEncoder;
        this.aresTcpHandler = aresServerTcpHandler;
    }


    private void runNettyServer() throws Exception {
        log.info("#####isUseEpoll:{}", useLinux());
        ServerBootstrap b = new ServerBootstrap();
        int cpuNum = Runtime.getRuntime().availableProcessors();
        // int eventCount = cpuNum * 2;//2 * cpuNum
        if (useLinux()) {
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup(4);
        } else {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup(4);
        }

        b.group(bossGroup, workerGroup)
                .channel(useLinux() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new IdleStateHandler(tcpServerHeartBeatTime, 0, 0, TimeUnit.MILLISECONDS))
                                .addLast(new ChannelTrafficShapingHandler(0, packetLimit))
                                .addLast(new AresBasedFrameDecoder())
                                .addLast(msgEncoder)
                                .addLast(new ServerPacketHandler(aresTcpHandler, firstTotalIgnoreReadIdleCount));
                    }

                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                        log.error("socket ={} =-----------error", ctx, cause);
                        ctx.close();
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.SO_SNDBUF, soSndBuf)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(waterMark, waterMark))
                .childOption(ChannelOption.SO_RCVBUF, soRevBuf)
                .childOption(ChannelOption.TCP_NODELAY, true);

        int newPort = tcpPort == 0 ? port : tcpPort;
        if (reusePort) {
            b.option(EpollChannelOption.SO_REUSEPORT, true);
            b.option(EpollChannelOption.SO_REUSEADDR, true);
            for (int i = 0; i < cpuNum; ++i) {
                Channel bindChannel = b.bind(newPort).sync().channel();
                log.info("bind port ={} success", newPort);
                bindChannels.add(bindChannel);
            }
        } else {
            Channel bindChannel = b.bind(newPort).sync().channel();
            bindChannels.add(bindChannel);
            log.info("bind port ={} success", newPort);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                destroy();
            }
        }));
        log.info("##################  start tcp  server : {} success", newPort);
    }


    private boolean useLinux() {
        if (isUseLinux) {
            return Epoll.isAvailable();
        }
        return false;
    }

    private void destroy() {
        try {
            for (Channel bindChannel : bindChannels) {
                bindChannel.close().sync();
            }
            // LogicProcessThreadPoolGroup.INSTANCE.shutDown();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            log.info("-XXXXXXXXXXXXXXXXXXXXXXXXXXXX  stop ares netty server success");
        } catch (Exception e) {
            log.error("stop ares netty server failed", e);
        }
    }

    public void afterPropertiesSet() throws Exception {
        runNettyServer();
    }
}
